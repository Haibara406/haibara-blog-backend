package com.blog.export.enums;

import lombok.Getter;

/**
 * 导出类型枚举
 * 
 * @author haibara
 * @description 定义支持的导出格式类型
 * @since 2025/1/21
 */
@Getter
public enum ExportType {
    HTML("html", "HTML格式", "text/html; charset=UTF-8", ".html"),
    EXCEL("excel", "Excel格式", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx");
    
    private final String code;
    private final String name;
    private final String contentType;
    private final String extension;
    
    ExportType(String code, String name, String contentType, String extension) {
        this.code = code;
        this.name = name;
        this.contentType = contentType;
        this.extension = extension;
    }
    
    /**
     * 根据代码获取导出类型
     * @param code 导出类型代码
     * @return 导出类型
     */
    public static ExportType fromCode(String code) {
        for (ExportType type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("不支持的导出类型: " + code);
    }
}