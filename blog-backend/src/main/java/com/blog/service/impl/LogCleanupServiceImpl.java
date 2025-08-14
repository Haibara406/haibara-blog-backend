package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.constants.ErrorConst;
import com.blog.constants.LogConst;
import com.blog.domain.entity.Log;
import com.blog.mapper.LogMapper;
import com.blog.mapper.LoginLogMapper;
import com.blog.service.LogCleanupService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 日志清理服务实现
 * 
 * @author haibara
 * @description 系统日志自动清理功能的具体实现
 * @since 2025/8/13
 */
@Slf4j
@Service
public class LogCleanupServiceImpl implements LogCleanupService {
    
    @Resource
    private LoginLogMapper loginLogMapper;
    
    @Resource
    private LogMapper logMapper;
    
    /**
     * 默认保留的登录日志数量
     */
    @Value("${log.cleanup.login-log.keep-count:5000}")
    private int defaultLoginLogKeepCount;
    
    /**
     * 默认保留的操作日志数量
     */
    @Value("${log.cleanup.operate-log.keep-count:8000}")
    private int defaultOperateLogKeepCount;
    
    /**
     * 受保护操作日志的总体上限
     */
    @Value("${log.cleanup.operate-log.protected-keep-count:20000}")
    private int protectedLogKeepCount;
    
    /**
     * 受保护的操作类型（不会被自动清理）
     * 基于LogConst中定义的重要操作类型，这些操作涉及系统安全、权限管理、数据修改等关键业务
     * 
     * 受保护的操作包括：
     * - 修改操作：重要的系统变更记录
     * - 删除操作：重要的数据删除记录
     * - 审核操作：重要的审核决策记录
     * - 授权操作：权限相关操作记录
     * - 重置密码：安全相关操作记录
     * - 确认邮件：安全相关操作记录
     * - 绑定操作：用户账户相关操作记录
     */
    private static final List<String> PROTECTED_OPERATIONS = Arrays.asList(
            LogConst.UPDATE,        // 修改 - 重要的系统变更
            LogConst.DELETE,        // 删除 - 重要的数据删除
            LogConst.APPROVE,       // 审核 - 重要的审核决策
            LogConst.GRANT,         // 授权 - 权限相关操作
            LogConst.RESET_PASSWORD,// 重置密码 - 安全相关操作
            LogConst.RESET_CONFIRM, // 确认邮件 - 安全相关操作
            LogConst.BINDING        // 绑定 - 用户账户相关操作
    );
    
    /**
     * 非保护的操作类型（可以被自动清理）
     * 这些操作通常是日常的、可重复的、对系统影响较小的操作
     * 
     * 非保护的操作包括：
     * - 新增操作：常规的数据新增
     * - 获取操作：查询类操作
     * - 搜索操作：搜索查询操作  
     * - 上传图片：文件上传操作
     * - 邮件发送：邮件通知操作
     * - 监控操作：系统监控记录
     * - 导出操作：数据导出操作
     * - 清理操作：系统维护操作
     */
    private static final List<String> NON_PROTECTED_OPERATIONS = Arrays.asList(
            LogConst.INSERT,        // 新增 - 常规的数据新增
            LogConst.GET,           // 获取 - 查询类操作
            LogConst.SEARCH,        // 搜索 - 搜索查询操作
            LogConst.UPLOAD_IMAGE,  // 上传图片 - 文件上传操作
            LogConst.EMAIL_SEND,    // 邮件发送 - 邮件通知操作
            LogConst.MONITOR,       // 监控 - 系统监控记录
            LogConst.EXPORT,        // 导出 - 数据导出操作
            LogConst.CLEANUP        // 清理 - 系统维护操作
    );
    
    /**
     * 服务初始化后验证日志类型分类
     */
    @PostConstruct
    public void init() {
        validateLogTypeClassification();
    }
    
