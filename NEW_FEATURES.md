# New Features Added to News Handler Project

This document describes the exciting new features that have been added to enhance the News Handler application.

## 🚀 New Services Added

### 1. News Analytics Service (`NewsAnalyticsService`)

**Location**: `src/main/java/com/example/demo/demo_project/service/NewsAnalyticsService.java`

**Features**:
- **Sentiment Analysis**: Analyzes news content sentiment using keyword-based approach
- **Trending Topics**: Identifies trending topics based on news frequency
- **News Recommendations**: Suggests related news based on content similarity
- **Advanced Search**: Multi-filter search with sorting capabilities
- **Statistics Dashboard**: Comprehensive news analytics and insights

**Key Methods**:
- `analyzeNewsSentiment(Long newsId)` - Analyze sentiment of specific news
- `getTrendingTopics(int days)` - Get trending topics for specified period
- `getNewsRecommendations(Long newsId, int limit)` - Get related news recommendations
- `getNewsStatistics()` - Get comprehensive news statistics
- `advancedSearch(NewsAdvancedSearchDto)` - Advanced search with filters

### 2. News Notification Service (`NewsNotificationService`)

**Location**: `src/main/java/com/example/demo/demo_project/service/NewsNotificationService.java`

**Features**:
- **Real-time Notifications**: Automated notification system for news events
- **Breaking News Alerts**: Detects and notifies about breaking news
- **Trending Topic Notifications**: Alerts about trending topics
- **Sentiment Alerts**: Notifications for negative sentiment news
- **User Subscription Management**: Subscribe/unsubscribe to different notification types

**Key Methods**:
- `subscribeToNotifications(NotificationSubscriptionDto)` - Subscribe to notifications
- `getUserNotifications(String userId)` - Get user's notifications
- `markNotificationAsRead(String userId, String notificationId)` - Mark notification as read
- `checkForBreakingNews()` - Scheduled task to check for breaking news
- `checkForTrendingTopics()` - Scheduled task to check trending topics
- `checkForSentimentAlerts()` - Scheduled task to check sentiment alerts

### 3. News Export Service (`NewsExportService`)

**Location**: `src/main/java/com/example/demo/demo_project/service/NewsExportService.java`

**Features**:
- **JSON Export**: Export news data in JSON format
- **CSV Export**: Export news data in CSV format
- **Report Generation**: Generate comprehensive text reports
- **Statistics Export**: Export news statistics
- **Trending Topics Export**: Export trending topics analysis

**Key Methods**:
- `exportNewsAsJson(NewsExportRequestDto)` - Export news as JSON
- `exportNewsAsCsv(NewsExportRequestDto)` - Export news as CSV
- `generateNewsReport(NewsReportRequestDto)` - Generate comprehensive report
- `exportNewsStatistics()` - Export statistics
- `exportTrendingTopics(int days)` - Export trending topics

## 🎯 New Controllers Added

### 1. News Analytics Controller (`NewsAnalyticsController`)

**Location**: `src/main/java/com/example/demo/demo_project/controller/NewsAnalyticsController.java`

**Endpoints**:
- `GET /api/v1/analytics/sentiment/{newsId}` - Analyze news sentiment
- `GET /api/v1/analytics/trending?days={days}` - Get trending topics
- `GET /api/v1/analytics/recommendations/{newsId}?limit={limit}` - Get news recommendations
- `GET /api/v1/analytics/statistics` - Get news statistics
- `POST /api/v1/analytics/search` - Advanced search

### 2. News Notification Controller (`NewsNotificationController`)

**Location**: `src/main/java/com/example/demo/demo_project/controller/NewsNotificationController.java`

**Endpoints**:
- `POST /api/v1/notifications/subscribe` - Subscribe to notifications
- `DELETE /api/v1/notifications/unsubscribe/{userId}` - Unsubscribe from notifications
- `GET /api/v1/notifications/{userId}` - Get user notifications
- `PUT /api/v1/notifications/{userId}/read/{notificationId}` - Mark notification as read
- `DELETE /api/v1/notifications/{userId}/clear` - Clear all notifications
- `GET /api/v1/notifications/statistics` - Get notification statistics

### 3. News Export Controller (`NewsExportController`)

**Location**: `src/main/java/com/example/demo/demo_project/controller/NewsExportController.java`

**Endpoints**:
- `POST /api/v1/export/json` - Export news as JSON
- `POST /api/v1/export/csv` - Export news as CSV
- `POST /api/v1/export/report` - Generate news report
- `GET /api/v1/export/statistics` - Export statistics
- `GET /api/v1/export/trending-topics?days={days}` - Export trending topics

