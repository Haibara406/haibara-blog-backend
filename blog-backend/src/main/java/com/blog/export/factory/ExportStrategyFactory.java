package com.blog.export.factory;

import com.blog.export.enums.BusinessType;
import com.blog.export.enums.ExportType;
import com.blog.export.strategy.ExportStrategy;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 导出策略工厂
 * 
 * @author haibara
 * @description 根据业务类型和导出类型获取对应的导出策略
 * @since 2025/1/21
 */
@Slf4j
@Component
public class ExportStrategyFactory {
    
    @Resource
    private List<ExportStrategy<?>> exportStrategies;
    
    private final Map<String, ExportStrategy<?>> strategyMap = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        log.info("开始初始化导出策略工厂");
        
        for (ExportStrategy<?> strategy : exportStrategies) {
            String key = strategy.getStrategyKey();
            strategyMap.put(key, strategy);
            log.info("注册导出策略: {} -> {}", key, strategy.getClass().getSimpleName());
        }
        
        log.info("导出策略工厂初始化完成，共注册 {} 个策略", strategyMap.size());
    }
    
    /**
     * 获取导出策略
     * 
     * @param businessType 业务类型
     * @param exportType 导出类型
     * @return 导出策略
     * @throws IllegalArgumentException 当找不到对应策略时抛出异常
     */
    @SuppressWarnings("unchecked")
    public <T> ExportStrategy<T> getStrategy(BusinessType businessType, ExportType exportType) {
        String key = businessType.name() + "_" + exportType.name();
        ExportStrategy<?> strategy = strategyMap.get(key);
        
        if (strategy == null) {
            String errorMsg = String.format("不支持的导出类型组合: %s - %s", businessType.getName(), exportType.getName());
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        log.debug("获取导出策略: {} -> {}", key, strategy.getClass().getSimpleName());
        return (ExportStrategy<T>) strategy;
    }
    
    /**
     * 检查是否支持指定的导出类型组合
     * 
     * @param businessType 业务类型
     * @param exportType 导出类型
     * @return 是否支持
     */
    public boolean isSupported(BusinessType businessType, ExportType exportType) {
        String key = businessType.name() + "_" + exportType.name();
        return strategyMap.containsKey(key);
    }
    
    /**
     * 获取所有已注册的策略键
     * 
     * @return 策略键集合
     */
    public java.util.Set<String> getAllStrategyKeys() {
        return strategyMap.keySet();
    }
    
    /**
     * 获取指定业务类型支持的导出类型
     * 
     * @param businessType 业务类型
     * @return 支持的导出类型列表
     */
    public List<ExportType> getSupportedExportTypes(BusinessType businessType) {
        return strategyMap.keySet().stream()
                .filter(key -> key.startsWith(businessType.name() + "_"))
                .map(key -> key.substring(key.lastIndexOf("_") + 1))
                .map(ExportType::valueOf)
                .toList();
    }
}