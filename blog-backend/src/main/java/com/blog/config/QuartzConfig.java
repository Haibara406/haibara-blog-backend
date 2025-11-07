package com.blog.config;

import com.blog.quartz.LogCleanupTask;
import com.blog.quartz.RefreshTheCache;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author haibara
 * @description Quartz配置类
 * @since 2025/7/27 16:36
 */
@Configuration
public class QuartzConfig {
    
    /**
     * 缓存刷新任务详情
     */
    @Bean
    public JobDetail cacheRefreshJobDetail() {
        //指定任务描述具体的实现类
        return JobBuilder.newJob(RefreshTheCache.class)
                // 指定任务的名称
                .withIdentity("refreshTheCache")
                // 任务描述
                .withDescription("任务描述：用于每五分钟刷新一次常用数据缓存")
                // 每次任务执行后进行存储
                .storeDurably()
                .build();
    }

    /**
     * 缓存刷新任务触发器
     */
    @Bean
    public Trigger cacheRefreshTrigger() {
        //创建触发器
        return TriggerBuilder.newTrigger()
                // 绑定工作任务
                .forJob(cacheRefreshJobDetail())
                // 触发器名称
                .withIdentity("cacheRefreshTrigger")
                // 每隔 5 分钟执行一次 job
                .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(60 * 5))
                .build();
    }
    
    /**
     * 日志清理任务详情
     */
    @Bean
    public JobDetail logCleanupJobDetail() {
        return JobBuilder.newJob(LogCleanupTask.class)
                // 指定任务的名称
                .withIdentity("logCleanupTask")
                // 任务描述
                .withDescription("任务描述：定期清理系统日志，保持数据库性能")
                // 每次任务执行后进行存储
                .storeDurably()
                .build();
    }
    
    /**
     * 日志清理任务触发器
     * 每天凌晨2点执行一次
     */
    @Bean
    public Trigger logCleanupTrigger() {
        return TriggerBuilder.newTrigger()
                // 绑定工作任务
                .forJob(logCleanupJobDetail())
                // 触发器名称
                .withIdentity("logCleanupTrigger")
                // 任务描述
                .withDescription("每天凌晨2点执行日志清理任务")
                // 使用Cron表达式：每天凌晨2点执行
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 2 * * ?"))
                .build();
    }
}
