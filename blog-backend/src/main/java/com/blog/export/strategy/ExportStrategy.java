package com.blog.export.strategy;

import com.blog.export.dto.ExportResult;
import com.blog.export.enums.BusinessType;
import com.blog.export.enums.ExportType;

import java.util.List;

/**
 * 导出策略接口
 * 
 * @author haibara
 * @description 定义导出操作的策略接口，支持策略模式
 * @since 2025/1/21
 */
public interface ExportStrategy<T> {
    
    /**
     * 导出数据
     * 
     * @param data 要导出的数据列表
     * @param fileName 文件名（不包含扩展名）
     * @return 导出结果
     */
    ExportResult export(List<T> data, String fileName);
    
    /**
     * 获取导出类型
     * 
     * @return 导出类型枚举
     */
    ExportType getExportType();
    
    /**
     * 获取支持的业务类型
     * 
     * @return 业务类型枚举
     */
    BusinessType getBusinessType();
    
    /**
     * 获取策略的唯一标识
     * 默认实现：业务类型_导出类型
     * 
     * @return 策略标识
     */
    default String getStrategyKey() {
        return getBusinessType().name() + "_" + getExportType().name();
    }
}