package com.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.domain.entity.Popup;
import com.blog.mapper.PopupMapper;
import com.blog.service.PopupService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * @author haibara
 * @description 弹窗管理服务实现类
 * @since 2025/8/7
 */
@Slf4j
@Service("sysPopupService")
public class PopupServiceImpl extends ServiceImpl<PopupMapper, Popup> implements PopupService {

    @Resource
    private PopupMapper popupMapper;

}
