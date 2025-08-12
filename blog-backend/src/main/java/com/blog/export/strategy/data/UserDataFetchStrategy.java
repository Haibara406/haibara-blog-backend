package com.blog.export.strategy.data;

import com.blog.domain.vo.UserListVO;
import com.blog.export.enums.BusinessType;
import com.blog.export.strategy.DataFetchStrategy;
import com.blog.service.UserService;
import com.blog.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户数据获取策略
 * 
 * @author haibara
 * @description 从用户服务获取用户列表数据
 * @since 2025/1/21
 */
@Component
public class UserDataFetchStrategy implements DataFetchStrategy<UserListVO> {
    
    @Resource
    private UserService userService;
    
    @Override
    public List<UserListVO> fetchData() {

        return userService.getUserOrSearch(null);
    }
    
    @Override
    public BusinessType getBusinessType() {
        return BusinessType.USER;
    }
    
    @Override
    public Class<UserListVO> getDataClass() {
        return UserListVO.class;
    }
    
    @Override
    public boolean hasPermission() {
        // 检查是否有用户列表查看权限（与UserController.getUserList()相同的权限）
        return SecurityUtils.hasAnyAuthority("system:user:list");
    }
}