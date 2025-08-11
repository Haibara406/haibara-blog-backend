package com.blog.constants;

import org.springframework.security.core.parameters.P;

/**
 * @author haibara
 * @description 参数验证信息常量
 * @since 2025/7/27 22:21
 */
public class ValidationConstants {

    public static final String USER_ID_NOT_NULL = "用户ID不能为空";

    public static final String BAN_REASON_NOT_NULL = "封禁原因不能为空";

    public static final String BAN_EXPIRE_TIME_NOT_NULL = "封禁到期时间不能为空";

    public static final String BAN_EXPIRE_TIME_MUST_BE_FUTURE = "封禁到期时间必须在当前时间之后";

    public static final String CODE_NOT_NULL = "验证码不能为空";

    public static final String EMAIL_WRONG_FORMAT = "邮箱格式不正确";

    public static final String EMAIL_WRONG_MESSAGE = "邮箱格式不符合规范,请提供一个有效的邮箱地址";

    public static final String PASSWORD_NOT_NULL = "密码不能为空";

    public static final String USER_NAME_WRONG_FORMAT = "用户名格式不正确，最少2字符，最多32字符，只能包含英文大小写，数字，以及中文字符";

    public static final String PASSWORD_NOT_STRONG = "密码强度不足,请包含大小写字母、数字和特殊字符，且长度为8-24位";

    public static final String ROLE_STATUS_VALUE_WRONG = "用户状态值不正确,只能为0或1";

    public static final String LOGIN_LOG_ID_NOT_NULL = "登录日志id不能为空";

    public static final String LOG_ID_NOT_NULL = "操作日志id不能为空";

    public static final String PERMISSION_ID_NOT_NULL = "权限id不能为空";

    public static final String ROLE_ID_NOT_NULL = "角色id不能为空";

    public static final String ROLE_NAME_NOT_NULL = "角色名称不能为空";

    public static final String ROLE_KEY_NOT_NULL = "角色标识不能为空";

    public static final String ORDER_NUM_NOT_NULL = "排序不能为空";

    public static final String STATUS_NOT_NULL = "状态不能为空";

    public static final String CURRENT_PAGE_NOT_NULL = "当前页不能为空";

    public static final String PAGE_SIZE_NOT_NULL = "每页条数不能为空";

    public static final String USER_NICKNAME_NOT_NULL = "用户昵称不能为空";

    public static final String USER_GENDER_NOT_NULL = "用户性别不能为空";

    public static final String USER_INTRO_NOT_NULL = "用户简介不能为空";

    public static final String USER_AVATAR_NOT_NULL = "用户头像不能为空";

    public static final String PERM_DESC_NOT_NULL = "权限描述不能为空";

    public static final String PERM_KEY_NOT_NULL = "权限字符不能为空";

    public static final String PERM_MENU_ID_NOT_NULL = "权限菜单不能为空";

    public static final String STATION_MASTER_NAME_IS_TOO_LONG = "站长名称字数不能超过30";

    public static final String STATION_MASTER_COPY_IS_TOO_LONG = "站长文案字数不能超过100";

    public static final String GITEE_LINK_IS_TOO_LONG = "gitee链接字数不能超过100";

    public static final String GITHUB_LINK_IS_TOO_LONG = "github链接字数不能超过100";

    public static final String WEBSITE_NAME_IS_TOO_LONG = "网站名称字数不能超过30";

    public static final String HEADER_NOTIFICATION_IS_TOO_LONG = "头部通知字数不能超过100";

    public static final String SIDEBAR_ANNOUNCEMENT_IS_TOO_LONG = "侧面公告字数不能超过1000";

    public static final String RECORD_INFO_IS_TOO_LONG = "备案信息字数不能超过100";

    public static final String CATEGORY_ID_NOT_NULL = "分类id不能为空";

    public static final String TAG_ID_NOT_NULL = "标签id不能为空";

    public static final String ARTICLE_COVER_NOT_NULL = "文章缩略图不能为空";

    public static final String ARTICLE_TITLE_NOT_NULL = "文章标题不能为空";

    public static final String ARTICLE_CONTENT_NOT_NULL = "文章内容不能为空";

    public static final String ARTICLE_TYPE_NOT_NULL = "文章类型不能为空";

    public static final String ARTICLE_CONTENT_SEARCH_TOO_LONG = "文章搜索长度应在1-15之间";

    public static final String IS_TOP_NOT_NULL = "是否置顶不能为空";

    public static final String ARTICLE_STATUS_NOT_NULL = "文章状态不能为空";

    public static final String GATEGORY_NAME_NOT_NULL = "分类名称不能为空";

    public static final String GATEGORY_NAME_IS_TOO_LONG = "分类名称字数不能超过20";

    public static final String TAG_NAME_NOT_NULL = "标签名称不能为空";

    public static final String TAG_NAME_IS_TOO_LONG = "标签名称长度不能超过20";

    public static final String TREE_HOLE_ID_NOT_NULL = "树洞id不能为空";

    public static final String TREE_HOLE_IS_CHECK_NOT_NULL = "树洞是否通过不能为空";

    public static final String LEAVE_WORD_ID_NOT_NULL = "留言id不能为空";

    public static final String LEAVE_WORD_IS_CHECK_NOT_NULL = "留言是否通过不能为空";

    public static final String COMMENT_TYPE_NOT_NULL = "评论类型不能为空";

    public static final String COMMENT_TYPE_ID_NOT_NULL = "类型id不能为空";

    public static final String COMMENT_CONTENT_NOT_NULL = "评论内容不能为空";

    public static final String COMMENT_ID_NOT_NULL = "评论id不能为空";

    public static final String COMMENT_IS_CHECK_NOT_NULL = "是否通过不能为空";

    public static final String FAVORITE_ID_NOT_NULL = "收藏id不能为空";

    public static final String FAVORITE_IS_CHECK_NOT_NULL = "收藏是否通过不能为空";

    public static final String WEBSITE_NAME_TOO_LONG = "网站名字不能超过15个字";

    public static final String WEBSITE_URL_TOO_LONG = "网站url不能超过50个字符";

    public static final String WEBSITE_DESCRIPTION_TOO_LONG = "网站描述不能超过30个字符";

    public static final String WEBSITE_BACKGROUND_TOO_LONG = "网站背景不能超过200个字符";

    public static final String LINK_ID_NOT_NULL = "友链id不能为空";

    public static final String LINK_IS_CHECK_NOT_NULL = "是否通过不能为空";

    public static final String VERIFY_CODE_NOT_NULL = "校验码不能为空";

    public static final String ALBUM_NAME_NOT_NULL = "相册名称不能为空";

    public static final String ALBUM_NAME_IS_TOO_LONG = "相册名称字数不能超过20";

    public static final String ALBUM_OR_PHOTO_ID_NOT_NULL = "相册或照片id不能为空";

    public static final String TYPE_ERROR = "参数错误";

}
