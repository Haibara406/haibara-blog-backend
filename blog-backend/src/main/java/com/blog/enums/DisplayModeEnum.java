package com.blog.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author haibara
 * @description 弹窗显示模式枚举
 * @since 2025/8/7
 */
@Getter
@AllArgsConstructor
@Schema(description = "弹窗显示模式枚举")
public enum DisplayModeEnum {

    @Schema(description = "每次刷新")
    EVERY_REFRESH(1, "每次刷新"),

    @Schema(description = "会话期间一次")
    ONCE_PER_SESSION(2, "会话期间一次"),

    @Schema(description = "每日一次")
    ONCE_PER_DAY(3, "每日一次"),

    @Schema(description = "永久一次")
    ONCE_FOREVER(4, "永久一次");

    // 显示模式值
    private final Integer value;
    // 描述
    private final String desc;
}
