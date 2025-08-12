package com.blog.export.factory;

import com.blog.export.enums.BusinessType;
import com.blog.export.strategy.DataFetchStrategy;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据获取工厂
 * 
 * @author haibara
 * @description 根据业务类型获取对应的数据获取策略
 * @since 2025/1/21
 */
@Slf4j
@Component
public class DataFetchFactory {
    
    @Resource
    private List<DataFetchStrategy<?>> dataFetchStrategies;
    
    private final Map<BusinessType, DataFetchStrategy<?>> strategyMap = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        log.info("开始初始化数据获取工厂");
        
        for (DataFetchStrategy<?> strategy : dataFetchStrategies) {
            BusinessType businessType = strategy.getBusinessType();
            strategyMap.put(businessType, strategy);
            log.info("注册数据获取策略: {} -> {}", businessType.getName(), strategy.getClass().getSimpleName());
        }
        
        log.info("数据获取工厂初始化完成，共注册 {} 个策略", strategyMap.size());
    }
    
    /**
     * 获取数据获取策略
     * 
     * @param businessType 业务类型
     * @return 数据获取策略
     * @throws IllegalArgumentException 当找不到对应策略时抛出异常
     */
    @SuppressWarnings("unchecked")
    public <T> DataFetchStrategy<T> getStrategy(BusinessType businessType) {
        DataFetchStrategy<?> strategy = strategyMap.get(businessType);
        
        if (strategy == null) {
            String errorMsg = String.format("不支持的业务类型: %s", businessType.getName());
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        log.debug("获取数据获取策略: {} -> {}", businessType.getName(), strategy.getClass().getSimpleName());
        return (DataFetchStrategy<T>) strategy;
    }
    
    /**
     * 检查是否支持指定的业务类型
     * 
     * @param businessType 业务类型
     * @return 是否支持
     */
    public boolean isSupported(BusinessType businessType) {
        return strategyMap.containsKey(businessType);
    }
    
    /**
     * 获取所有支持的业务类型
     * 
     * @return 业务类型列表
     */
    public List<BusinessType> getSupportedBusinessTypes() {
        return List.copyOf(strategyMap.keySet());
    }
    
    /**
     * 检查指定业务类型的权限
     * 
     * @param businessType 业务类型
     * @return 是否有权限
     */
    public boolean hasPermission(BusinessType businessType) {
        DataFetchStrategy<?> strategy = strategyMap.get(businessType);
        return strategy != null && strategy.hasPermission();
    }
}