package com.blog.export.enums;

import com.blog.domain.vo.*;
import lombok.Getter;

/**
 * 业务类型枚举
 * 
 * @author haibara
 * @description 定义支持导出的业务类型
 * @since 2025/1/21
 */
@Getter
public enum BusinessType {
    USER("user", "用户管理", UserListVO.class),
    ROLE("role", "角色管理", RoleAllVO.class),
    CATEGORY("category", "分类管理", CategoryVO.class),
    TAG("tag", "标签管理", TagVO.class),
    COMMENT("comment", "评论管理", CommentListVO.class),
    BLACK_LIST("blackList", "黑名单管理", BlackListVO.class),
    LOGIN_LOG("loginLog", "登录日志", LoginLogVO.class),
    OPERATE_LOG("operateLog", "操作日志", LogVO.class);
    
    private final String code;
    private final String name;
    private final Class<?> voClass;
    
    BusinessType(String code, String name, Class<?> voClass) {
        this.code = code;
        this.name = name;
        this.voClass = voClass;
    }
    
    /**
     * 根据代码获取业务类型
     * @param code 业务代码
     * @return 业务类型
     */
    public static BusinessType fromCode(String code) {
        for (BusinessType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("不支持的业务类型: " + code);
    }
}