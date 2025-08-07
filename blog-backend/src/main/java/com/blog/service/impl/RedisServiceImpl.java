package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.constants.RedisConst;
import com.blog.constants.SQLConst;
import com.blog.domain.entity.*;
import com.blog.domain.vo.CategoryVO;
import com.blog.domain.vo.TagVO;
import com.blog.enums.CommentEnum;
import com.blog.enums.FavoriteEnum;
import com.blog.enums.LikeEnum;
import com.blog.mapper.*;
import com.blog.service.CategoryService;
import com.blog.service.RedisService;
import com.blog.service.TagService;
import com.blog.utils.RedisCache;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author haibara
 * @description redis相关接口实现类
 * @since 2025/7/27 15:37
 */

@Slf4j
@Service
public class RedisServiceImpl implements RedisService {

    @Resource
    private RedisCache redisCache;

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private FavoriteMapper favoriteMapper;

    @Resource
    private LikeMapper likeMapper;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private BlackListMapper blackListMapper;

    @Resource
    private CategoryService categoryService;

    @Resource
    private TagService tagService;

    @Override
    public void articleCountClear() {
        log.info("--------执行清除redis文章相关数量缓存--------");
        boolean isDel = redisCache.deleteObject(RedisConst.ARTICLE_FAVORITE_COUNT);
        isDel = isDel && redisCache.deleteObject(RedisConst.ARTICLE_LIKE_COUNT);
        isDel = isDel && redisCache.deleteObject(RedisConst.ARTICLE_COMMENT_COUNT);
        if (isDel) log.info("--------清除redis文章相关数量缓存成功--------");
        else log.info("--------清除redis文章相关数量缓存失败--------");
    }

    @Override
    public void articleVisitCount() {
        try {
            // 批量构建访问量缓存Map
            Map<String, Object> visitCountMap = articleMapper.selectList(null)
                    .stream()
                    .collect(Collectors.toMap(
                            article -> RedisConst.ARTICLE_VISIT_COUNT + article.getId(),
                            Article::getVisitCount
                    ));

            // 批量设置到Redis
            if (!visitCountMap.isEmpty()) {
                visitCountMap.forEach((key, value) -> redisCache.setCacheObject(key, value));
                log.info("--------执行redis文章访问量缓存成功，缓存文章数量: {}--------", visitCountMap.size());
            } else {
                log.info("--------没有文章数据，无法缓存访问量--------");
            }
        } catch (Exception e) {
            log.error("--------执行redis文章访问量缓存失败--------", e);
        }
    }

    @Override
    public void clearLimitCache() {
        log.info("--------执行清除redis限流缓存--------:");
        try {
            Collection<String> keys = redisCache.keys("limit*");
            if (redisCache.deleteObject(keys) > 0) log.info("--------清除redis限流缓存成功--------");
            else log.info("--------没有redis限流缓存，无法清除--------");
        } catch (Exception e) {
            log.error("--------执行清除redis限流缓存失败", e);
        }
    }

    @Override
    public void initCount() {
        log.info("--------开始执行缓存文章点赞数量，评论数量，收藏数量--------");
        long startTime = System.currentTimeMillis();
        // 获取所有文章ID
        List<Long> articleIds = articleMapper.selectList(null).stream()
                .map(Article::getId).toList();

        if (articleIds.isEmpty()) return;

        // 批量查询收藏数
        Map<String, Long> favoriteCount = favoriteMapper.selectList(
                new LambdaQueryWrapper<Favorite>()
                        .in(Favorite::getTypeId, articleIds)
                        .eq(Favorite::getType, FavoriteEnum.FAVORITE_TYPE_ARTICLE.getType())
        ).stream().collect(Collectors.groupingBy(
                f -> f.getTypeId().toString(),
                Collectors.counting()
        ));

        // 批量查询点赞数
        Map<String, Long> likeCount = likeMapper.selectList(
                new LambdaQueryWrapper<Like>()
                        .in(Like::getTypeId, articleIds)
                        .eq(Like::getType, LikeEnum.LIKE_TYPE_ARTICLE.getType())
        ).stream().collect(Collectors.groupingBy(
                l -> l.getTypeId().toString(),
                Collectors.counting()
        ));

        // 批量查询评论数
        Map<String, Long> commentCount = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .in(Comment::getTypeId, articleIds)
                        .eq(Comment::getType, CommentEnum.COMMENT_TYPE_ARTICLE.getType())
                        .eq(Comment::getIsCheck, SQLConst.COMMENT_IS_CHECK)
        ).stream().collect(Collectors.groupingBy(
                c -> c.getTypeId().toString(),
                Collectors.counting()
        ));

