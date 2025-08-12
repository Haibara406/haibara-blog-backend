package com.blog.export.strategy.excel;

import com.blog.domain.vo.LogVO;
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
 * 操作日志Excel导出策略
 * 
 * @author haibara
 * @description 将操作日志数据导出为Excel格式
 * @since 2025/1/21
 */
@Slf4j
@Component
public class OperateLogExcelExportStrategy implements ExportStrategy<LogVO> {
    
    private static final String SHEET_NAME = "操作日志列表";
    private static final String[] HEADERS = {
        "序号", "日志ID", "模块名称", "操作名称", "操作人员", "操作IP", "操作地址", "操作状态", "操作时间", "消耗时间(ms)"
    };
    
    @Override
    public ExportResult export(List<LogVO> data, String fileName) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            log.info("开始导出操作日志数据为Excel格式，数据条数: {}, 文件名: {}", data.size(), fileName);
            
            Sheet sheet = workbook.createSheet(SHEET_NAME);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            createHeader(sheet, headerStyle);
            fillData(sheet, data, dataStyle);
            autoSizeColumns(sheet);
            
            workbook.write(outputStream);
            String finalFileName = generateFileName(fileName);
            byte[] contentBytes = outputStream.toByteArray();
            
            log.info("操作日志Excel导出完成，文件大小: {} bytes", contentBytes.length);
            
            return ExportResult.success(contentBytes, finalFileName, getExportType().getContentType(), data.size());
            
        } catch (IOException e) {
            log.error("操作日志Excel导出失败", e);
            return ExportResult.failure("Excel导出失败: " + e.getMessage());
        }
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        style.setFont(font);
        
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }
    
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }
    
    private void createHeader(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        headerRow.setHeight((short) (20 * 20));
        
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }
    }
    
    private void fillData(Sheet sheet, List<LogVO> data, CellStyle dataStyle) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        for (int i = 0; i < data.size(); i++) {
            Row row = sheet.createRow(i + 1);
            LogVO operateLog = data.get(i);
            
            createCell(row, 0, i + 1, dataStyle);
            createCell(row, 1, operateLog.getId(), dataStyle);
            createCell(row, 2, operateLog.getModule(), dataStyle);
            createCell(row, 3, operateLog.getOperation(), dataStyle);
            createCell(row, 4, operateLog.getUserName(), dataStyle);
            createCell(row, 5, operateLog.getIp(), dataStyle);
            createCell(row, 6, operateLog.getAddress(), dataStyle);
            createCell(row, 7, getStateText(operateLog.getState()), dataStyle);
            createCell(row, 8, operateLog.getLoginTime() != null ? dateFormat.format(operateLog.getLoginTime()) : "", dataStyle);
            createCell(row, 9, operateLog.getTime(), dataStyle);
        }
    }
    
    private String getStateText(Integer state) {
        if (state == null) return "未知";
        switch (state) {
            case 0: return "成功";
            case 1: return "失败";
            case 2: return "异常";
            default: return "未知";
        }
    }
    
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
    
    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
            int currentWidth = sheet.getColumnWidth(i);
            if (currentWidth < 2000) {
                sheet.setColumnWidth(i, 2000);
            }
            if (currentWidth > 8000) {
                sheet.setColumnWidth(i, 8000);
            }
        }
    }
    
    private String generateFileName(String baseName) {
        if (baseName == null || baseName.trim().isEmpty()) {
            baseName = "操作日志列表";
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
        return BusinessType.OPERATE_LOG;
    }
}