package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.Popup;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author haibara
 * @description 弹窗管理表数据库访问层
 * @since 2025/8/7
 */
@Mapper
public interface PopupMapper extends BaseMapper<Popup> {
}
