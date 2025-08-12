package com.blog.export.controller;

import com.blog.annotation.AccessLimit;
import com.blog.annotation.LogAnnotation;
import com.blog.constants.AccessLimitConst;
import com.blog.constants.LogConst;
import com.blog.domain.response.ResponseResult;
import com.blog.export.dto.ExportResult;
import com.blog.export.enums.BusinessType;
import com.blog.export.enums.ExportType;
import com.blog.export.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 数据导出控制器
 * 
 * @author haibara
 * @description 提供数据导出相关接口
 * @since 2025/1/21
 */
@Slf4j
@RestController
@RequestMapping("/export")
@Tag(name = "数据导出相关接口")
@Validated
public class ExportController {
    
    @Resource
    private ExportService exportService;
    
    /**
     * 导出数据
     * <p>
     * 根据指定的业务类型和导出类型导出数据。
     * 该接口需要系统导出权限，支持导出为HTML或Excel格式。
     * 操作完成后会记录操作日志。
     *
     * @param businessType 业务类型，支持的值：
     *                    <ul>
     *                        <li>USER: 用户管理</li>
     *                        <li>ROLE: 角色管理</li>
     *                        <li>CATEGORY: 分类管理</li>
     *                        <li>TAG: 标签管理</li>
     *                        <li>COMMENT: 评论管理</li>
     *                        <li>BLACK_LIST: 黑名单管理</li>
     *                        <li>LOGIN_LOG: 登录日志</li>
     *                        <li>OPERATE_LOG: 操作日志</li>
     *                    </ul>
     * @param exportType 导出类型，支持的值：
     *                  <ul>
     *                      <li>HTML: HTML格式</li>
     *                      <li>EXCEL: Excel格式</li>
     *                  </ul>
     * @param fileName 文件名，可选参数，如果不提供则使用业务类型的默认名称
     * @param response HTTP响应对象，用于返回文件内容
     * @see BusinessType 业务类型枚举
     * @see ExportType 导出类型枚举
     * @see LogConst#EXPORT 操作类型：导出
     */
    @PreAuthorize("hasAnyAuthority('system:export')")
    @Operation(summary = "导出数据")
    @Parameters({
            @Parameter(name = "businessType", description = "业务类型", required = true),
            @Parameter(name = "exportType", description = "导出类型", required = true),
            @Parameter(name = "fileName", description = "文件名（可选）", required = false)
    })
    @LogAnnotation(module = "数据导出", operation = LogConst.EXPORT)
    @AccessLimit(seconds = AccessLimitConst.DEFAULT_SECONDS,
            maxCount = AccessLimitConst.DEFAULT_MAX_COUNT)
    @GetMapping("/{businessType}/{exportType}")
    public void export(
            @PathVariable("businessType") String businessType,
            @PathVariable("exportType") String exportType,
            @RequestParam(value = "fileName", required = false) String fileName,
            HttpServletResponse response) {
        
        try {
            log.info("接收到导出请求: businessType={}, exportType={}, fileName={}", 
                    businessType, exportType, fileName);
            
            // 解析业务类型和导出类型
            BusinessType bizType = BusinessType.fromCode(businessType);
            ExportType expType = ExportType.fromCode(exportType);
            
            // 执行导出
            ExportResult result = exportService.export(bizType, expType, fileName);
            
            if (result.isSuccess()) {
                // 设置响应头
                setResponseHeaders(response, result);
                
                // 写入响应内容
                response.getOutputStream().write(result.getContent());
                response.getOutputStream().flush();
                
                log.info("导出文件下载成功: {}", result.getFileName());
            } else {
                // 导出失败，返回错误信息
                handleExportError(response, result.getErrorMessage());
            }
            
        } catch (IllegalArgumentException e) {
            log.error("导出参数错误: {}", e.getMessage());
            handleExportError(response, "导出参数错误: " + e.getMessage());
        } catch (IOException e) {
            log.error("导出文件写入响应失败", e);
            handleExportError(response, "文件下载失败");
        } catch (Exception e) {
            log.error("导出过程中发生未知异常", e);
            handleExportError(response, "导出失败：系统异常");
        }
    }
    
