package com.blog.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author haibara
 * @description 弹窗是否可关闭枚举
 * @since 2025/8/7
 */
@Getter
@AllArgsConstructor
@Schema(description = "弹窗是否可关闭枚举")
public enum CloseableEnum {

    @Schema(description = "不可关闭")
    NOT_CLOSEABLE(0, "不可关闭"),

    @Schema(description = "可关闭")
    CLOSEABLE(1, "可关闭");

    // 是否可关闭值
    private final Integer value;
    // 描述
    private final String desc;
}
