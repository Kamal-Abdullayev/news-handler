package com.example.demo.demo_project.controller;

import com.example.demo.demo_project.dto.*;
import com.example.demo.demo_project.service.NewsExportService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@RequestMapping("/api/v1/export")
@RestController
@RateLimiter(name = "demo-project")
@Slf4j
public class NewsExportController {
    private final NewsExportService newsExportService;

    @PostMapping("/json")
    public ResponseEntity<byte[]> exportNewsAsJson(@Valid @RequestBody NewsExportRequestDto exportRequest) {
        log.info("Exporting news as JSON with criteria: {}", exportRequest);
        
        NewsExportDto exportDto = newsExportService.exportNewsAsJson(exportRequest);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", exportDto.filename());
        headers.setContentLength(exportDto.content().length);
        
        return new ResponseEntity<>(exportDto.content(), headers, HttpStatus.OK);
    }

    @PostMapping("/csv")
    public ResponseEntity<byte[]> exportNewsAsCsv(@Valid @RequestBody NewsExportRequestDto exportRequest) {
        log.info("Exporting news as CSV with criteria: {}", exportRequest);
        
        NewsExportDto exportDto = newsExportService.exportNewsAsCsv(exportRequest);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("text/csv"));
        headers.setContentDispositionFormData("attachment", exportDto.filename());
        headers.setContentLength(exportDto.content().length);
        
        return new ResponseEntity<>(exportDto.content(), headers, HttpStatus.OK);
    }

    @PostMapping("/report")
    public ResponseEntity<byte[]> generateNewsReport(@Valid @RequestBody NewsReportRequestDto reportRequest) {
        log.info("Generating news report with criteria: {}", reportRequest);
        
        NewsExportDto exportDto = newsExportService.generateNewsReport(reportRequest);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", exportDto.filename());
        headers.setContentLength(exportDto.content().length);
        
        return new ResponseEntity<>(exportDto.content(), headers, HttpStatus.OK);
    }

    @GetMapping("/statistics")
    public ResponseEntity<byte[]> exportNewsStatistics() {
        log.info("Exporting news statistics");
        
        NewsExportDto exportDto = newsExportService.exportNewsStatistics();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", exportDto.filename());
        headers.setContentLength(exportDto.content().length);
        
        return new ResponseEntity<>(exportDto.content(), headers, HttpStatus.OK);
    }

    @GetMapping("/trending-topics")
    public ResponseEntity<byte[]> exportTrendingTopics(@RequestParam(defaultValue = "7") int days) {
        log.info("Exporting trending topics for the last {} days", days);
        
        NewsExportDto exportDto = newsExportService.exportTrendingTopics(days);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", exportDto.filename());
        headers.setContentLength(exportDto.content().length);
        
        return new ResponseEntity<>(exportDto.content(), headers, HttpStatus.OK);
    }
}
