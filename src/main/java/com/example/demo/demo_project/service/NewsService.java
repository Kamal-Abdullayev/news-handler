package com.example.demo.demo_project.service;

import com.example.demo.demo_project.constants.ExternalApiConfigConstant;
import com.example.demo.demo_project.constants.MinioConfigConstant;
import com.example.demo.demo_project.constants.ProjectHelperConstant;
import com.example.demo.demo_project.dto.*;
import com.example.demo.demo_project.entity.ApiResponse;
import com.example.demo.demo_project.entity.News;
import com.example.demo.demo_project.entity.Source;
import com.example.demo.demo_project.exception.IncompleteProcessException;
import com.example.demo.demo_project.exception.ResourceNotFoundException;
import com.example.demo.demo_project.repository.NewsRepository;
import io.minio.*;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
@CacheConfig(cacheNames = {"demo-cache"})
public class NewsService {
    private final NewsRepository newsRepository;
    private final SourceService sourceService;
    private final RestTemplate restTemplate;
    private final MinioClient minioClient;
    private final MinioConfigConstant minioConfigConstant;
    private final ExternalApiConfigConstant externalApiConfigConstant;
    private final ProjectHelperConstant projectHelperConstant;

    @Transactional
    public void saveNews(NewsSaveRequestDto newsSaveRequestDto) {
        Source source = sourceService.getSourceObjectBySourceId(newsSaveRequestDto.sourceId());
        String imagePathForFileStorageSystem = projectHelperConstant.getImageFolderPath()
                + UUID.randomUUID() + "/"
                + System.currentTimeMillis() + ".jpg";

        log.info("News image path for file storage system: {}", imagePathForFileStorageSystem);
        try {
            downloadAndSaveImage(newsSaveRequestDto.urlToImage(), imagePathForFileStorageSystem);
            log.info("News image successfully saved to file storage system");
        } catch (IOException e) {
            log.error("Failed to download image from URL: {}. Error: {}", newsSaveRequestDto.urlToImage(), e.getMessage());
            throw new IncompleteProcessException("Failed to download image from " + newsSaveRequestDto.urlToImage(), e);
        }
        newsRepository.saveNews(
                News.builder()
                        .author(newsSaveRequestDto.author())
                        .title(newsSaveRequestDto.title())
                        .description(newsSaveRequestDto.description())
                        .url(newsSaveRequestDto.url())
                        .urlToImage(newsSaveRequestDto.urlToImage())
                        .localUrlToImage(imagePathForFileStorageSystem)
                        .publishedAt(LocalDateTime.now())
                        .content(newsSaveRequestDto.content())
                        .source(source)
                        .build()
        );
        log.info("News saved successfully with title: {}", newsSaveRequestDto.title());
    }

    public List<NewsResponseDto> getAllNews() {
        log.info("The news are querying with limit: {}", projectHelperConstant.getNewsQueryLimit());
        return newsRepository.findAllNews(projectHelperConstant.getNewsQueryLimit())
                .stream()
                .map(NewsResponseDto::convert)
                .toList();
    }

    public NewsResponseDto getNewsById(Long id) {
        log.info("The news is querying with id: {}", id);
        return NewsResponseDto.convert(getNewsObjectById(id));
    }

    protected News getNewsObjectById(Long id) {
        log.info("The news object is querying with id: {}", id);
        return newsRepository.findNewsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News not found with id: " + id));
    }

    @Transactional
    public NewsResponseDto updateNewsById(Long id, NewsUpdateRequestDto newsSaveRequestDto) {
        News news = getNewsObjectById(id);
        log.info("The news is updating with id: {}", id);
        return NewsResponseDto.convert(updateNewsObjectByRequestedData(news, newsSaveRequestDto));
    }

    @Transactional
    public void deleteNewsById(Long id) {
        News news = getNewsObjectById(id);
        log.info("The news is deleting with id: {}", id);
        if (!newsRepository.deleteNewsById(news.getId()))
            throw new IncompleteProcessException("Problem occurred while deleting the entity with id: " + news.getId());
    }

    @Transactional
    public void deleteNewsByTitle(NewsDeleteRequestDto newsDeleteRequestDto) {
        String title = newsDeleteRequestDto.title();
        log.info("The news is deleting with title: {}", title);
        if (title.isBlank())
            throw new IncompleteProcessException("Title is empty or blank");

        if (!newsRepository.deleteNewsByTitle(title))
            throw new ResourceNotFoundException("News not found with title: " + title);
    }

