package com.blog.export.strategy.data;

import com.blog.domain.vo.TagVO;
import com.blog.export.enums.BusinessType;
import com.blog.export.strategy.DataFetchStrategy;
import com.blog.service.TagService;
import com.blog.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 标签数据获取策略
 * 
 * @author haibara
 * @description 从标签服务获取标签列表数据
 * @since 2025/1/21
 */
@Component
public class TagDataFetchStrategy implements DataFetchStrategy<TagVO> {
    
    @Resource
    private TagService tagService;
    
    @Override
    public List<TagVO> fetchData() {
        // 调用TagService的listAllTag方法获取标签列表
        return tagService.listAllTag();
    }
    
    @Override
    public BusinessType getBusinessType() {
        return BusinessType.TAG;
    }
    
    @Override
    public Class<TagVO> getDataClass() {
        return TagVO.class;
    }
    
    @Override
    public boolean hasPermission() {
        // 检查是否有标签搜索权限
        return SecurityUtils.hasAnyAuthority("blog:tag:search");
    }
}