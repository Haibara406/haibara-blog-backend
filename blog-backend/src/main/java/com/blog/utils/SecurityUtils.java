package com.blog.utils;

import com.blog.domain.entity.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author haibara
 * @description Security获取相关信息工具类
 * @since 2025/7/27 16:21
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户id
     * @return 用户id
     */
    public static Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser user) {
            return user.getUser().getId();
        }
        return 0L; // 如果无法获取用户ID，返回默认值
    }

    /**
     * 判断用户是否登录
     * @return 是否登录
     */
    public static boolean isLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof LoginUser;
    }

    /**
     * 获取当前登录用户的角色
     * @return 角色列表
     */
    public static List<String> getUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList(); // 如果无法获取用户角色，返回空列表
    }

    /**
     * 检查用户是否拥有指定权限
     * @param authority 权限字符串
     * @return 是否拥有权限
     */
    public static boolean hasAuthority(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
    }
    
    /**
     * 检查用户是否拥有任意一个指定权限
     * @param authorities 权限字符串数组
     * @return 是否拥有任意一个权限
     */
    public static boolean hasAnyAuthority(String... authorities) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        List<String> userAuthorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        
        for (String authority : authorities) {
            if (userAuthorities.contains(authority)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取request
     * @return request
     */
    public static HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            return requestAttributes.getRequest();
        }
        return null;
    }

}