package com.blog.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.constants.*;
import com.blog.domain.dto.*;
import com.blog.domain.entity.*;
import com.blog.domain.response.ResponseResult;
import com.blog.domain.vo.UserAccountVO;
import com.blog.domain.vo.UserDetailsVO;
import com.blog.domain.vo.UserListVO;
import com.blog.enums.*;
import com.blog.mapper.*;
import com.blog.service.IpService;
import com.blog.service.UserService;
import com.blog.utils.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author haibara
 * @description 用户服务实现类
 * @since 2025/7/27
 */
@Slf4j
@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private RolePermissionMapper rolePermissionMapper;

    @Resource
    private PermissionMapper permissionMapper;

    @Resource
    private RedisCache redisCache;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private LikeMapper likeMapper;

    @Resource
    private FavoriteMapper favoriteMapper;

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private TreeHoleMapper treeHoleMapper;

    @Resource
    private LeaveWordMapper leaveWordMapper;

    @Resource
    private LinkMapper linkMapper;

    @Resource
    private IpService ipService;

    @Resource
    private FileUploadUtils fileUploadUtils;

    /**
     * 根据用户名加载用户详情（Spring Security接口实现）
     *
     * @param username 用户名或邮箱
     * @return 用户详情对象
     * @throws UsernameNotFoundException 用户不存在异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        HttpServletRequest request = SecurityUtils.getCurrentHttpRequest();
        String equipmentHeader = null;
        String typeHeader = null;
        String accessToken = null;
        if(request != null){
            equipmentHeader = request.getHeader(Const.TYPE_HEADER);
            typeHeader = request.getHeader(Const.FRONTEND_LOGIN_TYPE);
            accessToken = request.getHeader(Const.FRONTEND_THIRD_LOGIN_TOKEN);
        }

        User user = null;

        // 首先判断是不是第三方登录
        if(typeHeader != null){

            // gitee
            if(typeHeader.equals(RegisterOrLoginTypeEnum.GITEE.getStrategy())) {
                String result = HttpUtils.sendGet(UrlEnum.GITEE_USER_INFO.getUrl(), "access_token=" + accessToken);
                JSONObject jsonObject = JSON.parseObject(result);
                Integer uuid = (Integer) jsonObject.get(SQLConst.ID);
                user = userMapper.selectById(uuid);
            }

            // github
            if (typeHeader.equals(RegisterOrLoginTypeEnum.GITHUB.getStrategy())) {
                OkHttpClient client = new OkHttpClient();
                Headers headers = new Headers.Builder()
                        .add(RequestHeaderEnum.GITHUB_USER_INFO.getHeader(), RequestHeaderEnum.GITHUB_USER_INFO.getContent())
                        .add(RespConst.TOKEN_HEADER, RespConst.TOKEN_PREFIX + accessToken)
                        .build();
                Request getRequest = new Request.Builder()
                        .url(UrlEnum.GITHUB_USER_INFO.getUrl())
                        .method(UrlEnum.GITHUB_USER_INFO.getMethod(), null)
                        .headers(headers)
                        .build();
                try (Response response = client.newCall(getRequest).execute()) {
                    JSONObject jsonObject;
                    if (response.body() != null) {
                        jsonObject = JSON.parseObject(response.body().string());
                        Integer uuid = (Integer) jsonObject.get(SQLConst.ID);
                        user = userMapper.selectById(uuid);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }else{
            user = findAccountByNameOrEmail(username);
        }

        // 2. 判断用户是否存在
        if (ObjectUtils.isEmpty(user)) {
            // 不存在，抛出异常
            throw new UsernameNotFoundException(RespConst.USERNAME_OR_PASSWORD_ERROR_MSG);
        }
        return handlerLogin(user, equipmentHeader);
    }

    /**
     * 根据用户名或邮箱查找用户
     *
     * @param text 用户名或邮箱
     * @return 用户实体
     */
    private User findAccountByNameOrEmail(String text) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, text).or().eq(User::getEmail, text).eq(User::getRegisterType, RegisterOrLoginTypeEnum.EMAIL.getRegisterType());
        return userMapper.selectOne(wrapper);
    }

    /**
     * 处理用户登录逻辑
     *
     * @param user 用户实体
     * @param equipmentHeader 设备头信息
     * @return 登录用户对象
     */
    private LoginUser handlerLogin(User user, String equipmentHeader) {
        HttpServletRequest request = SecurityUtils.getCurrentHttpRequest();
        String header = null;
        if (request != null) {
            header = request.getHeader(Const.TYPE_HEADER);
        }
        // 查询用户角色
        List<UserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, user.getId()));
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).toList();
        List<Role> roles = roleIds.isEmpty() ? List.of() :
                roleMapper.selectBatchIds(roleIds)
                        .stream()
                        .filter(role -> Objects.equals(role.getStatus(), RoleEnum.ROLE_STATUS_NORMAL.getValue()))
                        .toList();
        // 用户是否被禁用
        if (Objects.equals(user.getIsDisable(), RoleEnum.ROLE_STATUS_DEACTIVATE.getValue())) {
            throw new BadCredentialsException(RespConst.ACCOUNT_DISABLED_MSG);
        }
        // 是否测试账号前台
        if (header == null || (roles.stream().anyMatch(role -> role.getRoleKey().equals(SecurityConst.ROLE_TESTER)) && !header.equals(Const.BACKEND_REQUEST))) {
            throw new BadCredentialsException(RespConst.TEST_ACCOUNT_MSG);
        }

        // 判断用户是否具备任何权限,
        if ((equipmentHeader != null && equipmentHeader.equals(Const.BACKEND_REQUEST) && ObjectUtils.isEmpty(roles))) {
            throw new BadCredentialsException(RespConst.NO_PERMISSION_MSG);
        }
        if (!roles.isEmpty()) {
            // 查询权限关系表
            List<RolePermission> rolePermissions = rolePermissionMapper.selectBatchIds(roles.stream().map(Role::getId).toList());
            // 查询角色权限
            List<Long> pIds = rolePermissions.stream().map(RolePermission::getPermissionId).toList();
            List<Permission> permissions = permissionMapper.selectBatchIds(pIds);
            // 组合角色，权限
            List<String> list = permissions.stream().map(Permission::getPermissionKey).collect(Collectors.toList());
            roles.forEach(role -> list.add(SecurityConst.ROLE_PREFIX + role.getRoleKey()));
            return new LoginUser(user, list);
        }
        return new LoginUser(user, List.of());
    }

    /**
     * 更新用户登录状态
     *
     * @param id 用户ID
     * @param type 登录类型
     */
    @Override
    public void userLoginStatus(Long id, Integer type) {
        String ipAddr = IpUtils.getIpAddr(SecurityUtils.getCurrentHttpRequest());
        if(IpUtils.isUnknown(ipAddr)){
            ipAddr = IpUtils.getHostIp();
        }

        User user = User.builder()
                .id(id)
                .loginTime(new Date())
                .loginType(type)
                .loginIp(ipAddr)
                .build();
        if (updateById(user)) {
            ipService.refreshIpDetailAsyncByUidAndLogin(user.getId());
        }
    }

    /**
     * 根据用户ID查找用户账户信息
     *
     * @param userId 用户ID
     * @return 用户账户视图对象
     */
    @Override
    public UserAccountVO findAccountById(Long userId) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getId, userId));
        // 通过SpringSecurity上下文拿到当前用户的角色
        List<String> userRoles = SecurityUtils.getUserRoles();
        // 角色身份
        List<String> roles = new ArrayList<>();
        // 权限
        List<String> permissions = new ArrayList<>();

        userRoles.forEach(role -> {
            if (role.startsWith(SecurityConst.ROLE_PREFIX)) {
                // 去掉前缀，添加
                roles.add(role.substring(SecurityConst.ROLE_PREFIX.length()));
            } else {
                permissions.add(role);
            }
        });

        return user.asViewObject(UserAccountVO.class, role -> {
            role.setRoles(roles);
            role.setPermissions(permissions);
        });
    }

    /**
     * 更新用户信息
     *
     * @param userUpdateDTO 用户更新数据传输对象
     * @return 操作结果
     */
    @Override
    public ResponseResult<Void> updateUser(UserUpdateDTO userUpdateDTO) {
        Long userId = SecurityUtils.getUserId();
        User user = userUpdateDTO.asViewObject(User.class, v -> v.setId(userId));
        if (this.updateById(user)) {
            return ResponseResult.success();
        }
        return ResponseResult.failure();
    }

    /**
     * 上传用户头像
     *
     * @param avatarFile 头像文件
     * @return 上传结果，包含头像URL
     * @throws Exception 上传异常
     */
    @Override
    public ResponseResult<String> uploadAvatar(MultipartFile avatarFile) throws Exception {
        String upload = fileUploadUtils.upload(UploadEnum.USER_AVATAR, avatarFile);
        return ResponseResult.success(upload);
    }

    /**
     * 更新邮箱并验证
     *
     * @param updateEmailDTO 更新邮箱数据传输对象
     * @return 操作结果
     */
    @Override
    public ResponseResult<Void> updateEmailAndVerify(UpdateEmailDTO updateEmailDTO) {
        // 1.验证密码是否正确
        User user = userMapper.selectById(SecurityUtils.getUserId());
        // 邮箱是否改变
        if (user.getEmail().equals(updateEmailDTO.getEmail())) return ResponseResult.failure(ErrorConst.EMAIL_NOT_CHANGE);
        // 是否已经存在该邮箱
        if (userIsExist(null, updateEmailDTO.getEmail())) return ResponseResult.failure(ErrorConst.EMAIL_HAS_REGISTERED);
        if (bCryptPasswordEncoder.matches(updateEmailDTO.getPassword(), user.getPassword())) {
            // 2.验证码是否正确
            ResponseResult<Void> verifyCode = verifyCode(updateEmailDTO.getEmail(), updateEmailDTO.getCode(), RedisConst.RESET_EMAIL);
            if (verifyCode == null){
                // 3.修改
                user.setEmail(updateEmailDTO.getEmail());
                userMapper.updateById(user);
                return ResponseResult.success();
            }
        }
        return ResponseResult.failure(ErrorConst.WRONG_PASSWORD_OR_CODE);
    }

    /**
     * 第三方用户更新邮箱
     *
     * @param updateEmailDTO 更新邮箱数据传输对象
     * @return 操作结果
     */
    @Override
    public ResponseResult<Void> thirdUpdateEmail(UpdateEmailDTO updateEmailDTO) {
        // 1.验证密码是否正确
        User user = userMapper.selectById(SecurityUtils.getUserId());
        // 邮箱是否改变
        if (user.getEmail() != null && user.getEmail().equals(updateEmailDTO.getEmail())) return ResponseResult.failure(ErrorConst.EMAIL_NOT_CHANGE);
        // 2.验证码是否正确
        ResponseResult<Void> verifyCode = verifyCode(updateEmailDTO.getEmail(), updateEmailDTO.getCode(), RedisConst.RESET_EMAIL);
        // 是否已经存在该邮箱
        if (userIsExist(null, updateEmailDTO.getEmail())) return ResponseResult.failure(ErrorConst.EMAIL_HAS_REGISTERED);
        if (verifyCode == null){
            // 3.修改
            user.setEmail(updateEmailDTO.getEmail());
            userMapper.updateById(user);
            return ResponseResult.success();
        }
        return ResponseResult.failure(ErrorConst.WRONG_PASSWORD_OR_CODE);
    }

    /**
     * 用户注册
     *
     * @param userRegisterDTO 用户注册数据传输对象
     * @return 注册结果
     */
    @Override
    public ResponseResult<Void> userRegister(UserRegisterDTO userRegisterDTO) {
        // 1.验证验证码是否正确
        ResponseResult<Void> verifyCodeResult =
                verifyCode(userRegisterDTO.getEmail(), userRegisterDTO.getCode(), RedisConst.REGISTER);

        if(verifyCodeResult != null) return verifyCodeResult;

        // 2.判断用户名和邮箱是否已经存在
        boolean isExist = userIsExist(userRegisterDTO.getUsername(), userRegisterDTO.getEmail());
        if(isExist){
            return ResponseResult.failure(RespEnum.USERNAME_OR_EMAIL_EXIST.getCode(), RespEnum.USERNAME_OR_EMAIL_EXIST.getMsg());
        }

        // 3.密码加密
        String encodePwd = passwordEncoder.encode(userRegisterDTO.getPassword());
        Date date = new Date();

        // 4.获取注册的ip地址
        String ipAddr = IpUtils.getIpAddr(SecurityUtils.getCurrentHttpRequest());
        if (IpUtils.isUnknown(ipAddr)) {
            ipAddr = IpUtils.getHostIp();
        }

        // 5.保存用户信息
        User user = User.builder()
                .id(null)
                .nickname(userRegisterDTO.getUsername())
                .username(userRegisterDTO.getUsername())
                .password(encodePwd)
                .registerType(RegisterOrLoginTypeEnum.EMAIL.getRegisterType())
                .registerIp(ipAddr)
                .gender(UserConst.DEFAULT_GENDER)
                .avatar(UserConst.DEFAULT_AVATAR)
                .intro(UserConst.DEFAULT_INTRODUCTION)
                .registerType(RegisterOrLoginTypeEnum.EMAIL.getRegisterType())
                .isDeleted(UserConst.DEFAULT_STATUS)
                .email(userRegisterDTO.getEmail())
                .loginTime(date).build();

        // 6.成功保存用户信息后删除验证码缓存
        if (this.save(user)) {
            ipService.refreshIpDetailAsyncByUidAndRegister(user.getId());
            redisCache.deleteObject(RedisConst.VERIFY_CODE + RedisConst.REGISTER + RedisConst.SEPARATOR + userRegisterDTO.getEmail());
            return ResponseResult.success();
        } else {
            return ResponseResult.failure();
        }
    }

    /**
     * 用户重置密码确认
     *
     * @param userResetDTO 用户重置确认数据传输对象
     * @return 确认结果
     */
    @Override
    public ResponseResult<Void> userResetConfirm(UserResetConfirmDTO userResetDTO) {
        // 判断验证码是否正确
        ResponseResult<Void> verifyCode = verifyCode(userResetDTO.getEmail(), userResetDTO.getCode(), RedisConst.RESET);
        if (verifyCode != null) return verifyCode;
        return ResponseResult.success();
    }

    /**
     * 用户重置密码
     *
     * @param userResetDTO 用户重置密码数据传输对象
     * @return 重置结果
     */
    @Override
    public ResponseResult<Void> userResetPassword(UserResetPasswordDTO userResetDTO) {
        // 校验验证码
        ResponseResult<Void> verifyCode = verifyCode(userResetDTO.getEmail(), userResetDTO.getCode(), RedisConst.RESET);
        if (verifyCode != null) return verifyCode;

        // 加密新密码
        String password = passwordEncoder.encode(userResetDTO.getPassword());
        User user = User.builder().password(password).build();
        if (this.update(user, new LambdaQueryWrapper<User>().eq(User::getEmail, userResetDTO.getEmail()))) {
            // 删除验证码
            redisCache.deleteObject(RedisConst.VERIFY_CODE + RedisConst.RESET + RedisConst.SEPARATOR + userResetDTO.getEmail());
            return ResponseResult.success();
        } else {
            return ResponseResult.failure();
        }
    }

    /**
     * 获取用户列表或搜索用户
     *
     * @param userSearchDTO 用户搜索条件
     * @return 用户列表
     */
    @Override
    public List<UserListVO> getUserOrSearch(UserSearchDTO userSearchDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotNull(userSearchDTO)) {
            wrapper.like(StringUtils.isNotEmpty(userSearchDTO.getUsername()), User::getUsername, userSearchDTO.getUsername())
                    .like(StringUtils.isNotEmpty(userSearchDTO.getEmail()), User::getEmail, userSearchDTO.getEmail())
                    .eq(StringUtils.isNotNull(userSearchDTO.getIsDisable()), User::getIsDisable, userSearchDTO.getIsDisable());
            if (StringUtils.isNotNull(userSearchDTO.getCreateTimeStart()) && StringUtils.isNotNull(userSearchDTO.getCreateTimeEnd())) {
                wrapper.gt(User::getCreateTime, userSearchDTO.getCreateTimeStart()).and(a -> a.lt(User::getCreateTime, userSearchDTO.getCreateTimeEnd()));
            }
        }
        return userMapper.selectList(wrapper).stream().map(user -> user.asViewObject(UserListVO.class)).toList();
    }

    /**
     * 更新用户状态
     *
     * @param id 用户ID
     * @param status 用户状态（0-启用，1-禁用）
     * @return 更新结果
     */
    @Override
    public ResponseResult<Void> updateStatus(Long id, Integer status) {
        User user = User.builder().isDisable(status).id(id).build();
        // 更新状态
        if (userMapper.updateById(user) > 0) {
            return ResponseResult.success();
        }
        return ResponseResult.failure();
    }

    /**
     * 根据ID查找用户详细信息
     *
     * @param id 用户ID
     * @return 用户详细信息视图对象
     */
    @Override
    public UserDetailsVO findUserDetailsById(Long id) {
        User user = userMapper.selectById(id);
        if (StringUtils.isNotNull(user)) {
            // 用户角色
            List<UserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
            List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).toList();
            if (!roleIds.isEmpty()) {
                List<Role> roles = roleMapper.selectBatchIds(roleIds);
                return user.asViewObject(UserDetailsVO.class, v -> v.setRoles(roles.stream().map(Role::getRoleName).toList()));
            } else {
                return user.asViewObject(UserDetailsVO.class);
            }
        }
        return null;
    }

    /**
     * 删除用户
     *
     * @param ids 用户ID列表
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResponseResult<Void> deleteUser(List<Long> ids) {
        // 删除用户
        if (removeBatchByIds(ids)) {
            // 删除用户角色关系
            userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().in(UserRole::getUserId, ids));
            // 删除用户评论、点赞、收藏
            commentMapper.delete(new LambdaQueryWrapper<Comment>().in(Comment::getCommentUserId, ids).or(a -> a.in(Comment::getReplyUserId, ids)));
            likeMapper.delete(new LambdaQueryWrapper<Like>().in(Like::getUserId, ids));
            favoriteMapper.delete(new LambdaQueryWrapper<Favorite>().in(Favorite::getUserId, ids));
            // 删除用户文章
            articleMapper.delete(new LambdaQueryWrapper<Article>().in(Article::getUserId, ids));
            // 删除用户树洞
            treeHoleMapper.delete(new LambdaQueryWrapper<TreeHole>().in(TreeHole::getUserId, ids));
            // 删除用户留言
            leaveWordMapper.delete(new LambdaQueryWrapper<LeaveWord>().in(LeaveWord::getUserId, ids));
            // 删除用户友链
            linkMapper.delete(new LambdaQueryWrapper<Link>().in(Link::getUserId, ids));
            return ResponseResult.success();
        }
        return ResponseResult.failure();
    }


    /**
     * 判断用户名或邮箱是否已存在
     *
     * @param username 用户名
     * @param email    邮箱
     * @return boolean
     */
    private boolean userIsExist(String username, String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username).or().eq(User::getEmail, email);
        return this.userMapper.selectOne(wrapper) != null;
    }

    /**
     * 判断验证码是否正确
     * @param email 邮箱
     * @param code 验证码
     * @param type 验证码的用途(如注册)
     * @return 统一的响应格式
     */
    private ResponseResult<Void> verifyCode(String email, String code, String type) {
        String redisCode = redisCache.getCacheObject(RedisConst.VERIFY_CODE + type + RedisConst.SEPARATOR + email);
        if (redisCode == null)
            return ResponseResult.failure(RespEnum.VERIFY_CODE_ERROR.getCode(), RespConst.VERIFY_CODE_NULL_MSG);
        if (!redisCode.equals(code))
            return ResponseResult.failure(RespEnum.VERIFY_CODE_ERROR.getCode(), RespEnum.VERIFY_CODE_ERROR.getMsg());
        return null;
    }
}