    @Cacheable(cacheNames = "demo-cache", key = "#title")
    public NewsResponseDto getNewsByTitle(String title) {
        log.info("The news are querying with title: {}", title);

        if (title.isBlank()) {
            log.error("Title is empty or blank");
            throw new ResourceNotFoundException("News not found with title: " + title);
        }
        Optional<News> newsOptional = newsRepository.findByTitle(title);
        return newsOptional.map(news -> {
            log.info("The news with title {} exist in the database", title);
            return NewsResponseDto.convert(news);
        }).orElseGet(() -> getNewsFromExternalApiByTitle(title));
    }

    private NewsResponseDto getNewsFromExternalApiByTitle(String title) {
        log.info("The news are querying with title: {} from external api", title);
        ResponseEntity<ApiResponse> newsEntityResponse = sendRequestToExternalApi("q=" + title + "&from=2024-11-30&to=2024-11-1");
        List<News> newsList = setLocalUrlToExternalNewsImages(
                Objects.requireNonNull(newsEntityResponse.getBody()).getArticles());
        return NewsResponseDto.convert(
                saveAllExternalApiNewsEntities(newsList, 1).get(0)
        );
    }

    public List<NewsResponseDto> getNewsFromExternalApi(String searchKeyword) {
        ResponseEntity<ApiResponse> newsEntityResponse = sendRequestToExternalApi(searchKeyword);
        List<News> newsList = setLocalUrlToExternalNewsImages(
                Objects.requireNonNull(newsEntityResponse.getBody()).getArticles());
        return saveAllExternalApiNewsEntities(newsList, projectHelperConstant.getSourceBatchSize()).stream()
                .map(NewsResponseDto::convert)
                .toList();
    }

