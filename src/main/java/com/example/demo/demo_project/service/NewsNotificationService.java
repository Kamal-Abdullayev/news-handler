package com.example.demo.demo_project.service;

import com.example.demo.demo_project.constants.ProjectHelperConstant;
import com.example.demo.demo_project.dto.*;
import com.example.demo.demo_project.entity.News;
import com.example.demo.demo_project.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class NewsNotificationService {
    private final NewsRepository newsRepository;
    private final NewsAnalyticsService newsAnalyticsService;
    private final ProjectHelperConstant projectHelperConstant;

    // In-memory storage for notifications (in a real app, this would be a database)
    private final Map<String, List<NewsNotificationDto>> notificationStore = new ConcurrentHashMap<>();
    private final Map<String, NotificationSubscriptionDto> subscriptions = new ConcurrentHashMap<>();

    /**
     * Subscribe to news notifications
     */
    public void subscribeToNotifications(NotificationSubscriptionDto subscription) {
        log.info("New subscription for user: {} with preferences: {}", subscription.userId(), subscription.preferences());
        subscriptions.put(subscription.userId(), subscription);
    }

    /**
     * Unsubscribe from notifications
     */
    public void unsubscribeFromNotifications(String userId) {
        log.info("Unsubscribing user: {} from notifications", userId);
        subscriptions.remove(userId);
        notificationStore.remove(userId);
    }

    /**
     * Get notifications for a specific user
     */
    @Cacheable(cacheNames = "demo-cache", key = "'notifications_' + #userId")
    public List<NewsNotificationDto> getUserNotifications(String userId) {
        log.info("Getting notifications for user: {}", userId);
        return notificationStore.getOrDefault(userId, new ArrayList<>());
    }

    /**
     * Mark notification as read
     */
    @CacheEvict(cacheNames = "demo-cache", key = "'notifications_' + #userId")
    public void markNotificationAsRead(String userId, String notificationId) {
        log.info("Marking notification {} as read for user: {}", notificationId, userId);
        List<NewsNotificationDto> notifications = notificationStore.get(userId);
        if (notifications != null) {
            notifications.removeIf(notification -> notification.id().equals(notificationId));
        }
    }

    /**
     * Clear all notifications for a user
     */
    @CacheEvict(cacheNames = "demo-cache", key = "'notifications_' + #userId")
    public void clearUserNotifications(String userId) {
        log.info("Clearing all notifications for user: {}", userId);
        notificationStore.remove(userId);
    }

    /**
     * Check for breaking news and send notifications
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void checkForBreakingNews() {
        log.info("Checking for breaking news...");
        
        LocalDateTime oneHourAgo = LocalDateTime.now().minus(1, ChronoUnit.HOURS);
        List<News> recentNews = newsRepository.findNewsByDateRange(oneHourAgo, LocalDateTime.now());
        
        // Check for high-priority keywords that might indicate breaking news
        Set<String> breakingNewsKeywords = Set.of("breaking", "urgent", "alert", "crisis", "emergency", "disaster", "attack", "accident");
        
        List<News> breakingNews = recentNews.stream()
                .filter(news -> {
                    String text = getNewsText(news).toLowerCase();
                    return breakingNewsKeywords.stream().anyMatch(text::contains);
                })
                .collect(Collectors.toList());
        
        if (!breakingNews.isEmpty()) {
            log.info("Found {} breaking news items", breakingNews.size());
            sendBreakingNewsNotifications(breakingNews);
        }
    }

    /**
     * Check for trending topics and send notifications
     */
    @Scheduled(fixedRate = 1800000) // Every 30 minutes
    public void checkForTrendingTopics() {
        log.info("Checking for trending topics...");
        
        List<TrendingTopicDto> trendingTopics = newsAnalyticsService.getTrendingTopics(1); // Last 24 hours
        
        if (!trendingTopics.isEmpty()) {
            log.info("Found {} trending topics", trendingTopics.size());
            sendTrendingTopicNotifications(trendingTopics);
        }
    }

    /**
     * Check for sentiment alerts
     */
    @Scheduled(fixedRate = 600000) // Every 10 minutes
    public void checkForSentimentAlerts() {
        log.info("Checking for sentiment alerts...");
        
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minus(30, ChronoUnit.MINUTES);
        List<News> recentNews = newsRepository.findNewsByDateRange(thirtyMinutesAgo, LocalDateTime.now());
        
        List<News> negativeNews = recentNews.stream()
                .filter(news -> {
                    try {
                        NewsSentimentDto sentiment = newsAnalyticsService.analyzeNewsSentiment(news.getId());
                        return "NEGATIVE".equals(sentiment.sentiment()) && sentiment.sentimentScore() < -0.1;
                    } catch (Exception e) {
                        log.warn("Could not analyze sentiment for news {}: {}", news.getId(), e.getMessage());
                        return false;
                    }
                })
                .collect(Collectors.toList());
        
        if (!negativeNews.isEmpty()) {
            log.info("Found {} negative news items", negativeNews.size());
            sendSentimentAlerts(negativeNews);
        }
    }

    /**
     * Send breaking news notifications to subscribed users
     */
    private void sendBreakingNewsNotifications(List<News> breakingNews) {
        for (Map.Entry<String, NotificationSubscriptionDto> entry : subscriptions.entrySet()) {
            String userId = entry.getKey();
            NotificationSubscriptionDto subscription = entry.getValue();
            
            if (subscription.preferences().contains("BREAKING_NEWS")) {
                for (News news : breakingNews) {
                    NewsNotificationDto notification = new NewsNotificationDto(
                            UUID.randomUUID().toString(),
                            "BREAKING_NEWS",
                            "Breaking News: " + news.getTitle(),
                            news.getDescription(),
                            news.getId(),
                            LocalDateTime.now(),
                            false
                    );
                    
                    addNotificationToUser(userId, notification);
                }
            }
        }
    }

    /**
     * Send trending topic notifications
     */
    private void sendTrendingTopicNotifications(List<TrendingTopicDto> trendingTopics) {
        for (Map.Entry<String, NotificationSubscriptionDto> entry : subscriptions.entrySet()) {
            String userId = entry.getKey();
            NotificationSubscriptionDto subscription = entry.getValue();
            
            if (subscription.preferences().contains("TRENDING_TOPICS")) {
                // Send top 3 trending topics
                trendingTopics.stream()
                        .limit(3)
                        .forEach(topic -> {
                            NewsNotificationDto notification = new NewsNotificationDto(
                                    UUID.randomUUID().toString(),
                                    "TRENDING_TOPIC",
                                    "Trending Topic: " + topic.topic(),
                                    "This topic has been mentioned " + topic.frequency() + " times recently",
                                    null,
                                    LocalDateTime.now(),
                                    false
                            );
                            
                            addNotificationToUser(userId, notification);
                        });
            }
        }
    }

    /**
     * Send sentiment alerts
     */
    private void sendSentimentAlerts(List<News> negativeNews) {
        for (Map.Entry<String, NotificationSubscriptionDto> entry : subscriptions.entrySet()) {
            String userId = entry.getKey();
            NotificationSubscriptionDto subscription = entry.getValue();
            
            if (subscription.preferences().contains("SENTIMENT_ALERTS")) {
                for (News news : negativeNews) {
                    NewsNotificationDto notification = new NewsNotificationDto(
                            UUID.randomUUID().toString(),
                            "SENTIMENT_ALERT",
                            "Negative Sentiment Alert",
                            "News with negative sentiment: " + news.getTitle(),
                            news.getId(),
                            LocalDateTime.now(),
                            false
                    );
                    
                    addNotificationToUser(userId, notification);
                }
            }
        }
    }

    /**
     * Add notification to user's notification list
     */
    private void addNotificationToUser(String userId, NewsNotificationDto notification) {
        notificationStore.computeIfAbsent(userId, k -> new ArrayList<>()).add(notification);
        log.info("Added notification {} to user {}", notification.id(), userId);
    }

    /**
     * Get news text for analysis
     */
    private String getNewsText(News news) {
        return (news.getContent() != null ? news.getContent() : "") + 
               (news.getDescription() != null ? " " + news.getDescription() : "") +
               (news.getTitle() != null ? " " + news.getTitle() : "");
    }

    /**
     * Get notification statistics
     */
    public NotificationStatisticsDto getNotificationStatistics() {
        int totalSubscriptions = subscriptions.size();
        int totalNotifications = notificationStore.values().stream()
                .mapToInt(List::size)
                .sum();
        
        Map<String, Long> notificationTypeStats = notificationStore.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(
                        NewsNotificationDto::type,
                        Collectors.counting()
                ));
        
        return new NotificationStatisticsDto(
                totalSubscriptions,
                totalNotifications,
                notificationTypeStats,
                LocalDateTime.now()
        );
    }
}
