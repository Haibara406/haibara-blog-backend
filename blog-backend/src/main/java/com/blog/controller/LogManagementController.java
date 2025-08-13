package com.blog.controller;

import com.blog.annotation.AccessLimit;
import com.blog.annotation.LogAnnotation;
import com.blog.constants.AccessLimitConst;

import com.blog.constants.ErrorConst;
import com.blog.domain.response.ResponseResult;
import com.blog.service.LogCleanupService;
import com.blog.utils.ControllerUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 日志管理控制器
 *
 * @author haibara
 * @description 提供日志清理和统计相关的管理接口
 * @since 2025/8/13
 */
@Slf4j
@RestController
@RequestMapping("/log-management")
@Tag(name = "日志管理相关接口")
@Validated
public class LogManagementController {

    @Resource
    private LogCleanupService logCleanupService;

    /**
     * 获取日志统计信息
     * <p>
     * 查询当前系统中各类日志的数量统计信息，包括：
     * - 登录日志总数
     * - 操作日志总数
     * - 受保护的操作日志数量
     * 该接口用于系统监控和容量规划。
     *
     * @return 响应结果，包含日志统计信息
     */
    @PreAuthorize("hasAnyAuthority('system:log:statistics')")
    @Operation(summary = "获取日志统计信息")
    @AccessLimit(seconds = AccessLimitConst.DEFAULT_SECONDS, 
                maxCount = AccessLimitConst.DEFAULT_MAX_COUNT)
    @GetMapping("/statistics")
    public ResponseResult<String> getLogStatistics() {
        return ControllerUtils.messageHandler(() -> logCleanupService.getLogStatistics());
    }

    /**
     * 手动执行日志清理
     * <p>
     * 立即执行一次完整的日志清理操作，清理规则：
     * - 登录日志：保留最新的5000条记录
     * - 操作日志：保留最新的8000条记录（保护重要操作）
     * 该操作通常在系统维护时手动执行，或者在日志量激增时紧急清理。
     * 操作完成后会记录操作日志。
     *
     * @return 响应结果，包含清理执行结果
     *         <ul>
     *             <li>成功时返回清理统计信息</li>
     *             <li>失败时返回错误信息</li>
     *         </ul>
     */
    @PreAuthorize("hasAnyAuthority('system:log:cleanup')")
    @Operation(summary = "手动执行日志清理")
    @LogAnnotation(module = "日志管理", operation = "清理")
    @AccessLimit(seconds = AccessLimitConst.DEFAULT_SECONDS,
            maxCount = AccessLimitConst.MANUAL_CLEAN_LOG_MAX_COUNT)
    @PostMapping("/cleanup")
    public ResponseResult<String> manualCleanup() {
        return ControllerUtils.messageHandler(() -> {
            log.info("管理员手动触发日志清理操作");
            return logCleanupService.performFullCleanup();
        });
    }

    /**
     * 手动清理登录日志
     * <p>
     * 单独清理登录日志，可以指定保留的记录数量。
     * 该接口提供更细粒度的控制，适用于特定场景的清理需求。
     * 操作完成后会记录操作日志。
     *
     * @param keepCount 保留的记录数量，必须大于0，建议不少于1000条
     * @return 响应结果，包含删除的记录数量
     */
    @PreAuthorize("hasAnyAuthority('system:log:cleanup:login')")
    @Operation(summary = "手动清理登录日志")
    @Parameter(name = "keepCount", description = "保留的记录数量", required = true)
    @LogAnnotation(module = "日志管理", operation = "清理")
    @AccessLimit(seconds = AccessLimitConst.DEFAULT_SECONDS,
            maxCount = AccessLimitConst.MANUAL_CLEAN_LOGIN_LOG_MAX_COUNT)
    @PostMapping("/cleanup/login-log")
    public ResponseResult<String> cleanupLoginLogs(@RequestParam("keepCount") int keepCount) {
        if (keepCount <= 0) {
            return ResponseResult.failure(ErrorConst.LOGIN_LOG_KEEP_COUNT_MUST_GT_ZERO);
        }
        if (keepCount < 1000) {
            return ResponseResult.failure(ErrorConst.LOGIN_LOG_KEEP_COUNT_SUGGEST_MIN_1000);
        }
        
        return ControllerUtils.messageHandler(() -> {
            log.info("管理员手动清理登录日志，保留数量: {}", keepCount);
            int deletedCount = logCleanupService.cleanupLoginLogs(keepCount);
            return String.format("登录日志清理完成，删除了 %d 条记录", deletedCount);
        });
    }

    /**
     * 手动清理操作日志
     * <p>
     * 单独清理操作日志，可以指定保留的记录数量。
     * 重要的系统操作记录将被保护，不会被清理。
     * 操作完成后会记录操作日志。
     *
     * @param keepCount 保留的记录数量，必须大于0，建议不少于2000条
     * @return 响应结果，包含删除的记录数量
     */
    @PreAuthorize("hasAnyAuthority('system:log:cleanup:operate')")
    @Operation(summary = "手动清理操作日志")
    @Parameter(name = "keepCount", description = "保留的记录数量", required = true)
    @LogAnnotation(module = "日志管理", operation = "清理")
    @AccessLimit(seconds = AccessLimitConst.DEFAULT_SECONDS,
            maxCount = AccessLimitConst.MANUAL_CLEAN_OPERATE_LOG_MAX_COUNT)
    @PostMapping("/cleanup/operate-log")
    public ResponseResult<String> cleanupOperateLogs(@RequestParam("keepCount") int keepCount) {
        if (keepCount <= 0) {
            return ResponseResult.failure(ErrorConst.OPERATE_LOG_KEEP_COUNT_MUST_GT_ZERO);
        }
        if (keepCount < 2000) {
            return ResponseResult.failure(ErrorConst.OPERATE_LOG_KEEP_COUNT_SUGGEST_MIN_2000);
        }
        
        return ControllerUtils.messageHandler(() -> {
            log.info("管理员手动清理操作日志，保留数量: {}", keepCount);
            int deletedCount = logCleanupService.cleanupOperateLogs(keepCount);
            return String.format("操作日志清理完成，删除了 %d 条记录（受保护的操作记录已保留）", deletedCount);
        });
    }
}