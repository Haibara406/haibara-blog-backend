package com.blog.export.strategy.data;

import com.blog.domain.vo.CategoryVO;
import com.blog.export.enums.BusinessType;
import com.blog.export.strategy.DataFetchStrategy;
import com.blog.service.CategoryService;
import com.blog.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 分类数据获取策略
 * 
 * @author haibara
 * @description 从分类服务获取分类列表数据
 * @since 2025/1/21
 */
@Component
public class CategoryDataFetchStrategy implements DataFetchStrategy<CategoryVO> {
    
    @Resource
    private CategoryService categoryService;
    
    @Override
    public List<CategoryVO> fetchData() {
        // 调用CategoryService的listAllCategory方法获取分类列表
        // 这相当于CategoryController.listAllCategory()的逻辑
        return categoryService.listAllCategory();
    }
    
    @Override
    public BusinessType getBusinessType() {
        return BusinessType.CATEGORY;
    }
    
    @Override
    public Class<CategoryVO> getDataClass() {
        return CategoryVO.class;
    }
    
    @Override
    public boolean hasPermission() {
        // 检查是否有分类搜索权限（分类管理的权限）
        return SecurityUtils.hasAnyAuthority("blog:category:search");
    }
}