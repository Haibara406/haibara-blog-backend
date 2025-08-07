package com.blog.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author haibara
 * @description 弹窗类型枚举
 * @since 2025/8/7
 */
@Getter
@AllArgsConstructor
@Schema(description = "弹窗类型枚举")
public enum PopupTypeEnum {

    @Schema(description = "通知")
    NOTIFICATION(1, "通知"),

    @Schema(description = "广告")
    ADVERTISEMENT(2, "广告"),

    @Schema(description = "公告")
    ANNOUNCEMENT(3, "公告"),

    @Schema(description = "活动")
    ACTIVITY(4, "活动");

    // 类型值
    private final Integer value;
    // 描述
    private final String desc;
}