    /**
     * 获取支持的业务类型列表
     * <p>
     * 返回系统支持导出的所有业务类型及其描述信息。
     * 该接口需要系统导出权限。
     *
     * @return 响应结果，包含支持的业务类型列表
     *         <ul>
     *             <li>成功时返回业务类型列表</li>
     *             <li>失败时返回错误信息</li>
     *         </ul>
     */
    @PreAuthorize("hasAnyAuthority('system:export')")
    @Operation(summary = "获取支持的业务类型")
    @AccessLimit(seconds = AccessLimitConst.DEFAULT_SECONDS,
            maxCount = AccessLimitConst.DEFAULT_MAX_COUNT)
    @GetMapping("/business-types")
    public ResponseResult<List<BusinessType>> getSupportedBusinessTypes() {
        try {
            List<BusinessType> businessTypes = exportService.getSupportedBusinessTypes();
            return ResponseResult.success(businessTypes);
        } catch (Exception e) {
            log.error("获取支持的业务类型失败", e);
            return ResponseResult.failure("获取业务类型列表失败");
        }
    }
    
    /**
     * 获取指定业务类型支持的导出类型
     * <p>
     * 根据业务类型返回该业务支持的所有导出格式。
     * 该接口需要系统导出权限。
     *
     * @param businessType 业务类型代码
     * @return 响应结果，包含支持的导出类型列表
     */
    @PreAuthorize("hasAnyAuthority('system:export')")
    @Operation(summary = "获取指定业务类型支持的导出类型")
    @Parameter(name = "businessType", description = "业务类型", required = true)
    @AccessLimit(seconds = AccessLimitConst.DEFAULT_SECONDS,
            maxCount = AccessLimitConst.DEFAULT_MAX_COUNT)
    @GetMapping("/export-types/{businessType}")
    public ResponseResult<List<ExportType>> getSupportedExportTypes(
            @PathVariable("businessType") String businessType) {
        try {
            BusinessType bizType = BusinessType.fromCode(businessType);
            List<ExportType> exportTypes = exportService.getSupportedExportTypes(bizType);
            return ResponseResult.success(exportTypes);
        } catch (IllegalArgumentException e) {
            log.error("无效的业务类型: {}", businessType);
            return ResponseResult.failure("无效的业务类型");
        } catch (Exception e) {
            log.error("获取支持的导出类型失败", e);
            return ResponseResult.failure("获取导出类型列表失败");
        }
    }
    
    /**
     * 检查导出权限
     * <p>
     * 检查当前用户是否有权限导出指定业务类型的数据。
     * 该接口需要系统导出权限。
     *
     * @param businessType 业务类型代码
     * @return 响应结果，包含权限检查结果
     */
    @PreAuthorize("hasAnyAuthority('system:export')")
    @Operation(summary = "检查导出权限")
    @Parameter(name = "businessType", description = "业务类型", required = true)
    @AccessLimit(seconds = AccessLimitConst.DEFAULT_SECONDS,
            maxCount = AccessLimitConst.DEFAULT_MAX_COUNT)
    @GetMapping("/permission/{businessType}")
    public ResponseResult<Boolean> checkExportPermission(
            @PathVariable("businessType") String businessType) {
        try {
            BusinessType bizType = BusinessType.fromCode(businessType);
            boolean hasPermission = exportService.hasPermission(bizType);
            return ResponseResult.success(hasPermission);
        } catch (IllegalArgumentException e) {
            log.error("无效的业务类型: {}", businessType);
            return ResponseResult.failure("无效的业务类型");
        } catch (Exception e) {
            log.error("检查导出权限失败", e);
            return ResponseResult.failure("权限检查失败");
        }
    }
    
    /**
     * 设置响应头
     */
    private void setResponseHeaders(HttpServletResponse response, ExportResult result) {
        // 设置内容类型
        response.setContentType(result.getContentType());
        
        // 设置字符编码
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        
        // 设置文件下载头
        String encodedFileName = URLEncoder.encode(result.getFileName(), StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", 
                String.format("attachment; filename=\"%s\"; filename*=UTF-8''%s", 
                        encodedFileName, encodedFileName));
        
        // 设置内容长度
        if (result.getFileSize() > 0) {
            response.setContentLengthLong(result.getFileSize());
        }
        
        // 设置缓存控制
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
    }
    
    /**
     * 处理导出错误
     */
    private void handleExportError(HttpServletResponse response, String errorMessage) {
        try {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            
            String errorJson = String.format("{\"success\": false, \"message\": \"%s\"}", errorMessage);
            response.getWriter().write(errorJson);
            response.getWriter().flush();
        } catch (IOException e) {
            log.error("写入错误响应失败", e);
        }
    }
}