    /**
     * 验证日志操作类型分类的完整性
     * 确保LogConst中所有定义的操作类型都被正确分类为保护或非保护
     */
    private void validateLogTypeClassification() {
        Set<String> allLogConstOperations = Set.of(
                LogConst.UPDATE, LogConst.INSERT, LogConst.DELETE, LogConst.APPROVE,
                LogConst.GET, LogConst.GRANT, LogConst.SEARCH, LogConst.UPLOAD_IMAGE,
                LogConst.EMAIL_SEND, LogConst.RESET_PASSWORD, LogConst.RESET_CONFIRM,
                LogConst.BINDING, LogConst.MONITOR, LogConst.EXPORT, LogConst.CLEANUP
        );
        
        Set<String> classifiedOperations = new HashSet<>();
        classifiedOperations.addAll(PROTECTED_OPERATIONS);
        classifiedOperations.addAll(NON_PROTECTED_OPERATIONS);
        
        // 检查是否有未分类的操作
        Set<String> unclassifiedOperations = new HashSet<>(allLogConstOperations);
        unclassifiedOperations.removeAll(classifiedOperations);
        
        if (!unclassifiedOperations.isEmpty()) {
            log.warn("发现未分类的日志操作类型: {}", unclassifiedOperations);
        }
        
        // 检查是否有重复分类的操作
        Set<String> duplicateOperations = new HashSet<>();
        for (String operation : PROTECTED_OPERATIONS) {
            if (NON_PROTECTED_OPERATIONS.contains(operation)) {
                duplicateOperations.add(operation);
            }
        }
        
        if (!duplicateOperations.isEmpty()) {
            log.error("发现重复分类的日志操作类型: {}", duplicateOperations);
        }
        
        log.info("日志操作类型分类验证完成 - 受保护操作: {}, 非保护操作: {}, 总计: {}", 
                PROTECTED_OPERATIONS.size(), NON_PROTECTED_OPERATIONS.size(), 
                allLogConstOperations.size());
    }
    
    /**
     * 清理登录日志
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanupLoginLogs(int keepCount) {
        try {
            log.info("开始清理登录日志，保留最新 {} 条记录", keepCount);
            
            // 1. 查询当前登录日志总数
            Long totalCount = loginLogMapper.selectCount(null);
            if (totalCount <= keepCount) {
                log.info("当前登录日志数量 {} 未超过保留数量 {}，无需清理", totalCount, keepCount);
                return 0;
            }
            
            // 2. 计算需要删除的数量
            long deleteCount = totalCount - keepCount;
            log.info("当前登录日志总数 {}，需要删除最旧的 {} 条记录", totalCount, deleteCount);
            
            // 3. 查询需要删除的记录ID（最旧的记录）
            List<Long> idsToDelete = loginLogMapper.selectOldestIds(deleteCount);
            
            if (idsToDelete.isEmpty()) {
                log.warn("未找到需要删除的登录日志记录");
                return 0;
            }
            
            // 4. 执行批量删除
            int deletedCount = loginLogMapper.deleteBatchIds(idsToDelete);
            log.info("成功删除 {} 条登录日志记录", deletedCount);
            
            return deletedCount;
            
        } catch (Exception e) {
            log.error(ErrorConst.LOGIN_LOG_CLEANUP_ERROR, e);
            throw new RuntimeException(ErrorConst.LOGIN_LOG_CLEANUP_FAILED + e.getMessage());
        }
    }
    
    /**
     * 清理操作日志
     * 实现双重限制策略：
     * 1. 先清理普通操作日志到指定数量
     * 2. 如果总日志数仍超过受保护日志上限，则清理最旧的受保护日志
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanupOperateLogs(int keepCount) {
        try {
            log.info("开始清理操作日志，普通日志保留 {} 条，受保护日志上限 {} 条", keepCount, protectedLogKeepCount);
            
            int totalDeletedCount = 0;
            
            // 第一阶段：清理普通操作日志
            totalDeletedCount += cleanupNormalOperateLogs(keepCount);
            
            // 第二阶段：检查并清理超出上限的受保护日志
            totalDeletedCount += cleanupExcessProtectedLogs();
            
            log.info("操作日志清理完成，总共删除 {} 条记录", totalDeletedCount);
            return totalDeletedCount;
            
        } catch (Exception e) {
            log.error(ErrorConst.OPERATE_LOG_CLEANUP_ERROR, e);
            throw new RuntimeException(ErrorConst.OPERATE_LOG_CLEANUP_FAILED + e.getMessage());
        }
    }
    
    /**
     * 清理普通操作日志（非受保护）
     */
    private int cleanupNormalOperateLogs(int keepCount) {
        // 1. 查询非保护操作的日志总数
        LambdaQueryWrapper<Log> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.notIn(Log::getOperation, PROTECTED_OPERATIONS);
        Long normalLogCount = logMapper.selectCount(countWrapper);
        
        if (normalLogCount <= keepCount) {
            log.info("当前普通操作日志数量 {} 未超过保留数量 {}，无需清理", normalLogCount, keepCount);
            return 0;
        }
        
        // 2. 计算需要删除的数量
        long deleteCount = normalLogCount - keepCount;
        log.info("当前普通操作日志总数 {}，需要删除最旧的 {} 条记录", normalLogCount, deleteCount);
        
        // 3. 查询需要删除的记录ID（最旧的非保护记录）
        List<Long> idsToDelete = logMapper.selectOldestNonProtectedIds(PROTECTED_OPERATIONS, deleteCount);
        
        if (idsToDelete.isEmpty()) {
            log.warn("未找到需要删除的普通操作日志记录");
            return 0;
        }
        
        // 4. 执行批量删除
        int deletedCount = logMapper.deleteBatchIds(idsToDelete);
        log.info("成功删除 {} 条普通操作日志记录", deletedCount);
        
        return deletedCount;
    }
    
