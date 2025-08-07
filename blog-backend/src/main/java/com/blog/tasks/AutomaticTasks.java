package com.blog.tasks;

import com.blog.service.RedisService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author haibara
 * @description 自动任务，初始化
 * @since 2025/8/1 14:16
 */

@Slf4j
@Component
public class AutomaticTasks implements ApplicationRunner {

    @Resource
    private RedisService redisService;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("--------开始执行初始化任务--------");
        redisService.articleCountClear();
        redisService.articleVisitCount();
        redisService.clearLimitCache();
        redisService.initBlackList();
        redisService.initCount();

        // 预热基础数据缓存
        redisService.preloadCategories();
        redisService.preloadTags();

        log.info("--------执行初始化任务结束--------");
    }
}
