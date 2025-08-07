package com.blog.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author haibara
 * @description 弹窗显示位置枚举
 * @since 2025/8/7
 */
@Getter
@AllArgsConstructor
@Schema(description = "弹窗显示位置枚举")
public enum DisplayPositionEnum {

    @Schema(description = "前台")
    FRONTEND(1, "前台"),

    @Schema(description = "管理端")
    BACKEND(2, "管理端");

    // 位置值
    private final Integer value;
    // 描述
    private final String desc;
}
