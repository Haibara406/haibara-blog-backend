package com.blog.service;

import com.blog.domain.vo.CategoryVO;
import com.blog.domain.vo.TagVO;

import java.util.List;

/**
 * @author haibara
 * @description redis相关接口
 * @since 2025/7/27 15:36
 */
public interface RedisService {

    void articleCountClear();

    void articleVisitCount();

    void clearLimitCache();

    void initCount();

    void initBlackList();

    /**
     * 预热分类缓存
     */
    void preloadCategories();

    /**
     * 预热标签缓存
     */
    void preloadTags();

    /**
     * 重新加载分类缓存
     */
    List<CategoryVO> reloadCategories();

    /**
     * 重新加载标签缓存
     */
    List<TagVO> reloadTags();
}