    private ResponseEntity<ApiResponse> sendRequestToExternalApi(String searchKeyword) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(externalApiConfigConstant.getHeader(), externalApiConfigConstant.getSecretKey());
        HttpEntity<String> entity = new HttpEntity<>(headers);
        log.info("The news are querying with keyword: {} from external api", searchKeyword);
        return restTemplate.exchange(
                addSearchCriteriaToUrl(searchKeyword),
                HttpMethod.GET,
                entity,
                ApiResponse.class
        );
    }


    public List<NewsResponseDto> getNewsFromExternalApiDefaultQuery() {
        log.info("The news are querying with default keyword: {} from external api", projectHelperConstant.getDefaultDummyNewsSearchQuery());
        return getNewsFromExternalApi(projectHelperConstant.getDefaultDummyNewsSearchQuery());
    }

    private List<News> saveAllExternalApiNewsEntities(List<News> newsList, int batchSize) {
        log.info("Saving {} news", newsList.size());
        return newsRepository.saveAll(newsList, batchSize);
    }

    private String addSearchCriteriaToUrl(String keyword) {
        log.info("Search from external api: {}", externalApiConfigConstant.getUrl() + keyword);
        return externalApiConfigConstant.getUrl() + keyword;
    }

    private News updateNewsObjectByRequestedData(News news, NewsUpdateRequestDto newsSaveRequestDto) {
        log.info("The news object is updating with id: {}", news.getId());

        if (newsSaveRequestDto.title() != null &&
                !newsSaveRequestDto.title().isEmpty() &&
                newsSaveRequestDto.title().equals(news.getTitle())) {
            news.setTitle(newsSaveRequestDto.title());
            log.info("The news title is updated with id: {}", news.getId());
        }
        if (newsSaveRequestDto.description() != null &&
                newsSaveRequestDto.description().isEmpty() &&
                newsSaveRequestDto.description().equals(news.getDescription())) {
            news.setDescription(newsSaveRequestDto.description());
            log.info("The news description is updated with id: {}", news.getId());
        }
        if (newsSaveRequestDto.author() != null &&
                !newsSaveRequestDto.author().isEmpty() &&
                newsSaveRequestDto.author().equals(news.getAuthor())) {
            news.setAuthor(newsSaveRequestDto.author());
            log.info("The news author is updated with id: {}", news.getId());
        }
        if (newsSaveRequestDto.content() != null &&
                newsSaveRequestDto.content().isEmpty() &&
                newsSaveRequestDto.content().equals(news.getContent())) {
            news.setContent(newsSaveRequestDto.content());
            log.info("The news content is updated with id: {}", news.getId());
        }
        if (newsSaveRequestDto.sourceId() > 0 &&
                Objects.equals(newsSaveRequestDto.content(), news.getContent()) &&
                sourceService.getSourceObjectBySourceId(news.getSource().getSourceId()) != null) {
            news.setContent(newsSaveRequestDto.content());
            log.info("The news content is updated with id: {}", news.getId());
        }

        return newsRepository.updateNews(news.getId(),
                        newsSaveRequestDto.author(),
                        newsSaveRequestDto.title(),
                        newsSaveRequestDto.description(),
                        newsSaveRequestDto.content(),
                        newsSaveRequestDto.sourceId()
                ).orElseThrow(() -> new ResourceNotFoundException("News not found with id: " + news.getId()));
    }

    protected void downloadAndSaveImage(String imageUrl, String path) throws IOException {
        log.info("Image will download to \"{}\" path with \"{}\"", path, imageUrl);
        ByteArrayResource imageResource = restTemplate.getForObject(imageUrl, ByteArrayResource.class);

        if (imageResource != null) {
            log.info("Image uploading to file storage system");
            saveImageToMinio(path, imageResource.getByteArray());
        } else {
            log.error("Image download failed with url \"{}\" for \"{}\" path", imageUrl, path);
            throw new IOException("Failed to download image from: " + imageUrl);
        }
    }

    public void saveImageToMinio(String imagePath, byte[] imageContent) {
        log.info("Image will upload to MinIO bucket with \"{}\" path", imagePath);
        try {
            checkIfBucketNotExistCreate();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfigConstant.getBucket().getName())
                    .object(imagePath)
                    .stream(new ByteArrayInputStream(imageContent),
                            imageContent.length, -1)
                    .build()
            );
            log.info("Image successfully saved to MinIO bucket: {}", minioConfigConstant.getBucket());

        } catch (ErrorResponseException e) {
            log.error("Error response from MinIO for image '{}': {}", imagePath, e.errorResponse().message());
            throw new IncompleteProcessException("MinIO responded with an error. Image upload failed.", e);

        } catch (InsufficientDataException e) {
            log.error("Insufficient data for image '{}': {}", imagePath, e.getMessage());
            throw new IncompleteProcessException("Upload failed due to insufficient data.", e);

        } catch (ServerException e) {
            log.error("Server error while uploading image '{}': {}", imagePath, e.getMessage());
            throw new IncompleteProcessException("Upload failed due to a server error.", e);

        } catch (IOException | InternalException | InvalidKeyException |
                 InvalidResponseException | NoSuchAlgorithmException | XmlParserException e) {
            log.error("General error while uploading image '{}': {}", imagePath, e.getMessage());
            throw new IncompleteProcessException("An error occurred while uploading the image.", e);
        }

    }

    public NewsImageResponseDto readFileFromMinio(String folderPath, String imageName) {
        log.info("Read image from MinIO bucket with \"{}\" path and \"{}\" name", folderPath, imageName);
        String imagePath = projectHelperConstant.getImageFolderPath() + folderPath + "/" + imageName;
        checkIfBucketNotExistCreate();
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioConfigConstant.getBucket().getName())
                        .object(imagePath)
                        .build());
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            byte[] imageData = outputStream.toByteArray();
            log.info("Image successfully read from MinIO bucket: {}", minioConfigConstant.getBucket());
            return new NewsImageResponseDto(
                    new ByteArrayResource(imageData),
                    imageData.length);

        } catch (MinioException | IOException e) {
            log.error("Error occurred while retrieving the image \"'{}'\": {}", imagePath, e.getMessage());
            throw new IncompleteProcessException("Error occurred while retrieving the image", e);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error while uploading image to \"'{}'\": {}", imagePath, e.getMessage());
            throw new IncompleteProcessException("An error occurred while uploading the image.", e);
        }
    }

    private List<News> setLocalUrlToExternalNewsImages(List<News> newsList) {
        log.info("Setting local URL to external news images");
         newsList.forEach(news -> {
            try {
                String imagePath = projectHelperConstant.getImageFolderPath()
                        + UUID.randomUUID() + "/"
                        + System.currentTimeMillis() + ".jpg";
                log.info("Image path for file storage system: {}", imagePath);
                if (news.getUrlToImage() != null) {
                    downloadAndSaveImage(news.getUrlToImage(), imagePath);
                    news.setLocalUrlToImage(imagePath);
                    log.info("Image successfully saved to file storage system");
                }

            } catch (IOException e) {
                log.error("Failed to download image for news item with URL: {}. Error: {}", news.getUrlToImage(), e.getMessage());
                throw new IncompleteProcessException("Error downloading image for news item with URL: " + news.getUrlToImage(), e);
            }
        });
         log.info("Local URL to external news images successfully set");
         return newsList;
    }

    private void checkIfBucketNotExistCreate() {
        log.info("Checking if bucket '{}' exist", minioConfigConstant.getBucket());
        boolean found;
        try {
            String bucketName = minioConfigConstant.getBucket().getName();
            found = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(minioConfigConstant.getBucket().getName()).build());
            if (found) {
                log.info("Bucket '{}' already exists.", bucketName);
            } else {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Bucket '{}' created successfully.", bucketName);
            }
        }
        catch (Exception e) {
            log.error("Failed to create a bucket: {}. Error: {}", minioConfigConstant.getBucket(), e.getMessage());
            throw new IncompleteProcessException("Error while creating bucket: " + minioConfigConstant.getBucket(), e);
        }
    }

}
