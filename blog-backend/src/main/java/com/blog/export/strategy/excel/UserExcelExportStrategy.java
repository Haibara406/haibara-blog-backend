package com.blog.export.strategy.excel;

import com.blog.domain.vo.UserListVO;
import com.blog.export.dto.ExportResult;
import com.blog.export.enums.BusinessType;
import com.blog.export.enums.ExportType;
import com.blog.export.strategy.ExportStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 用户Excel导出策略
 * 
 * @author haibara
 * @description 将用户数据导出为Excel格式
 * @since 2025/1/21
 */
@Slf4j
@Component
public class UserExcelExportStrategy implements ExportStrategy<UserListVO> {
    
    private static final String SHEET_NAME = "用户列表";
    private static final String[] HEADERS = {
        "序号", "用户ID", "用户名", "邮箱", "注册方式", "登录地址", "状态", "创建时间"
    };
    
    @Override
    public ExportResult export(List<UserListVO> data, String fileName) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            log.info("开始导出用户数据为Excel格式，数据条数: {}, 文件名: {}", data.size(), fileName);
            
            // 创建工作表
            Sheet sheet = workbook.createSheet(SHEET_NAME);
            
            // 创建样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            // 创建表头
            createHeader(sheet, headerStyle);
            
            // 填充数据
            fillData(sheet, data, dataStyle);
            
            // 自动调整列宽
            autoSizeColumns(sheet);
            
            // 写入输出流
            workbook.write(outputStream);
            
            // 生成文件名
            String finalFileName = generateFileName(fileName);
            
            byte[] contentBytes = outputStream.toByteArray();
            log.info("用户Excel导出完成，文件大小: {} bytes", contentBytes.length);
            
            return ExportResult.success(
                contentBytes, 
                finalFileName, 
                getExportType().getContentType(), 
                data.size()
            );
            
        } catch (IOException e) {
            log.error("用户Excel导出失败", e);
            return ExportResult.failure("Excel导出失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        style.setFont(font);
        
        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }
    
    /**
     * 创建数据样式
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }
    
    /**
     * 创建表头
     */
    private void createHeader(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        headerRow.setHeight((short) (20 * 20)); // 设置行高
        
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }
    }
    
    /**
     * 填充数据
     */
    private void fillData(Sheet sheet, List<UserListVO> data, CellStyle dataStyle) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        for (int i = 0; i < data.size(); i++) {
            Row row = sheet.createRow(i + 1);
            UserListVO user = data.get(i);
            
            // 序号
            createCell(row, 0, i + 1, dataStyle);
            // 用户ID
            createCell(row, 1, user.getId(), dataStyle);
            // 用户名
            createCell(row, 2, user.getUsername(), dataStyle);
            // 邮箱
            createCell(row, 3, user.getEmail(), dataStyle);
            // 注册方式
            createCell(row, 4, getRegisterTypeText(user.getRegisterType()), dataStyle);
            // 登录地址
            createCell(row, 5, user.getLoginAddress() != null ? user.getLoginAddress() : "暂无", dataStyle);
            // 状态
            createCell(row, 6, user.getIsDisable() == 0 ? "正常" : "禁用", dataStyle);
            // 创建时间
            createCell(row, 7, user.getCreateTime() != null ? dateFormat.format(user.getCreateTime()) : "", dataStyle);
        }
    }
    
    /**
     * 创建单元格
     */
    private void createCell(Row row, int columnIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        if (value != null) {
            if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else {
                cell.setCellValue(value.toString());
            }
        }
        cell.setCellStyle(style);
    }
    
    /**
     * 自动调整列宽
     */
    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
            // 设置最小列宽
            int currentWidth = sheet.getColumnWidth(i);
            if (currentWidth < 2000) {
                sheet.setColumnWidth(i, 2000);
            }
            // 设置最大列宽，防止过宽
            if (currentWidth > 8000) {
                sheet.setColumnWidth(i, 8000);
            }
        }
    }
    
    /**
     * 获取注册方式文本
     */
    private String getRegisterTypeText(Integer registerType) {
        if (registerType == null) {
            return "未知";
        }
        switch (registerType) {
            case 0: return "邮箱注册";
            case 1: return "Gitee";
            case 2: return "Github";
            default: return "未知";
        }
    }
    
    /**
     * 生成文件名
     */
    private String generateFileName(String baseName) {
        if (baseName == null || baseName.trim().isEmpty()) {
            baseName = "用户列表";
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("%s_%s%s", baseName, timestamp, getExportType().getExtension());
    }
    
    @Override
    public ExportType getExportType() {
        return ExportType.EXCEL;
    }
    
    @Override
    public BusinessType getBusinessType() {
        return BusinessType.USER;
    }
}