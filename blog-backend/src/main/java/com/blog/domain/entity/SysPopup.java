package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.blog.domain.BaseData;

import java.sql.Time;
import java.util.Date;

/**
 * @author haibara
 * @description sys_popup表实体类
 * @since 2025/8/7 14:20
 */
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@TableName("sys_popup")
@Schema(description = "弹窗管理实体")
public class SysPopup implements BaseData {

    @Schema(description = "弹窗ID", example = "1")
    private Long id;

    @Schema(description = "弹窗标题", example = "欢迎访问")
    private String title;

    @Schema(description = "弹窗内容", example = "# 欢迎来到 Haibara Blog\n\n感谢您的访问！")
    private String content;

    @Schema(description = "弹窗类型：1-通知，2-广告，3-公告，4-活动", example = "1", allowableValues = {"1", "2", "3", "4"})
    private Integer popupType;

    @Schema(description = "显示位置：1-前台，2-管理端", example = "1", allowableValues = {"1", "2"})
    private Integer displayPosition;

    @Schema(description = "目标页面路径(多个用逗号分隔,如:/,/article,/about,为空则所有页面显示)", example = "/home,/article")
    private String targetPages;

    @Schema(description = "目标用户：1-所有用户，2-登录用户，3-游客", example = "1", allowableValues = {"1", "2", "3"})
    private Integer targetUsers;

    @Schema(description = "开始日期(为空则立即生效)", example = "2025-08-07 14:30:00")
    private Date startDate;

    @Schema(description = "结束日期(为空则永久有效)", example = "2025-12-31 23:59:59")
    private Date endDate;

    @Schema(description = "开始时间(若与结束时间都为null则全天都能看到)", example = "09:00:00")
    private Time startTime;

    @Schema(description = "结束时间(若与开始时间都为null则全天都能看到)", example = "18:00:00")
    private Time endTime;

    @Schema(description = "显示模式：1-每次刷新，2-会话期间一次，3-每日一次，4-永久一次", example = "1", allowableValues = {"1", "2", "3", "4"})
    private Integer displayMode;

    @Schema(description = "内容类型：1-Markdown，2-HTML，3-纯文本", example = "1", allowableValues = {"1", "2", "3"})
    private Integer contentType;

    @Schema(description = "弹窗图片URL", example = "https://example.com/popup-image.jpg")
    private String imageUrl;

    @Schema(description = "点击按钮后的跳转链接", example = "https://example.com/target-page")
    private String jumpUrl;

    @Schema(description = "弹窗位置", example = "center")
    private String popupPosition;

    @Schema(description = "优先级权重(1-99,默认50,数字越大优先级越高)", example = "50")
    private Integer priority;

    @Schema(description = "用户是否可关闭：0-不可关闭，1-可关闭", example = "1", allowableValues = {"0", "1"})
    private Integer closeable;

    @Schema(description = "自动关闭时间(秒,为空则不自动关闭)", example = "10")
    private Integer autoCloseTime;

    @Schema(description = "是否禁用：0-否，1-是", example = "0", allowableValues = {"0", "1"})
    private Integer isDisable;

    @Schema(description = "创建时间", example = "2025-08-07 14:30:00")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @Schema(description = "更新时间", example = "2025-08-07 15:30:00")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @Schema(description = "是否删除：0-未删除，1-已删除", example = "0", allowableValues = {"0", "1"})
    private Integer isDeleted;
}