        // 缓存到Redis
        redisCache.setCacheMap(RedisConst.ARTICLE_FAVORITE_COUNT, favoriteCount);
        redisCache.setCacheMap(RedisConst.ARTICLE_LIKE_COUNT, likeCount);
        redisCache.setCacheMap(RedisConst.ARTICLE_COMMENT_COUNT, commentCount);

        log.info("--------成功执行缓存文章点赞数量，评论数量，收藏数量--------");
        log.info("批量查询完成，耗时: {}ms, 文章数: {}, 收藏记录: {}, 点赞记录: {}, 评论记录: {}",
                System.currentTimeMillis() - startTime,
                articleIds.size(),
                favoriteCount.size(),
                likeCount.size(),
                commentCount.size());
    }

    @Override
    public void initBlackList() {
        // 清除黑名单缓存
        redisCache.deleteObject(RedisConst.BLACK_LIST_UID_KEY);
        redisCache.deleteObject(RedisConst.BLACK_LIST_IP_KEY);

        // 将所有黑名单id初始化到redis中
        log.info("--------开始执行初始化黑名单缓存--------");
        List<BlackList> blackLists = blackListMapper.selectList(null);

        if (!blackLists.isEmpty()) {
            // 分别构建用户ID和IP的缓存Map
            Map<String, Object> uidMap = new HashMap<>();
            Map<String, Object> ipMap = new HashMap<>();

            blackLists.forEach(blackList -> {
                if (blackList.getUserId() != null) {
                    uidMap.put(blackList.getUserId().toString(), blackList.getExpiresTime());
                } else {
                    ipMap.put(blackList.getIpInfo().getCreateIp(), blackList.getExpiresTime());
                }
            });

            // 批量设置到Redis
            if (!uidMap.isEmpty()) {
                uidMap.forEach((key, value) -> redisCache.setCacheMapValue(RedisConst.BLACK_LIST_UID_KEY, key, value));
            }
            if (!ipMap.isEmpty()) {
                ipMap.forEach((key, value) -> redisCache.setCacheMapValue(RedisConst.BLACK_LIST_IP_KEY, key, value));
            }

            log.info("--------成功执行初始化黑名单缓存，用户黑名单: {}个，IP黑名单: {}个--------",
                    uidMap.size(), ipMap.size());
        } else {
            log.info("--------没有黑名单信息，无法初始化--------");
        }
    }

    @Override
    public void preloadCategories() {
        log.info("--------开始预热分类缓存--------");
        try {
            List<CategoryVO> categories = reloadCategories();
            log.info("--------分类缓存预热完成，缓存数量: {}--------", categories.size());
        } catch (Exception e) {
            log.error("--------分类缓存预热失败--------", e);
        }
    }

    @Override
    public void preloadTags() {
        log.info("--------开始预热标签缓存--------");
        try {
            List<TagVO> tags = reloadTags();
            log.info("--------标签缓存预热完成，缓存数量: {}--------", tags.size());
        } catch (Exception e) {
            log.error("--------标签缓存预热失败--------", e);
        }
    }

    @Override
    public List<CategoryVO> reloadCategories() {
        log.debug("--------从数据库重新加载分类数据--------");
        // 调用CategoryService的原始查询方法（不走缓存）
        List<CategoryVO> categories = categoryService.listAllCategoryFromDB();

        // 缓存到Redis（设置24小时过期）
        redisCache.setCacheObject(RedisConst.CATEGORY_LIST, categories, 24, TimeUnit.HOURS);
        log.debug("--------分类数据已缓存到Redis，数量: {}--------", categories.size());

        return categories;
    }

    @Override
    public List<TagVO> reloadTags() {
        log.debug("--------从数据库重新加载标签数据--------");
        // 调用TagService的原始查询方法（不走缓存）
        List<TagVO> tags = tagService.listAllTagFromDB();

        // 缓存到Redis（设置24小时过期）
        redisCache.setCacheObject(RedisConst.TAG_LIST, tags, 24, TimeUnit.HOURS);
        log.debug("--------标签数据已缓存到Redis，数量: {}--------", tags.size());

        return tags;
    }
}
