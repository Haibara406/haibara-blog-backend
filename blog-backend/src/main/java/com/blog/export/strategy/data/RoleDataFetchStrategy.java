package com.blog.export.strategy.data;

import com.blog.domain.vo.RoleAllVO;
import com.blog.export.enums.BusinessType;
import com.blog.export.strategy.DataFetchStrategy;
import com.blog.service.RoleService;
import com.blog.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 角色数据获取策略
 * 
 * @author haibara
 * @description 从角色服务获取角色列表数据
 * @since 2025/1/21
 */
@Component
public class RoleDataFetchStrategy implements DataFetchStrategy<RoleAllVO> {
    
    @Resource
    private RoleService roleService;
    
    @Override
    public List<RoleAllVO> fetchData() {
        // 调用RoleService的selectRole方法获取角色列表
        // 传入null参数表示获取所有角色（相当于RoleController.selectAllRole()的逻辑）
        return roleService.selectRole(null);
    }
    
    @Override
    public BusinessType getBusinessType() {
        return BusinessType.ROLE;
    }
    
    @Override
    public Class<RoleAllVO> getDataClass() {
        return RoleAllVO.class;
    }
    
    @Override
    public boolean hasPermission() {
        // 检查是否有角色列表查看权限（与RoleController.selectAllRole()相同的权限）
        return SecurityUtils.hasAnyAuthority("system:role:list");
    }
}