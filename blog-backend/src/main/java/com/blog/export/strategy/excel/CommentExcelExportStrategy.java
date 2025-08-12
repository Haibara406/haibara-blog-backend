package com.blog.export.strategy.excel;

import com.blog.domain.vo.CommentListVO;
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
 * 评论Excel导出策略
 * 
 * @author haibara
 * @description 将评论数据导出为Excel格式
 * @since 2025/1/21
 */
@Slf4j
@Component
public class CommentExcelExportStrategy implements ExportStrategy<CommentListVO> {
    
    private static final String SHEET_NAME = "评论列表";
    private static final String[] HEADERS = {
        "序号", "评论ID", "评论类型", "类型ID", "评论内容", "评论用户", "审核状态", "评论时间"
    };
    
    @Override
    public ExportResult export(List<CommentListVO> data, String fileName) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            log.info("开始导出评论数据为Excel格式，数据条数: {}, 文件名: {}", data.size(), fileName);
            
            Sheet sheet = workbook.createSheet(SHEET_NAME);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            createHeader(sheet, headerStyle);
            fillData(sheet, data, dataStyle);
            autoSizeColumns(sheet);
            
            workbook.write(outputStream);
            String finalFileName = generateFileName(fileName);
            byte[] contentBytes = outputStream.toByteArray();
            
            log.info("评论Excel导出完成，文件大小: {} bytes", contentBytes.length);
            
            return ExportResult.success(contentBytes, finalFileName, getExportType().getContentType(), data.size());
            
        } catch (IOException e) {
            log.error("评论Excel导出失败", e);
            return ExportResult.failure("Excel导出失败: " + e.getMessage());
        }
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
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
    
    private void fillData(Sheet sheet, List<CommentListVO> data, CellStyle dataStyle) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        for (int i = 0; i < data.size(); i++) {
            Row row = sheet.createRow(i + 1);
            CommentListVO comment = data.get(i);
            
            createCell(row, 0, i + 1, dataStyle);
            createCell(row, 1, comment.getId(), dataStyle);
            createCell(row, 2, comment.getType() == 1 ? "文章评论" : "留言评论", dataStyle);
            createCell(row, 3, comment.getTypeId(), dataStyle);
            createCell(row, 4, comment.getCommentContent(), dataStyle);
            createCell(row, 5, comment.getCommentUserName(), dataStyle);
            createCell(row, 6, comment.getIsCheck() == 1 ? "已审核" : "未审核", dataStyle);
            createCell(row, 7, comment.getCreateTime() != null ? dateFormat.format(comment.getCreateTime()) : "", dataStyle);
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
            baseName = "评论列表";
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
        return BusinessType.COMMENT;
    }
}