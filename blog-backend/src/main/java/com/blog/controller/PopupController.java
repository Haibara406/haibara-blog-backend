package com.blog.controller;

import com.blog.annotation.AccessLimit;
import com.blog.annotation.LogAnnotation;
import com.blog.constants.AccessLimitConst;
import com.blog.constants.LogConst;
import com.blog.domain.dto.PopupDTO;
import com.blog.domain.dto.PopupQueryDTO;
import com.blog.domain.response.ResponseResult;
import com.blog.domain.vo.PageVO;
import com.blog.domain.vo.PopupVO;
import com.blog.service.PopupService;
import com.blog.utils.ControllerUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author haibara
 * @description 弹窗管理相关接口
 * @since 2025/8/7
 */
@Slf4j
@RestController
@Tag(name = "弹窗管理相关接口")
@RequestMapping("/popup")
@Validated
public class PopupController {

    @Resource
    private PopupService popupService;

    // ==================== 前台接口 ====================

    @Operation(summary = "获取当前页面弹窗")
    @Parameters({
            @Parameter(name = "currentPage", description = "当前页面路径", required = true),
            @Parameter(name = "sessionId", description = "会话ID", required = true)
    })
    @AccessLimit(seconds = AccessLimitConst.DEFAULT_SECONDS,
            maxCount = AccessLimitConst.DEFAULT_MAX_COUNT)
    @GetMapping("/current")
    public ResponseResult<List<PopupVO>> getCurrentPagePopups(
            @RequestParam("currentPage") @NotNull String currentPage,
            @RequestParam("sessionId") @NotNull String sessionId
    ) {
        return ControllerUtils.messageHandler(() -> popupService.getCurrentPagePopups(currentPage, sessionId));
    }

    // ==================== 后台管理接口 ====================

    @PreAuthorize("hasAnyAuthority('blog:popup:list')")
    @Operation(summary = "获取弹窗列表")
    @Parameters({
            @Parameter(name = "current", description = "页码", required = true),
            @Parameter(name = "pageSize", description = "每页数量", required = true)
    })
    @LogAnnotation(module = "弹窗管理", operation = LogConst.GET)
    @AccessLimit(seconds = AccessLimitConst.DEFAULT_SECONDS,
            maxCount = AccessLimitConst.DEFAULT_MAX_COUNT)
    @GetMapping("/back/list/{current}/{pageSize}")
    public ResponseResult<PageVO<List<PopupVO>>> getPopupList(
            @PathVariable("current") @NotNull Long current,
            @PathVariable("pageSize") @NotNull Long pageSize
    ) {
        return ControllerUtils.messageHandler(() -> popupService.getPopupList(current, pageSize));
    }

    @PreAuthorize("hasAnyAuthority('blog:popup:search')")
    @Operation(summary = "搜索弹窗列表")
    @Parameter(name = "popupQueryDTO", description = "搜索条件")
    @LogAnnotation(module = "弹窗管理", operation = LogConst.SEARCH)
    @AccessLimit(seconds = AccessLimitConst.DEFAULT_SECONDS,
            maxCount = AccessLimitConst.DEFAULT_MAX_COUNT)
    @PostMapping("/back/search")
    public ResponseResult<PageVO<List<PopupVO>>> searchPopup(@RequestBody @Valid PopupQueryDTO popupQueryDTO) {
        return ControllerUtils.messageHandler(() -> popupService.searchPopup(popupQueryDTO));
    }

    @PreAuthorize("hasAnyAuthority('blog:popup:add')")
    @Operation(summary = "添加弹窗")
    @Parameter(name = "popupDTO", description = "弹窗信息")
    @LogAnnotation(module = "弹窗管理", operation = LogConst.INSERT)
    @AccessLimit(seconds = AccessLimitConst.DEFAULT_SECONDS,
            maxCount = AccessLimitConst.DEFAULT_MAX_COUNT)
    @PostMapping("/back/add")
    public ResponseResult<Void> addPopup(@RequestBody @Valid PopupDTO popupDTO) {
        return popupService.addPopup(popupDTO);
    }

    @PreAuthorize("hasAnyAuthority('blog:popup:update')")
    @Operation(summary = "修改弹窗")
    @Parameter(name = "popupDTO", description = "弹窗信息")
    @LogAnnotation(module = "弹窗管理", operation = LogConst.UPDATE)
    @AccessLimit(seconds = AccessLimitConst.DEFAULT_SECONDS,
            maxCount = AccessLimitConst.DEFAULT_MAX_COUNT)
    @PutMapping("/back/update")
    public ResponseResult<Void> updatePopup(@RequestBody @Valid PopupDTO popupDTO) {
        return popupService.updatePopup(popupDTO);
    }

    @PreAuthorize("hasAnyAuthority('blog:popup:get')")
    @Operation(summary = "获取弹窗详情")
    @Parameter(name = "id", description = "弹窗ID", required = true)
    @LogAnnotation(module = "弹窗管理", operation = LogConst.GET)
    @AccessLimit(seconds = AccessLimitConst.DEFAULT_SECONDS,
            maxCount = AccessLimitConst.DEFAULT_MAX_COUNT)
    @GetMapping("/back/get/{id}")
    public ResponseResult<PopupDTO> getPopupById(@PathVariable("id") @NotNull Long id) {
        return ControllerUtils.messageHandler(() -> popupService.getPopupById(id));
    }

    @PreAuthorize("hasAnyAuthority('blog:popup:delete')")
    @Operation(summary = "删除弹窗")
    @Parameter(name = "ids", description = "弹窗ID列表")
    @LogAnnotation(module = "弹窗管理", operation = LogConst.DELETE)
    @AccessLimit(seconds = AccessLimitConst.DEFAULT_SECONDS,
            maxCount = AccessLimitConst.DEFAULT_MAX_COUNT)
    @DeleteMapping("/back/delete")
    public ResponseResult<Void> deletePopup(@RequestBody List<Long> ids) {
        return popupService.deletePopup(ids);
    }

    @PreAuthorize("hasAnyAuthority('blog:popup:status:update')")
    @Operation(summary = "更新弹窗状态")
    @Parameters({
            @Parameter(name = "id", description = "弹窗ID", required = true),
            @Parameter(name = "status", description = "状态(0:禁用 1:启用)", required = true)
    })
    @LogAnnotation(module = "弹窗管理", operation = LogConst.UPDATE)
    @AccessLimit(seconds = AccessLimitConst.DEFAULT_SECONDS,
            maxCount = AccessLimitConst.DEFAULT_MAX_COUNT)
    @PostMapping("/back/update/status")
    public ResponseResult<Void> updateStatus(
            @RequestParam("id") @NotNull Long id,
            @RequestParam("status") @NotNull Integer status
    ) {
        return popupService.updateStatus(id, status);
    }

    @PreAuthorize("hasAnyAuthority('blog:popup:upload')")
    @Operation(summary = "上传弹窗图片")
    @Parameter(name = "file", description = "图片文件", required = true)
    @LogAnnotation(module = "弹窗管理", operation = LogConst.UPLOAD_IMAGE)
    @AccessLimit(seconds = AccessLimitConst.DEFAULT_SECONDS,
            maxCount = AccessLimitConst.DEFAULT_MAX_COUNT)
    @PostMapping("/back/upload/image")
    public ResponseResult<String> uploadImage(@RequestParam("file") MultipartFile file) {
        return popupService.uploadImage(file);
    }
}
