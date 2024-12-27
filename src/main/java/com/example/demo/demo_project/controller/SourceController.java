package com.example.demo.demo_project.controller;

import com.example.demo.demo_project.dto.SourceDto;
import com.example.demo.demo_project.dto.SourceRequestDto;
import com.example.demo.demo_project.service.SourceService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/source")
@RestController
@RateLimiter(name = "demo-project")
public class SourceController {
    private final SourceService sourceService;


    @GetMapping("/all")
    public ResponseEntity<List<SourceDto>> getAllSources() {
        return new ResponseEntity<>(sourceService.getAllSources(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SourceDto> getSourceBySourceId(@PathVariable("id") Long id) {
        return new ResponseEntity<>(sourceService.getSourceBySourceId(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<HttpStatus> saveSource(@Valid @RequestBody SourceRequestDto sourceDto) {
        sourceService.saveSource(sourceDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/all")
    public ResponseEntity<HttpStatus> saveSource(@Valid @RequestBody List<SourceRequestDto> sourceDtoList) {
        sourceService.saveAllSources(sourceDtoList);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SourceDto> updateSource(@Valid @PathVariable("id") Long id, @RequestBody SourceRequestDto sourceDto) {
        return new ResponseEntity<>(sourceService.updateSourceById(id, sourceDto), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteSource(@PathVariable("id") Long id) {
        sourceService.deleteSourceById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}

