package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.constants.ErrorConst;
import com.blog.constants.RedisConst;
import com.blog.constants.SQLConst;
import com.blog.constants.SuccessConst;
import com.blog.domain.dto.CommentIsCheckDTO;
import com.blog.domain.dto.SearchCommentDTO;
import com.blog.domain.dto.UserCommentDTO;
import com.blog.domain.entity.Comment;
import com.blog.domain.entity.LeaveWord;
import com.blog.domain.entity.Like;
import com.blog.domain.entity.User;
import com.blog.domain.response.ResponseResult;
import com.blog.domain.vo.ArticleCommentVO;
import com.blog.domain.vo.CommentListVO;
import com.blog.domain.vo.PageVO;
import com.blog.enums.CommentEnum;
import com.blog.enums.CommentTypeEnum;
import com.blog.enums.LikeEnum;
import com.blog.enums.MailboxAlertsEnum;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.LeaveWordMapper;
import com.blog.mapper.LikeMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.CommentService;
import com.blog.service.LikeService;
import com.blog.service.PublicService;
import com.blog.utils.RedisCache;
import com.blog.utils.SecurityUtils;
import com.blog.utils.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author haibara
 * @description 评论服务实现类
 * @since 2025/7/27
 */
@Slf4j
@Service("commentService")
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private LikeService likeService;

    @Resource
    private RedisCache redisCache;

    @Resource
    private LikeMapper likeMapper;

    @Resource
    private PublicService publicService;

    @Resource
    private LeaveWordMapper leaveWordMapper;

    @Value("${spring.mail.username}")
    private String fromUser;

    @Value("${mail.article-email-notice}")
    private Boolean articleEmailNotice;

    @Value("${mail.article-reply-notice}")
    private Boolean articleReplyNotice;

    @Value("${mail.message-email-notice}")
    private Boolean messageEmailNotice;

    @Value("${mail.message-reply-notice}")
    private Boolean messageReplyNotice;


    /**
     * 分页查询评论列表
     * @param type 评论类型
     * @param typeId 关联对象ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页评论列表
     */
    @Override
    public PageVO<List<ArticleCommentVO>> getComment(Integer type, Integer typeId, Integer pageNum, Integer pageSize) {
        // 查询父评论
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .orderByDesc(Comment::getCreateTime)
                .eq(Comment::getType, type)
                .eq(Comment::getTypeId, typeId)
                .eq(Comment::getIsCheck, SQLConst.COMMENT_IS_CHECK)
                .isNull(Comment::getParentId);
        Page<Comment> page = new Page<>(pageNum, pageSize);
        IPage<Comment> commentIPage = commentMapper.selectPage(page, queryWrapper);
        List<Comment> comments = commentIPage.getRecords();
        // 查询所有子评论
        LambdaQueryWrapper<Comment> childQueryWrapper = new LambdaQueryWrapper<>();
        childQueryWrapper
                .orderByDesc(Comment::getCreateTime)
                .eq(Comment::getType, type)
                .eq(Comment::getTypeId, typeId)
                .eq(Comment::getIsCheck, SQLConst.COMMENT_IS_CHECK)
                .isNotNull(Comment::getParentId);
        List<Comment> childComment = commentMapper.selectList(childQueryWrapper);
        // 合并父评论和子评论
        if (!childComment.isEmpty()) comments.addAll(childComment);
        List<ArticleCommentVO> commentsVOS = comments.stream().map(comment -> comment.asViewObject(ArticleCommentVO.class)).toList();
        List<ArticleCommentVO> parentComments = commentsVOS.stream().filter(comment -> comment.getParentId() == null).toList();

        // 批量查询用户信息
        Set<Long> userIds = commentsVOS.stream()
                .flatMap(comment -> Stream.of(comment.getCommentUserId(), comment.getReplyUserId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = userIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(userIds)
                        .stream()
                        .collect(Collectors.toMap(User::getId, user -> user));

        // 批量查询父评论点赞数量
        List<Long> parentCommentIds = parentComments.stream().map(ArticleCommentVO::getId).toList();
        Map<Long, Long> likeCountMap = getCommentLikeCountBatch(parentCommentIds);

        List<ArticleCommentVO> collect = parentComments.stream().peek(comment -> {
            // 从批量查询结果中获取用户信息
            User user = userMap.get(comment.getCommentUserId());
            if (user != null) {
                comment.setCommentUserNickname(user.getNickname())
                        .setCommentUserAvatar(user.getAvatar());
            }
            // 从批量查询结果中获取点赞数
            comment.setLikeCount(likeCountMap.getOrDefault(comment.getId(), 0L));

            comment.setChildComment(getChildComment(commentsVOS, comment.getId()));
            comment.setChildCommentCount(getChildCommentCount(commentsVOS, comment.getId()));
            comment.setParentCommentCount(this.count(new LambdaQueryWrapper<Comment>()
                    .eq(Comment::getType, type)
                    .eq(Comment::getTypeId, typeId)
                    .eq(Comment::getIsCheck, SQLConst.COMMENT_IS_CHECK)
                    .isNull(Comment::getParentId)));
        }).toList();
        // 总评论数量
        LambdaQueryWrapper<Comment> countWrapper = new LambdaQueryWrapper<>();
        countWrapper
                .eq(Comment::getType, type)
                .eq(Comment::getTypeId, typeId)
                .eq(Comment::getIsCheck, SQLConst.COMMENT_IS_CHECK);
        return new PageVO<>(collect, commentMapper.selectCount(countWrapper));
    }

    /**
     * 用户发布评论
     * @param commentDTO 评论数据
     * @return 发布结果
     */
    @Override
    public ResponseResult<String> userComment(UserCommentDTO commentDTO) {
        Comment comment = commentDTO.asViewObject(Comment.class, commentDto -> commentDto.setCommentUserId(SecurityUtils.getUserId()));
        if (this.save(comment)) {
            // 判断用户是否为第三方登录没有邮箱
            User user = userMapper.selectById(SecurityUtils.getUserId());
            if (StringUtils.isEmpty(user.getEmail())) {
                // 提示绑定邮箱
                return ResponseResult.success(SuccessConst.EMAIL_NOT_BANDING);
            }
            return this.commentEmailReminder(commentDTO, user, comment);
        }
        return ResponseResult.failure();
    }


    /**
     * 评论邮箱提醒
     * @param commentDTO 评论数据
     * @param user 用户信息
     * @param comment 评论实体
     * @return 处理结果
     */
    private ResponseResult<String> commentEmailReminder(UserCommentDTO commentDTO, User user, Comment comment) {
        // 缓存评论数量+1
        redisCache.incrementCacheMapValue(RedisConst.ARTICLE_COMMENT_COUNT, commentDTO.getTypeId().toString(), 1);
        // 评论
        if (StringUtils.isNull(commentDTO.getReplyId())) {

            if ((Objects.equals(commentDTO.getType(), CommentTypeEnum.ARTICLE_COMMENT.getType()) && !articleEmailNotice)
                    || Objects.equals(commentDTO.getType(), CommentTypeEnum.LEAVE_WORD_COMMENT.getType()) && !messageEmailNotice)
                return ResponseResult.success();

            Map<String, Object> selectWhereMap = new HashMap<>();
            selectWhereMap.put("commentType", commentDTO.getType());
            selectWhereMap.put("commentId", comment.getId());

            // 留言提示对应发布留言的用户
            if (Objects.equals(commentDTO.getType(), CommentTypeEnum.ARTICLE_COMMENT.getType())) {
                if (Objects.equals(fromUser, user.getEmail())) return ResponseResult.success();
                // 发邮箱给站长
                publicService.sendEmail(MailboxAlertsEnum.COMMENT_NOTIFICATION_EMAIL.getCodeStr(), fromUser, selectWhereMap);
            }

            if (Objects.equals(commentDTO.getType(), CommentTypeEnum.LEAVE_WORD_COMMENT.getType())) {
                // 查出回复的该留言用户的邮箱
                LeaveWord leaveWord = leaveWordMapper.selectOne(new LambdaQueryWrapper<LeaveWord>()
                        .eq(LeaveWord::getId, commentDTO.getTypeId()));
                User replyUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                        .eq(User::getId, leaveWord.getUserId()));
                // 用户没绑定邮箱，或者回复的留言是自己
                if (Objects.equals(replyUser.getEmail(), null) || Objects.equals(replyUser.getEmail(), user.getEmail()))
                    return ResponseResult.success();
                // 发送邮箱给该留言的用户
                publicService.sendEmail(MailboxAlertsEnum.COMMENT_NOTIFICATION_EMAIL.getCodeStr(), replyUser.getEmail(), selectWhereMap);
            }
        }
        // 回复评论
        if (Objects.nonNull(commentDTO.getReplyId())) {
            User replyUser = userMapper.selectById(commentDTO.getReplyUserId());
            if ((Objects.equals(commentDTO.getType(), CommentTypeEnum.ARTICLE_COMMENT.getType()) && !articleReplyNotice)
                    || (Objects.equals(commentDTO.getType(), CommentTypeEnum.LEAVE_WORD_COMMENT.getType()) && !messageReplyNotice))
                return ResponseResult.success();

            // 如果用户回复自己并且回复人是站长就无需提醒
            if (Objects.equals(replyUser.getEmail(), user.getEmail()) && Objects.equals(fromUser, user.getEmail())) {
                return ResponseResult.success();
            }

            Map<String, Object> selectWhereMap = new HashMap<>();
            selectWhereMap.put("commentType", commentDTO.getType());
            selectWhereMap.put("commentId", comment.getId());
            selectWhereMap.put("replyCommentId", commentDTO.getReplyId());

            // 回复人与被回复人不是站长本人的话就发送新增评论邮箱给站长
            if (!Objects.equals(user.getEmail(), fromUser) && !Objects.equals(replyUser.getEmail(), fromUser)) {
                publicService.sendEmail(MailboxAlertsEnum.COMMENT_NOTIFICATION_EMAIL.getCodeStr(), fromUser, selectWhereMap);
            }

            // 回复人不是站长本人并且不是自己回复自己，就发送回复通知
            if (!Objects.equals(user.getEmail(), replyUser.getEmail())) {
                publicService.sendEmail(MailboxAlertsEnum.REPLY_COMMENT_NOTIFICATION_EMAIL.getCodeStr(), replyUser.getEmail(), selectWhereMap);
            }
        }
        return ResponseResult.success();
    }

    /**
     * 获取后台评论列表
     * @param searchDTO 搜索条件
     * @return 评论列表
     */
    @Override
    public List<CommentListVO> getBackCommentList(SearchCommentDTO searchDTO) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotNull(searchDTO)) {
            List<User> users = userMapper
                    .selectList(new LambdaQueryWrapper<User>()
                            .like(User::getUsername, searchDTO.getCommentUserName()));
            if (!users.isEmpty())
                wrapper.in(StringUtils.isNotEmpty(searchDTO.getCommentUserName()),
                        Comment::getCommentUserId, users.stream().map(User::getId).collect(Collectors.toList()));
            else
                wrapper.eq(StringUtils.isNotNull(searchDTO.getCommentUserName()), Comment::getCommentUserId, null);

            wrapper.like(StringUtils.isNotEmpty(searchDTO.getCommentContent()), Comment::getCommentContent, searchDTO.getCommentContent())
                    .eq(StringUtils.isNotNull(searchDTO.getType()), Comment::getType, searchDTO.getType())
                    .eq(StringUtils.isNotNull(searchDTO.getIsCheck()), Comment::getIsCheck, searchDTO.getIsCheck());
        }

        List<Comment> comments = commentMapper.selectList(wrapper.orderByDesc(Comment::getCreateTime));

        if (comments.isEmpty()) {
            return List.of();
        }

        // 批量查询用户信息
        Set<Long> userIds = comments.stream().map(Comment::getCommentUserId).collect(Collectors.toSet());
        Map<Long, String> userMap = userMapper.selectBatchIds(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        return comments.stream()
                .map(comment -> comment.asViewObject(CommentListVO.class,
                v -> v.setCommentUserName(userMap.get(comment.getCommentUserId()))))
                .collect(Collectors.toList());

    }

    /**
     * 审核评论
     * @param isCheckDTO 审核状态数据
     * @return 操作结果
     */
    @Override
    public ResponseResult<Void> isCheckComment(CommentIsCheckDTO isCheckDTO) {
        LambdaUpdateWrapper<Comment> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Comment::getId, isCheckDTO.getId())
                .or()
                .eq(Comment::getParentId, isCheckDTO.getId());
        int updateCount = commentMapper.update(Comment.builder().id(isCheckDTO.getId()).isCheck(isCheckDTO.getIsCheck()).build(), wrapper);
        if (updateCount > 0) {
            // 同步redis评论数量
            // 如果是文章评论，则改变redis中文章数量
            // 1.查询评论所在的文章id
            Integer articleId = commentMapper
                    .selectOne(
                            new LambdaQueryWrapper<Comment>()
                                    .eq(Comment::getId, isCheckDTO.getId())
                                    .eq(Comment::getType, CommentEnum.COMMENT_TYPE_ARTICLE.getType())).getTypeId();
            // 2.修改redis数量
            if (Objects.equals(isCheckDTO.getIsCheck(), SQLConst.COMMENT_IS_CHECK)) {
                redisCache.incrementCacheMapValue(RedisConst.ARTICLE_COMMENT_COUNT, articleId.toString(), updateCount);
            } else {
                redisCache.incrementCacheMapValue(RedisConst.ARTICLE_COMMENT_COUNT, articleId.toString(), -updateCount);
            }
            return ResponseResult.success();
        }

        return ResponseResult.failure();
    }

    /**
     * 删除评论
     * @param id 评论ID
     * @return 操作结果
     */
    @Override
    public ResponseResult<Void> deleteComment(Long id) {
        // 是否还有子评论
        if (commentMapper.selectCount(new LambdaQueryWrapper<Comment>().eq(Comment::getParentId, id)) > 0) {
            return ResponseResult.failure(ErrorConst.COMMENT_HAS_REPLY);
        }

        // 先获取评论信息，用于更新缓存
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            return ResponseResult.failure();
        }

        if (commentMapper.deleteById(id) > 0) {
            // 删除评论的点赞
            likeMapper.delete(new LambdaQueryWrapper<Like>().eq(Like::getType, LikeEnum.LIKE_TYPE_COMMENT.getType()).and(a -> a.in(Like::getTypeId, id)));

            // 如果是文章评论且已审核通过，需要减少文章评论数缓存
            if (Objects.equals(comment.getType(), CommentEnum.COMMENT_TYPE_ARTICLE.getType())
                && Objects.equals(comment.getIsCheck(), SQLConst.COMMENT_IS_CHECK)) {
                redisCache.incrementCacheMapValue(RedisConst.ARTICLE_COMMENT_COUNT, comment.getTypeId().toString(), -1);
                log.debug("删除文章评论成功，已更新评论数缓存，文章ID: {}", comment.getTypeId());
            }

            return ResponseResult.success();
        }
        return ResponseResult.failure();
    }


    /**
     * 获取子评论列表
     * @param comments 所有评论列表
     * @param parentId 父评论ID
     * @return 子评论列表
     */
    private List<ArticleCommentVO> getChildComment(List<ArticleCommentVO> comments, Long parentId) {
        // 1. 批量获取所有用户信息，避免N+1查询
        Set<Long> userIds = comments.stream()
                .flatMap(comment -> Stream.of(comment.getCommentUserId(), comment.getReplyUserId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = userMapper.selectBatchIds(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 2. 按parentId分组，避免每次递归都遍历全量数据
        Map<Long, List<ArticleCommentVO>> commentsByParent = comments.stream()
                .filter(comment -> Objects.nonNull(comment.getParentId()))
                .collect(Collectors.groupingBy(ArticleCommentVO::getParentId));

        // 3. 递归构建子评论树
        return buildChildComments(commentsByParent, userMap, parentId);
    }

    /**
     * 递归构建子评论树
     * @param commentsByParent 按父评论ID分组的评论映射
     * @param userMap 用户信息映射
     * @param parentId 父评论ID
     * @return 子评论列表
     */
    private List<ArticleCommentVO> buildChildComments(Map<Long, List<ArticleCommentVO>> commentsByParent,
                                                      Map<Long, User> userMap, Long parentId) {
        List<ArticleCommentVO> children = commentsByParent.get(parentId);
        if (children == null || children.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询子评论的点赞数量
        List<Long> childCommentIds = children.stream().map(ArticleCommentVO::getId).toList();
        Map<Long, Long> childLikeCountMap = getCommentLikeCountBatch(childCommentIds);

        return children.stream()
                .peek(comment -> {
                    // 设置用户信息
                    User user = userMap.get(comment.getCommentUserId());
                    if (user != null) {
                        comment.setCommentUserNickname(user.getNickname())
                                .setCommentUserAvatar(user.getAvatar());
                    }

                    User replyUser = userMap.get(comment.getReplyUserId());
                    if (replyUser != null) {
                        comment.setReplyUserNickname(replyUser.getNickname());
                    }

                    // 从批量查询结果中获取点赞数
                    comment.setLikeCount(childLikeCountMap.getOrDefault(comment.getId(), 0L));

                    // 递归设置子评论
                    comment.setChildComment(buildChildComments(commentsByParent, userMap, comment.getId()));
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取父评论底下的评论数量
     * @param comments 评论列表
     * @param parentId 父评论ID
     * @return 子评论数量
     */
    private Long getChildCommentCount(List<ArticleCommentVO> comments, Long parentId) {
        // 递归获取父评论的子评论数
        return comments.stream()
                .filter(comment -> Objects.nonNull(comment.getParentId()) && Objects.equals(comment.getParentId(), parentId))
                .peek(comment -> {
                    // 回复子评论的数量
                    Long count = commentMapper.selectCount(new LambdaQueryWrapper<Comment>()
                            .eq(Comment::getReplyId, comment.getId())
                            .eq(Comment::getIsCheck, SQLConst.COMMENT_IS_CHECK));
                    comment.setChildCommentCount(count);
                })
                .mapToLong(comment -> {
                    if (!comment.getChildComment().isEmpty()) {
                        return (1 + getChildCommentCount(comment.getChildComment(), comment.getId()));
                    } else {
                        return 1;
                    }
                })
                .sum();
    }

    /**
     * 批量查询评论点赞数量
     * @param commentIds 评论ID列表
     * @return 评论ID到点赞数量的映射
     */
    private Map<Long, Long> getCommentLikeCountBatch(List<Long> commentIds) {
        if (commentIds.isEmpty()) return Map.of();

        return likeMapper.selectList(new LambdaQueryWrapper<Like>()
                .eq(Like::getType, LikeEnum.LIKE_TYPE_COMMENT.getType())
                .in(Like::getTypeId, commentIds))
                .stream()
                .collect(Collectors.groupingBy(like -> Long.valueOf(like.getTypeId()), Collectors.counting()));
    }
}
