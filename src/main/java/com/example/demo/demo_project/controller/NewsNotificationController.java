package com.example.demo.demo_project.controller;

import com.example.demo.demo_project.dto.*;
import com.example.demo.demo_project.service.NewsNotificationService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@RestController
@RateLimiter(name = "demo-project")
@Slf4j
public class NewsNotificationController {
    private final NewsNotificationService newsNotificationService;

    @PostMapping("/subscribe")
    public ResponseEntity<HttpStatus> subscribeToNotifications(
            @Valid @RequestBody NotificationSubscriptionDto subscription) {
        log.info("New subscription request for user: {}", subscription.userId());
        newsNotificationService.subscribeToNotifications(subscription);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/unsubscribe/{userId}")
    public ResponseEntity<HttpStatus> unsubscribeFromNotifications(@PathVariable String userId) {
        log.info("Unsubscribe request for user: {}", userId);
        newsNotificationService.unsubscribeFromNotifications(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<NewsNotificationDto>> getUserNotifications(@PathVariable String userId) {
        log.info("Getting notifications for user: {}", userId);
        return new ResponseEntity<>(newsNotificationService.getUserNotifications(userId), HttpStatus.OK);
    }

    @PutMapping("/{userId}/read/{notificationId}")
    public ResponseEntity<HttpStatus> markNotificationAsRead(
            @PathVariable String userId,
            @PathVariable String notificationId) {
        log.info("Marking notification {} as read for user: {}", notificationId, userId);
        newsNotificationService.markNotificationAsRead(userId, notificationId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<HttpStatus> clearUserNotifications(@PathVariable String userId) {
        log.info("Clearing all notifications for user: {}", userId);
        newsNotificationService.clearUserNotifications(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/statistics")
    public ResponseEntity<NotificationStatisticsDto> getNotificationStatistics() {
        log.info("Getting notification statistics");
        return new ResponseEntity<>(newsNotificationService.getNotificationStatistics(), HttpStatus.OK);
    }
}
