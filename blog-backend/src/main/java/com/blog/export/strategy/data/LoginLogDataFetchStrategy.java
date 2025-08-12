package com.blog.export.strategy.data;

import com.blog.domain.vo.LoginLogVO;
import com.blog.export.enums.BusinessType;
import com.blog.export.strategy.DataFetchStrategy;
import com.blog.service.LoginLogService;
import com.blog.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 登录日志数据获取策略
 * 
 * @author haibara
 * @description 从登录日志服务获取登录日志列表数据
 * @since 2025/1/21
 */
@Component
public class LoginLogDataFetchStrategy implements DataFetchStrategy<LoginLogVO> {
    
    @Resource
    private LoginLogService loginLogService;
    
    @Override
    public List<LoginLogVO> fetchData() {
        // 调用LoginLogService的searchLoginLog方法获取登录日志列表
        return loginLogService.searchLoginLog(null);
    }
    
    @Override
    public BusinessType getBusinessType() {
        return BusinessType.LOGIN_LOG;
    }
    
    @Override
    public Class<LoginLogVO> getDataClass() {
        return LoginLogVO.class;
    }
    
    @Override
    public boolean hasPermission() {
        // 检查是否有登录日志查看权限
        return SecurityUtils.hasAnyAuthority("system:log:login:list");
    }
}