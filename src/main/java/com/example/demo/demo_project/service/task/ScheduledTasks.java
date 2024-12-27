package com.example.demo.demo_project.service.task;

import com.example.demo.demo_project.constants.SchedulerConfigConstant;
import com.example.demo.demo_project.service.NewsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@AllArgsConstructor
@Component
@EnableScheduling
@CacheConfig(cacheNames = {"demo-cache"})
public class ScheduledTasks {
    private final NewsService newsService;
    private final SchedulerConfigConstant schedulerConfigConstant;

    private static int currentPage = 1;
    private static final int endPage = 5;

    @Scheduled(fixedRate = 10000)
    public void readDataFromExternalSource() {
        log.info("ScheduledTasks read data from external source starting...");
        if (currentPage < endPage) {
            newsService.getNewsFromExternalApi(
                    "q=" + schedulerConfigConstant.getKeyword() +
                            "&pageSize=" + schedulerConfigConstant.getPageSize() +
                            "&page=" + currentPage);
            log.info("ScheduledTasks read data from external source finished. Page number: {}", currentPage);
            currentPage++;
        }

    }

    @CacheEvict(allEntries = true)
    @PostConstruct
    @Scheduled(fixedRate = 10000)
    public void clearCache(){
        log.info("Caches are cleared...");
    }
}
