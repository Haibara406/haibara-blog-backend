package com.blog.export.strategy.data;

import com.blog.domain.vo.BlackListVO;
import com.blog.export.enums.BusinessType;
import com.blog.export.strategy.DataFetchStrategy;
import com.blog.service.BlackListService;
import com.blog.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 黑名单数据获取策略
 * 
 * @author haibara
 * @description 从黑名单服务获取黑名单列表数据
 * @since 2025/1/21
 */
@Component
public class BlackListDataFetchStrategy implements DataFetchStrategy<BlackListVO> {
    
    @Resource
    private BlackListService blackListService;
    
    @Override
    public List<BlackListVO> fetchData() {
        // 调用BlackListService的getBlackList方法获取黑名单列表
        return blackListService.getBlackList(null);
    }
    
    @Override
    public BusinessType getBusinessType() {
        return BusinessType.BLACK_LIST;
    }
    
    @Override
    public Class<BlackListVO> getDataClass() {
        return BlackListVO.class;
    }
    
    @Override
    public boolean hasPermission() {
        // 检查是否有黑名单查看权限
        return SecurityUtils.hasAnyAuthority("blog:black:select");
    }
}