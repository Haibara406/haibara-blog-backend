package com.blog.export.strategy;

import com.blog.export.enums.BusinessType;

import java.util.List;

/**
 * 数据获取策略接口
 * 
 * @author haibara
 * @description 定义数据获取操作的策略接口，用于从不同业务模块获取导出数据
 * @since 2025/1/21
 */
public interface DataFetchStrategy<T> {
    
    /**
     * 获取数据列表
     * 调用对应业务模块的list方法获取数据
     * 
     * @return 数据列表
     */
    List<T> fetchData();
    
    /**
     * 获取业务类型
     * 
     * @return 业务类型枚举
     */
    BusinessType getBusinessType();
    
    /**
     * 获取数据类型的Class对象
     * 
     * @return 数据类型Class
     */
    Class<T> getDataClass();
    
    /**
     * 验证当前用户是否有权限获取数据
     * 默认实现返回true，子类可以重写实现具体的权限检查
     * 
     * @return 是否有权限
     */
    default boolean hasPermission() {
        return true;
    }
}