## 📊 New DTOs Added

### Analytics DTOs:
- `NewsSentimentDto` - Sentiment analysis results
- `TrendingTopicDto` - Trending topic information
- `NewsRecommendationDto` - News recommendation with similarity score
- `NewsStatisticsDto` - Comprehensive news statistics
- `NewsAdvancedSearchDto` - Advanced search criteria

### Notification DTOs:
- `NewsNotificationDto` - Notification information
- `NotificationSubscriptionDto` - Subscription preferences
- `NotificationStatisticsDto` - Notification statistics

### Export DTOs:
- `NewsExportDto` - Export result information
- `NewsExportRequestDto` - Export request criteria
- `NewsReportRequestDto` - Report generation criteria

## 🔧 Enhanced Repository

### NewsRepository Updates:
- Added `findNewsByDateRange(LocalDateTime startDate, LocalDateTime endDate)` method
- Added corresponding query constant `FIND_NEWS_BY_DATE_RANGE`

## 🎨 Key Features Explained

### 1. Sentiment Analysis
- Uses keyword-based approach to analyze news sentiment
- Identifies positive, negative, and neutral sentiment
- Extracts keywords for better analysis
- Provides sentiment score for quantitative analysis

### 2. Trending Topics
- Analyzes news frequency over time periods
- Calculates trend scores based on mention frequency
- Caches results for better performance
- Configurable time periods (default: 7 days)

### 3. News Recommendations
- Uses content similarity based on keyword extraction
- Calculates Jaccard similarity between news articles
- Filters recommendations by relevance threshold
- Provides similarity scores for ranking

### 4. Advanced Search
- Multiple filter criteria: keyword, author, source, date range, sentiment
- Configurable sorting: by title, author, source, date
- Sort order: ascending or descending
- Configurable result limits

### 5. Real-time Notifications
- Automated detection of breaking news using keyword analysis
- Trending topic notifications every 30 minutes
- Sentiment alerts for negative news every 10 minutes
- User subscription management with preferences

### 6. Data Export
- Multiple export formats: JSON, CSV, Text reports
- Configurable filtering and date ranges
- Comprehensive statistics export
- Trending topics export with analysis

## 🚀 Usage Examples

### 1. Analyze News Sentiment
```bash
GET /api/v1/analytics/sentiment/123
```

### 2. Get Trending Topics
```bash
GET /api/v1/analytics/trending?days=7
```

### 3. Subscribe to Notifications
```bash
POST /api/v1/notifications/subscribe
{
  "userId": "user123",
  "email": "user@example.com",
  "preferences": ["BREAKING_NEWS", "TRENDING_TOPICS", "SENTIMENT_ALERTS"]
}
```

### 4. Advanced Search
```bash
POST /api/v1/analytics/search
{
  "keyword": "technology",
  "author": "John Doe",
  "source": "TechCrunch",
  "startDate": "2024-01-01T00:00:00",
  "endDate": "2024-12-31T23:59:59",
  "sentiment": "POSITIVE",
  "sortBy": "date",
  "sortOrder": "desc",
  "limit": 20
}
```

### 5. Export News as CSV
```bash
POST /api/v1/export/csv
{
  "startDate": "2024-01-01T00:00:00",
  "endDate": "2024-12-31T23:59:59",
  "source": "TechCrunch",
  "author": "John Doe",
  "limit": 100
}
```

## 🔄 Scheduled Tasks

The new services include several scheduled tasks:

1. **Breaking News Check** (Every 5 minutes)
   - Scans recent news for breaking news keywords
   - Sends notifications to subscribed users

2. **Trending Topics Check** (Every 30 minutes)
   - Analyzes trending topics from the last 24 hours
   - Sends notifications about top trending topics

3. **Sentiment Alerts** (Every 10 minutes)
   - Checks for negative sentiment news
   - Sends alerts to users subscribed to sentiment notifications

## 🎯 Benefits

1. **Enhanced User Experience**: Advanced search, recommendations, and notifications
2. **Data Insights**: Sentiment analysis and trending topics
3. **Real-time Monitoring**: Automated alerts and notifications
4. **Data Export**: Multiple formats for data analysis
5. **Scalable Architecture**: Caching and efficient algorithms
6. **Comprehensive Analytics**: Statistics and reporting capabilities

## 🔧 Configuration

All new features use the existing configuration system and follow the same patterns as the original codebase. The services are automatically discovered by Spring Boot and integrated with the existing caching and monitoring infrastructure.

## 🧪 Testing

The new features can be tested using the existing Postman collection or by adding new test cases to the existing test suite. All endpoints follow RESTful conventions and include proper error handling and validation.
