package com.blog.export.service;

import com.blog.export.dto.ExportResult;
import com.blog.export.enums.BusinessType;
import com.blog.export.enums.ExportType;
import com.blog.export.factory.DataFetchFactory;
import com.blog.export.factory.ExportStrategyFactory;
import com.blog.export.strategy.DataFetchStrategy;
import com.blog.export.strategy.ExportStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 导出服务
 * 
 * @author haibara
 * @description 提供统一的数据导出服务，整合数据获取和导出策略
 * @since 2025/1/21
 */
@Slf4j
@Service
public class ExportService {
    
    @Resource
    private ExportStrategyFactory exportStrategyFactory;
    
    @Resource
    private DataFetchFactory dataFetchFactory;
    
    /**
     * 导出数据
     * 
     * @param businessType 业务类型
     * @param exportType 导出类型
     * @param fileName 文件名（可为空，会使用默认名称）
     * @return 导出结果
     */
    public ExportResult export(BusinessType businessType, ExportType exportType, String fileName) {
        try {
            log.info("开始导出数据: 业务类型={}, 导出类型={}, 文件名={}", 
                    businessType.getName(), exportType.getName(), fileName);
            
            // 1. 检查是否支持该导出类型组合
            if (!exportStrategyFactory.isSupported(businessType, exportType)) {
                String errorMsg = String.format("不支持的导出类型组合: %s - %s", 
                        businessType.getName(), exportType.getName());
                log.error(errorMsg);
                return ExportResult.failure(errorMsg);
            }
            
            // 2. 获取数据获取策略并检查权限
            DataFetchStrategy<?> dataStrategy = dataFetchFactory.getStrategy(businessType);
            if (!dataStrategy.hasPermission()) {
                String errorMsg = String.format("没有权限导出 %s 数据", businessType.getName());
                log.error(errorMsg);
                return ExportResult.failure(errorMsg);
            }
            
            // 3. 获取数据
            List<?> data = dataStrategy.fetchData();
            if (data == null) {
                log.warn("获取到的数据为null: {}", businessType.getName());
                return ExportResult.failure("获取数据失败");
            }
            
            log.info("成功获取数据，条数: {}", data.size());
            
            // 4. 获取导出策略
            ExportStrategy<?> exportStrategy = exportStrategyFactory.getStrategy(businessType, exportType);
            
            // 5. 执行导出
            String finalFileName = fileName != null && !fileName.trim().isEmpty() ? 
                    fileName.trim() : businessType.getName();
            
            @SuppressWarnings({"unchecked", "rawtypes"})
            ExportResult result = ((ExportStrategy) exportStrategy).export(data, finalFileName);
            
            if (result.isSuccess()) {
                log.info("导出成功: 业务类型={}, 导出类型={}, 文件名={}, 文件大小={}字节, 数据条数={}", 
                        businessType.getName(), exportType.getName(), result.getFileName(), 
                        result.getFileSize(), result.getRecordCount());
            } else {
                log.error("导出失败: 业务类型={}, 导出类型={}, 错误信息={}", 
                        businessType.getName(), exportType.getName(), result.getErrorMessage());
            }
            
            return result;
            
        } catch (IllegalArgumentException e) {
            log.error("导出参数错误: {}", e.getMessage());
            return ExportResult.failure("导出参数错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("导出过程中发生异常", e);
            return ExportResult.failure("导出失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查是否支持指定的导出类型组合
     * 
     * @param businessType 业务类型
     * @param exportType 导出类型
     * @return 是否支持
     */
    public boolean isSupported(BusinessType businessType, ExportType exportType) {
        return dataFetchFactory.isSupported(businessType) && 
               exportStrategyFactory.isSupported(businessType, exportType);
    }
    
    /**
     * 检查是否有权限导出指定业务类型的数据
     * 
     * @param businessType 业务类型
     * @return 是否有权限
     */
    public boolean hasPermission(BusinessType businessType) {
        return dataFetchFactory.hasPermission(businessType);
    }
    
    /**
     * 获取指定业务类型支持的导出类型
     * 
     * @param businessType 业务类型
     * @return 支持的导出类型列表
     */
    public List<ExportType> getSupportedExportTypes(BusinessType businessType) {
        if (!dataFetchFactory.isSupported(businessType)) {
            return List.of();
        }
        return exportStrategyFactory.getSupportedExportTypes(businessType);
    }
    
    /**
     * 获取所有支持的业务类型
     * 
     * @return 业务类型列表
     */
    public List<BusinessType> getSupportedBusinessTypes() {
        return dataFetchFactory.getSupportedBusinessTypes();
    }
}