package com.blog.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author haibara
 * @description 弹窗位置枚举
 * @since 2025/8/7
 */
@Getter
@AllArgsConstructor
@Schema(description = "弹窗位置枚举")
public enum PopupPositionEnum {

    @Schema(description = "居中")
    CENTER("center", "居中"),

    @Schema(description = "顶部")
    TOP("top", "顶部"),

    @Schema(description = "底部")
    BOTTOM("bottom", "底部"),

    @Schema(description = "左侧")
    LEFT("left", "左侧"),

    @Schema(description = "右侧")
    RIGHT("right", "右侧");

    // 位置值
    private final String value;
    // 描述
    private final String desc;
}
