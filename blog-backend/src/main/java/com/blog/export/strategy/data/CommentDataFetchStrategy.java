package com.blog.export.strategy.data;

import com.blog.domain.vo.CommentListVO;
import com.blog.export.enums.BusinessType;
import com.blog.export.strategy.DataFetchStrategy;
import com.blog.service.CommentService;
import com.blog.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 评论数据获取策略
 * 
 * @author haibara
 * @description 从评论服务获取评论列表数据
 * @since 2025/1/21
 */
@Component
public class CommentDataFetchStrategy implements DataFetchStrategy<CommentListVO> {
    
    @Resource
    private CommentService commentService;
    
    @Override
    public List<CommentListVO> fetchData() {
        // 调用CommentService的getBackCommentList方法获取后台评论列表
        return commentService.getBackCommentList(null);
    }
    
    @Override
    public BusinessType getBusinessType() {
        return BusinessType.COMMENT;
    }
    
    @Override
    public Class<CommentListVO> getDataClass() {
        return CommentListVO.class;
    }
    
    @Override
    public boolean hasPermission() {
        // 检查是否有评论列表查看权限
        return SecurityUtils.hasAnyAuthority("blog:comment:list");
    }
}