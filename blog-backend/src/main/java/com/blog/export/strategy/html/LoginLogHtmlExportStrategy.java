package com.blog.export.strategy.html;

import com.blog.domain.vo.LoginLogVO;
import com.blog.export.dto.ExportResult;
import com.blog.export.enums.BusinessType;
import com.blog.export.enums.ExportType;
import com.blog.export.strategy.ExportStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 登录日志HTML导出策略
 * 
 * @author haibara
 * @description 将登录日志数据导出为HTML格式
 * @since 2025/1/21
 */
@Slf4j
@Component
public class LoginLogHtmlExportStrategy implements ExportStrategy<LoginLogVO> {
    
    @Resource
    private TemplateEngine templateEngine;
    
    private static final String TEMPLATE_NAME = "export/loginlog-export";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public ExportResult export(List<LoginLogVO> data, String fileName) {
        try {
            log.info("开始导出登录日志数据为HTML格式，数据条数: {}, 文件名: {}", data.size(), fileName);
            
            // 准备模板数据
            Context context = new Context();
            context.setVariable("loginLogList", data);
            context.setVariable("totalCount", data.size());
            context.setVariable("exportTime", LocalDateTime.now().format(DATE_FORMATTER));
            context.setVariable("fileName", fileName);
            context.setVariable("businessType", getBusinessType().getName());
            
            // 渲染HTML模板
            String htmlContent = templateEngine.process(TEMPLATE_NAME, context);
            
            // 转换为字节数组
            byte[] contentBytes = htmlContent.getBytes(StandardCharsets.UTF_8);
            
            // 生成文件名
            String finalFileName = generateFileName(fileName);
            
            log.info("登录日志HTML导出完成，文件大小: {} bytes", contentBytes.length);
            
            return ExportResult.success(
                contentBytes, 
                finalFileName, 
                getExportType().getContentType(), 
                data.size()
            );
            
        } catch (Exception e) {
            log.error("登录日志HTML导出失败", e);
            return ExportResult.failure("HTML导出失败: " + e.getMessage());
        }
    }
    
    @Override
    public ExportType getExportType() {
        return ExportType.HTML;
    }
    
    @Override
    public BusinessType getBusinessType() {
        return BusinessType.LOGIN_LOG;
    }
    
    /**
     * 生成文件名
     */
    private String generateFileName(String baseName) {
        if (baseName == null || baseName.trim().isEmpty()) {
            baseName = "登录日志列表";
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("%s_%s%s", baseName, timestamp, getExportType().getExtension());
    }
}