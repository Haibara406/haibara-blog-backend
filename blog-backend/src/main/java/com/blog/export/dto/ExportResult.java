package com.blog.export.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导出结果数据传输对象
 * 
 * @author haibara
 * @description 封装导出操作的结果信息
 * @since 2025/1/21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportResult {
    
    /**
     * 导出是否成功
     */
    private boolean success;
    
    /**
     * 导出的文件内容（字节数组）
     */
    private byte[] content;
    
    /**
     * 文件名（包含扩展名）
     */
    private String fileName;
    
    /**
     * 内容类型（MIME类型）
     */
    private String contentType;
    
    /**
     * 文件大小（字节）
     */
    private long fileSize;
    
    /**
     * 错误信息（当success为false时）
     */
    private String errorMessage;
    
    /**
     * 导出的数据条数
     */
    private int recordCount;
    
    /**
     * 导出时间戳
     */
    private long timestamp;
    
    /**
     * 创建成功的导出结果
     * 
     * @param content 文件内容
     * @param fileName 文件名
     * @param contentType 内容类型
     * @param recordCount 记录数
     * @return 导出结果
     */
    public static ExportResult success(byte[] content, String fileName, String contentType, int recordCount) {
        return ExportResult.builder()
                .success(true)
                .content(content)
                .fileName(fileName)
                .contentType(contentType)
                .fileSize(content != null ? content.length : 0)
                .recordCount(recordCount)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 创建失败的导出结果
     * 
     * @param errorMessage 错误信息
     * @return 导出结果
     */
    public static ExportResult failure(String errorMessage) {
        return ExportResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}