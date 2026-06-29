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
import io.minio.errors.ErrorResponseException;
import io.minio.messages.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private SourceService sourceService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioConfigConstant minioConfigConstant;

    @Mock
    private ExternalApiConfigConstant externalApiConfigConstant;

    @Mock
    private ProjectHelperConstant projectHelperConstant;

    @InjectMocks
    private NewsService newsService;

    private Source testSource;
    private News testNews;
    private NewsSaveRequestDto testNewsSaveRequestDto;
    private NewsUpdateRequestDto testNewsUpdateRequestDto;
    private NewsDeleteRequestDto testNewsDeleteRequestDto;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testSource = new Source();
        testSource.setSourceId(1L);
        testSource.setId("test-id");
        testSource.setName("Test Source");

        testNews = News.builder()
                .id(1L)
                .author("Test Author")
                .title("Test Title")
                .description("Test Description")
                .url("http://test.com")
                .urlToImage("http://test.com/image.jpg")
                .localUrlToImage("/images/test/image.jpg")
                .publishedAt(LocalDateTime.now())
                .content("Test Content")
                .source(testSource)
                .build();

        testNewsSaveRequestDto = new NewsSaveRequestDto(
                "Test Author",
                "Test Title",
                "Test Description",
                "http://test.com",
                "http://test.com/image.jpg",
                "Test Content",
                1L
        );

        testNewsUpdateRequestDto = new NewsUpdateRequestDto(
                "Updated Author",
                "Updated Title",
                "Updated Description",
                "Updated Content",
                1L
        );

        testNewsDeleteRequestDto = new NewsDeleteRequestDto("Test Title");
    }

    @Test
    void saveNews_Success() throws Exception {
        // Given
        when(sourceService.getSourceObjectBySourceId(1L)).thenReturn(testSource);
        when(projectHelperConstant.getImageFolderPath()).thenReturn("/images/");
        ByteArrayResource imageResource = new ByteArrayResource(new byte[]{1, 2, 3});
        when(restTemplate.getForObject(anyString(), eq(ByteArrayResource.class))).thenReturn(imageResource);
        MinioConfigConstant.Bucket bucket = new MinioConfigConstant.Bucket();
        bucket.setName("test-bucket");
        when(minioConfigConstant.getBucket()).thenReturn(bucket);
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        // When
        newsService.saveNews(testNewsSaveRequestDto);

        // Then
        verify(sourceService).getSourceObjectBySourceId(1L);
        verify(restTemplate).getForObject(eq("http://test.com/image.jpg"), eq(ByteArrayResource.class));
        verify(minioClient).putObject(any(PutObjectArgs.class));
        verify(newsRepository).saveNews(any(News.class));
    }

    @Test
    void saveNews_ImageDownloadFails_ThrowsIncompleteProcessException() {
        // Given
        when(sourceService.getSourceObjectBySourceId(1L)).thenReturn(testSource);
        when(projectHelperConstant.getImageFolderPath()).thenReturn("/images/");
        when(restTemplate.getForObject(anyString(), eq(ByteArrayResource.class))).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> newsService.saveNews(testNewsSaveRequestDto))
                .isInstanceOf(IncompleteProcessException.class)
                .hasMessageContaining("Failed to download image");
    }

    @Test
    void getAllNews_Success() {
        // Given
        List<News> newsList = Arrays.asList(testNews);
        when(projectHelperConstant.getNewsQueryLimit()).thenReturn(10);
        when(newsRepository.findAllNews(10)).thenReturn(newsList);

        // When
        List<NewsResponseDto> result = newsService.getAllNews();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Test Title");
        verify(newsRepository).findAllNews(10);
    }

    @Test
    void getNewsById_Success() {
        // Given
        when(newsRepository.findNewsById(1L)).thenReturn(Optional.of(testNews));

        // When
        NewsResponseDto result = newsService.getNewsById(1L);

        // Then
        assertThat(result.title()).isEqualTo("Test Title");
        verify(newsRepository).findNewsById(1L);
    }

    @Test
    void getNewsById_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(newsRepository.findNewsById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> newsService.getNewsById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("News not found with id: 1");
    }

    @Test
    void updateNewsById_Success() {
        // Given
        when(newsRepository.findNewsById(1L)).thenReturn(Optional.of(testNews));
        when(newsRepository.updateNews(anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(Optional.of(testNews));

        // When
        NewsResponseDto result = newsService.updateNewsById(1L, testNewsUpdateRequestDto);

        // Then
        assertThat(result).isNotNull();
        verify(newsRepository).updateNews(anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    void deleteNewsById_Success() {
        // Given
        when(newsRepository.findNewsById(1L)).thenReturn(Optional.of(testNews));
        when(newsRepository.deleteNewsById(1L)).thenReturn(true);

        // When
        newsService.deleteNewsById(1L);

        // Then
        verify(newsRepository).deleteNewsById(1L);
    }

    @Test
    void deleteNewsById_Fails_ThrowsIncompleteProcessException() {
        // Given
        when(newsRepository.findNewsById(1L)).thenReturn(Optional.of(testNews));
        when(newsRepository.deleteNewsById(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> newsService.deleteNewsById(1L))
                .isInstanceOf(IncompleteProcessException.class)
                .hasMessageContaining("Problem occurred while deleting");
    }

    @Test
    void deleteNewsByTitle_Success() {
        // Given
        when(newsRepository.deleteNewsByTitle("Test Title")).thenReturn(true);

        // When
        newsService.deleteNewsByTitle(testNewsDeleteRequestDto);

        // Then
        verify(newsRepository).deleteNewsByTitle("Test Title");
    }

    @Test
    void deleteNewsByTitle_BlankTitle_ThrowsIncompleteProcessException() {
        // Given
        NewsDeleteRequestDto blankTitleDto = new NewsDeleteRequestDto("");

        // When & Then
        assertThatThrownBy(() -> newsService.deleteNewsByTitle(blankTitleDto))
                .isInstanceOf(IncompleteProcessException.class)
                .hasMessageContaining("Title is empty or blank");
    }

    @Test
    void deleteNewsByTitle_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(newsRepository.deleteNewsByTitle("Test Title")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> newsService.deleteNewsByTitle(testNewsDeleteRequestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("News not found with title");
    }

    @Test
    void getNewsByTitle_FoundInDatabase_Success() {
        // Given
        when(newsRepository.findByTitle("Test Title")).thenReturn(Optional.of(testNews));

        // When
        NewsResponseDto result = newsService.getNewsByTitle("Test Title");

        // Then
        assertThat(result.title()).isEqualTo("Test Title");
        verify(newsRepository).findByTitle("Test Title");
        verifyNoInteractions(restTemplate);
    }

    @Test
    void getNewsByTitle_NotInDatabase_FetchesFromExternalApi() {
        // Given
        when(newsRepository.findByTitle("Test Title")).thenReturn(Optional.empty());
        
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setArticles(Arrays.asList(testNews));
        
        when(externalApiConfigConstant.getUrl()).thenReturn("http://api.test.com/");
        when(externalApiConfigConstant.getHeader()).thenReturn("X-API-Key");
        when(externalApiConfigConstant.getSecretKey()).thenReturn("secret");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(ApiResponse.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));
        when(projectHelperConstant.getImageFolderPath()).thenReturn("/images/");
        when(projectHelperConstant.getSourceBatchSize()).thenReturn(10);
        when(newsRepository.saveAll(anyList(), anyInt())).thenReturn(Arrays.asList(testNews));

        // When
        NewsResponseDto result = newsService.getNewsByTitle("Test Title");

        // Then
        assertThat(result).isNotNull();
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(ApiResponse.class));
    }

    @Test
    void getNewsByTitle_BlankTitle_ThrowsResourceNotFoundException() {
        // When & Then
        assertThatThrownBy(() -> newsService.getNewsByTitle(""))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("News not found with title");
    }

    @Test
    void getNewsFromExternalApi_Success() {
        // Given
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setArticles(Arrays.asList(testNews));
        
        when(externalApiConfigConstant.getUrl()).thenReturn("http://api.test.com/");
        when(externalApiConfigConstant.getHeader()).thenReturn("X-API-Key");
        when(externalApiConfigConstant.getSecretKey()).thenReturn("secret");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(ApiResponse.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));
        when(projectHelperConstant.getImageFolderPath()).thenReturn("/images/");
        when(projectHelperConstant.getSourceBatchSize()).thenReturn(10);
        when(newsRepository.saveAll(anyList(), anyInt())).thenReturn(Arrays.asList(testNews));

        // When
        List<NewsResponseDto> result = newsService.getNewsFromExternalApi("test query");

        // Then
        assertThat(result).hasSize(1);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(ApiResponse.class));
    }

    @Test
    void saveImageToMinio_Success() throws Exception {
        // Given
        byte[] imageData = new byte[]{1, 2, 3};
        MinioConfigConstant.Bucket bucket = new MinioConfigConstant.Bucket();
        bucket.setName("test-bucket");
        when(minioConfigConstant.getBucket()).thenReturn(bucket);
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        // When
        newsService.saveImageToMinio("/test/image.jpg", imageData);

        // Then
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void saveImageToMinio_BucketDoesNotExist_CreatesBucket() throws Exception {
        // Given
        byte[] imageData = new byte[]{1, 2, 3};
        MinioConfigConstant.Bucket bucket = new MinioConfigConstant.Bucket();
        bucket.setName("test-bucket");
        when(minioConfigConstant.getBucket()).thenReturn(bucket);
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

        // When
        newsService.saveImageToMinio("/test/image.jpg", imageData);

        // Then
        verify(minioClient).makeBucket(any(MakeBucketArgs.class));
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void saveImageToMinio_ErrorResponseException_ThrowsIncompleteProcessException() throws Exception {
        // Given
        byte[] imageData = new byte[]{1, 2, 3};
        MinioConfigConstant.Bucket bucket = new MinioConfigConstant.Bucket();
        bucket.setName("test-bucket");
        when(minioConfigConstant.getBucket()).thenReturn(bucket);
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        
        ErrorResponse errorResponse = mock(ErrorResponse.class);
        when(errorResponse.message()).thenReturn("Error message");
        ErrorResponseException exception = new ErrorResponseException(errorResponse, null, null);
        
        doThrow(exception).when(minioClient).putObject(any(PutObjectArgs.class));

        // When & Then
        assertThatThrownBy(() -> newsService.saveImageToMinio("/test/image.jpg", imageData))
                .isInstanceOf(IncompleteProcessException.class)
                .hasMessageContaining("MinIO responded with an error");
    }

    @Test
    void readFileFromMinio_Success() throws Exception {
        // Given
        byte[] imageData = new byte[]{1, 2, 3};
        InputStream inputStream = new ByteArrayInputStream(imageData);
        
        when(projectHelperConstant.getImageFolderPath()).thenReturn("/images/");
        MinioConfigConstant.Bucket bucket = new MinioConfigConstant.Bucket();
        bucket.setName("test-bucket");
        when(minioConfigConstant.getBucket()).thenReturn(bucket);
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        
        // Mock GetObjectResponse as it extends InputStream
        GetObjectResponse mockResponse = mock(GetObjectResponse.class);
        doAnswer(invocation -> inputStream.read()).when(mockResponse).read();
        doAnswer(invocation -> inputStream.read((byte[]) invocation.getArgument(0))).when(mockResponse).read(any(byte[].class));
        doAnswer(invocation -> inputStream.read((byte[]) invocation.getArgument(0), 
                (int) invocation.getArgument(1), (int) invocation.getArgument(2)))
                .when(mockResponse).read(any(byte[].class), anyInt(), anyInt());
        
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);

        // When
        NewsImageResponseDto result = newsService.readFileFromMinio("folder", "image.jpg");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.contentLength()).isEqualTo(3);
        verify(minioClient).getObject(any(GetObjectArgs.class));
    }

    @Test
    void readFileFromMinio_MinioException_ThrowsIncompleteProcessException() throws Exception {
        // Given
        when(projectHelperConstant.getImageFolderPath()).thenReturn("/images/");
        MinioConfigConstant.Bucket bucket = new MinioConfigConstant.Bucket();
        bucket.setName("test-bucket");
        when(minioConfigConstant.getBucket()).thenReturn(bucket);
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        when(minioClient.getObject(any(GetObjectArgs.class))).thenThrow(new IOException("MinIO error"));

        // When & Then
        assertThatThrownBy(() -> newsService.readFileFromMinio("folder", "image.jpg"))
                .isInstanceOf(IncompleteProcessException.class)
                .hasMessageContaining("Error occurred while retrieving the image");
    }

    @Test
    void downloadAndSaveImage_Success() throws Exception {
        // Given
        ByteArrayResource imageResource = new ByteArrayResource(new byte[]{1, 2, 3});
        when(restTemplate.getForObject(anyString(), eq(ByteArrayResource.class))).thenReturn(imageResource);
        MinioConfigConstant.Bucket bucket = new MinioConfigConstant.Bucket();
        bucket.setName("test-bucket");
        when(minioConfigConstant.getBucket()).thenReturn(bucket);
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        // When
        newsService.downloadAndSaveImage("http://test.com/image.jpg", "/test/path.jpg");

        // Then
        verify(restTemplate).getForObject("http://test.com/image.jpg", ByteArrayResource.class);
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void downloadAndSaveImage_NullResource_ThrowsIOException() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(ByteArrayResource.class))).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> newsService.downloadAndSaveImage("http://test.com/image.jpg", "/test/path.jpg"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Failed to download image");
    }

    @Test
    void getNewsFromExternalApiDefaultQuery_Success() {
        // Given
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setArticles(Arrays.asList(testNews));
        
        when(projectHelperConstant.getDefaultDummyNewsSearchQuery()).thenReturn("default query");
        when(externalApiConfigConstant.getUrl()).thenReturn("http://api.test.com/");
        when(externalApiConfigConstant.getHeader()).thenReturn("X-API-Key");
        when(externalApiConfigConstant.getSecretKey()).thenReturn("secret");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(ApiResponse.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));
        when(projectHelperConstant.getImageFolderPath()).thenReturn("/images/");
        when(projectHelperConstant.getSourceBatchSize()).thenReturn(10);
        when(newsRepository.saveAll(anyList(), anyInt())).thenReturn(Arrays.asList(testNews));

        // When
        List<NewsResponseDto> result = newsService.getNewsFromExternalApiDefaultQuery();

        // Then
        assertThat(result).hasSize(1);
        verify(projectHelperConstant).getDefaultDummyNewsSearchQuery();
    }

    @Test
    void updateNewsById_ResourceNotFound_ThrowsException() {
        // Given
        when(newsRepository.findNewsById(1L)).thenReturn(Optional.of(testNews));
        when(newsRepository.updateNews(anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> newsService.updateNewsById(1L, testNewsUpdateRequestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("News not found with id");
    }

    @Test
    void setLocalUrlToExternalNewsImages_Success() throws Exception {
        // Given
        News newsWithUrl = News.builder()
                .urlToImage("http://test.com/image.jpg")
                .build();
        
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setArticles(Arrays.asList(newsWithUrl));
        
        when(externalApiConfigConstant.getUrl()).thenReturn("http://api.test.com/");
        when(externalApiConfigConstant.getHeader()).thenReturn("X-API-Key");
        when(externalApiConfigConstant.getSecretKey()).thenReturn("secret");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(ApiResponse.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));
        when(projectHelperConstant.getImageFolderPath()).thenReturn("/images/");
        when(projectHelperConstant.getSourceBatchSize()).thenReturn(10);
        
        ByteArrayResource imageResource = new ByteArrayResource(new byte[]{1, 2, 3});
        when(restTemplate.getForObject(anyString(), eq(ByteArrayResource.class))).thenReturn(imageResource);
        MinioConfigConstant.Bucket bucket = new MinioConfigConstant.Bucket();
        bucket.setName("test-bucket");
        when(minioConfigConstant.getBucket()).thenReturn(bucket);
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        when(newsRepository.saveAll(anyList(), anyInt())).thenReturn(Arrays.asList(newsWithUrl));

        // When
        List<NewsResponseDto> result = newsService.getNewsFromExternalApi("test");

        // Then
        assertThat(result).hasSize(1);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<News>> newsCaptor = ArgumentCaptor.forClass(List.class);
        verify(newsRepository).saveAll(newsCaptor.capture(), anyInt());
        assertThat(newsCaptor.getValue().get(0).getLocalUrlToImage()).isNotNull();
    }

    @Test
    void setLocalUrlToExternalNewsImages_DownloadFails_ThrowsIncompleteProcessException() {
        // Given
        News newsWithUrl = News.builder()
                .urlToImage("http://test.com/image.jpg")
                .build();
        
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setArticles(Arrays.asList(newsWithUrl));
        
        when(externalApiConfigConstant.getUrl()).thenReturn("http://api.test.com/");
        when(externalApiConfigConstant.getHeader()).thenReturn("X-API-Key");
        when(externalApiConfigConstant.getSecretKey()).thenReturn("secret");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(ApiResponse.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));
        when(projectHelperConstant.getImageFolderPath()).thenReturn("/images/");
        when(restTemplate.getForObject(anyString(), eq(ByteArrayResource.class))).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> newsService.getNewsFromExternalApi("test"))
                .isInstanceOf(IncompleteProcessException.class)
                .hasMessageContaining("Error downloading image");
    }

}