    /**
     * 清理超出上限的受保护日志
     */
    private int cleanupExcessProtectedLogs() {
        // 1. 查询受保护操作的日志总数
        LambdaQueryWrapper<Log> protectedWrapper = new LambdaQueryWrapper<>();
        protectedWrapper.in(Log::getOperation, PROTECTED_OPERATIONS);
        Long protectedLogCount = logMapper.selectCount(protectedWrapper);
        
        if (protectedLogCount <= protectedLogKeepCount) {
            log.info("当前受保护日志数量 {} 未超过上限 {}，无需清理", protectedLogCount, protectedLogKeepCount);
            return 0;
        }
        
        // 2. 计算需要删除的受保护日志数量
        long deleteCount = protectedLogCount - protectedLogKeepCount;
        log.info("当前受保护日志总数 {}，超过上限 {}，需要删除最旧的 {} 条记录", 
                protectedLogCount, protectedLogKeepCount, deleteCount);
        
        // 3. 查询需要删除的受保护记录ID
        List<Long> idsToDelete = logMapper.selectOldestProtectedIds(PROTECTED_OPERATIONS, deleteCount);
        
        if (idsToDelete.isEmpty()) {
            log.warn("未找到需要删除的受保护操作日志记录");
            return 0;
        }
        
        // 4. 执行批量删除
        int deletedCount = logMapper.deleteBatchIds(idsToDelete);
        log.info("成功删除 {} 条受保护操作日志记录", deletedCount);
        
        return deletedCount;
    }
    
    /**
     * 执行完整的日志清理
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String performFullCleanup() {
        try {
            log.info("========== 开始执行自动日志清理任务 ==========");
            log.info("使用基于LogConst的日志操作类型分类策略");
            log.info("受保护操作类型: {}", PROTECTED_OPERATIONS);
            log.info("非保护操作类型: {}", NON_PROTECTED_OPERATIONS);
            
            String startTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            // 记录清理前的统计信息
            String beforeStats = getLogStatistics();
            log.info("清理前统计: {}", beforeStats);
            
            // 清理登录日志
            int deletedLoginLogs = cleanupLoginLogs(defaultLoginLogKeepCount);
            
            // 清理操作日志
            int deletedOperateLogs = cleanupOperateLogs(defaultOperateLogKeepCount);
            
            // 记录清理后的统计信息
            String afterStats = getLogStatistics();
            log.info("清理后统计: {}", afterStats);
            
            String result = String.format(
                "日志自动清理完成 [%s] - 删除登录日志: %d条, 删除操作日志: %d条 (基于LogConst分类策略)",
                startTime, deletedLoginLogs, deletedOperateLogs
            );
            
            log.info("========== 日志清理任务执行完成 ==========");
            log.info("清理结果: {}", result);
            
            return result;
            
        } catch (Exception e) {
            String errorMsg = String.format(ErrorConst.LOG_CLEANUP_TASK_FAILED + "%s", e.getMessage());
            log.error(errorMsg, e);
            return errorMsg;
        }
    }
    
    /**
     * 获取日志统计信息
     */
    @Override
    public String getLogStatistics() {
        try {
            // 登录日志统计
            Long loginLogCount = loginLogMapper.selectCount(null);
            
            // 操作日志统计
            Long operateLogCount = logMapper.selectCount(null);
            
            // 受保护的操作日志统计
            LambdaQueryWrapper<Log> protectedWrapper = new LambdaQueryWrapper<>();
            protectedWrapper.in(Log::getOperation, PROTECTED_OPERATIONS);
            Long protectedLogCount = logMapper.selectCount(protectedWrapper);
            
            return String.format(
                "登录日志: %d条, 操作日志: %d条 (其中受保护: %d条)",
                loginLogCount, operateLogCount, protectedLogCount
            );
            
        } catch (Exception e) {
            log.error(ErrorConst.GET_LOG_STATISTICS_FAILED, e);
            return ErrorConst.LOG_STATISTICS_GET_FAILED + e.getMessage();
        }
    }
}