package com.blog.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author haibara
 * @description 弹窗目标用户枚举
 * @since 2025/8/7
 */
@Getter
@AllArgsConstructor
@Schema(description = "弹窗目标用户枚举")
public enum TargetUsersEnum {

    @Schema(description = "所有用户")
    ALL_USERS(1, "所有用户"),

    @Schema(description = "登录用户")
    LOGGED_USERS(2, "登录用户"),

    @Schema(description = "游客")
    GUEST_USERS(3, "游客");

    // 用户类型值
    private final Integer value;
    // 描述
    private final String desc;
}
