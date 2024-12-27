package com.example.demo.demo_project.controller;

import com.example.demo.demo_project.dto.*;
import com.example.demo.demo_project.service.NewsService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/news")
@RestController
@RateLimiter(name = "demo-project")
@Slf4j
public class NewsController {
    private final NewsService newsService;

    @GetMapping("/all")
    public ResponseEntity<List<NewsResponseDto>> getAllNews() {
        return new ResponseEntity<>(newsService.getAllNews(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsResponseDto> getNewsById(@PathVariable Long id) {
        return new ResponseEntity<>(newsService.getNewsById(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<HttpStatus> saveNews(@Valid @RequestBody NewsSaveRequestDto newsSaveRequestDto) {
        log.info("saveNews method called");
        newsService.saveNews(newsSaveRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NewsResponseDto> updateNewsById(@PathVariable Long id, @Valid @RequestBody NewsUpdateRequestDto newsUpdateRequestDto) {
        return new ResponseEntity<>(newsService.updateNewsById(id, newsUpdateRequestDto), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<NewsResponseDto> deleteNewsById(@PathVariable Long id) {
        newsService.deleteNewsById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    public ResponseEntity<NewsResponseDto> deleteNewsByTitle(@Valid @RequestBody NewsDeleteRequestDto newsDeleteRequestDto) {
        newsService.deleteNewsByTitle(newsDeleteRequestDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/external/all")
    public ResponseEntity<List<NewsResponseDto>> getExternalNews() {
        return new ResponseEntity<>(newsService.getNewsFromExternalApiDefaultQuery(), HttpStatus.OK);
    }

    @GetMapping("/external")
    public ResponseEntity<NewsResponseDto> getExternalNewsByKeyword(@Valid @RequestBody NewsRequestByKeywordDto newsRequestByKeywordDto) {
        return new ResponseEntity<>(newsService.getNewsByTitle(newsRequestByKeywordDto.title()), HttpStatus.OK);
    }

    @GetMapping("/{folderPath}/{imageName}")
    public ResponseEntity<ByteArrayResource> getNewsByImageFolder(@PathVariable("folderPath") String folderPath,
                                                                  @PathVariable String imageName) {
        NewsImageResponseDto imageResponseDto = newsService.readFileFromMinio(folderPath, imageName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + imageName + "\"")
                .contentType(MediaType.IMAGE_JPEG) // Adjust based on your image type, e.g., IMAGE_PNG
                .contentLength(imageResponseDto.contentLength())
                .body(imageResponseDto.byteArrayResource());

    }
}
