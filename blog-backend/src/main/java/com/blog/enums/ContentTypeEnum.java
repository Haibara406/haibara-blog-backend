package com.blog.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author haibara
 * @description 弹窗内容类型枚举
 * @since 2025/8/7
 */
@Getter
@AllArgsConstructor
@Schema(description = "弹窗内容类型枚举")
public enum ContentTypeEnum {

    @Schema(description = "Markdown")
    MARKDOWN(1, "Markdown"),

    @Schema(description = "HTML")
    HTML(2, "HTML"),

    @Schema(description = "纯文本")
    PLAIN_TEXT(3, "纯文本");

    // 内容类型值
    private final Integer value;
    // 描述
    private final String desc;
}
