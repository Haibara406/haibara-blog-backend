package com.blog.controller;

import com.blog.annotation.AccessLimit;
import com.blog.constants.AccessLimitConst;
import com.blog.domain.request.oauth.GiteeBody;
import com.blog.domain.request.oauth.GithubBody;
import com.blog.enums.RegisterOrLoginTypeEnum;
import com.blog.service.OauthService;
import com.xkcoding.http.config.HttpConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.request.AuthGiteeRequest;
import me.zhyd.oauth.request.AuthGithubRequest;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 第三方OAuth登录控制器
 *
 * @author haibara
 * @description 第三方登录接口
 * @since 2025/7/28 16:52
 */

@Slf4j
@Tag(name = "第三方登录")
@RestController
@RequestMapping("/oauth")
@Validated
public class OauthController {


    @Resource
    private GiteeBody giteeBody;

    @Resource
    private GithubBody githubBody;

    @Resource
    private OauthService oauthService;

    @Value("${web.index.path}")
    private String path;

    /**
     * 发起Gitee OAuth登录授权
     * <p>
     * 构建Gitee OAuth授权URL并重定向用户到Gitee授权页面。
     * 用户在Gitee完成授权后，会被重定向回系统的回调地址。
     * 该接口是Gitee登录流程的第一步。
     *
     * @param response HTTP响应对象，用于执行重定向操作
     *                <ul>
     *                    <li>会自动重定向到Gitee的OAuth授权页面</li>
     *                    <li>授权页面会显示应用信息和权限范围</li>
     *                </ul>
     * @throws IOException 当重定向操作失败时抛出IO异常
     * @see #getGiteeAuthRequest() 获取Gitee授权请求对象
     * @see AuthStateUtils#createState() 生成防CSRF攻击的state参数
     */
    @Operation(summary = "Gitee登录")
    @AccessLimit(seconds = AccessLimitConst.DEFAULT_SECONDS,
            maxCount = AccessLimitConst.GITEE_OR_GITHUB_LOGIN_MAX_COUNT)
    @Parameters({
            @Parameter(name = "response", hidden = true, required = true)
    })
    @GetMapping("/gitee/render")
    public void giteeRenderAuth(HttpServletResponse response) throws IOException {
        AuthRequest authRequest = getGiteeAuthRequest();
        response.sendRedirect(authRequest.authorize(AuthStateUtils.createState()));
    }

    /**
     * 处理Gitee OAuth登录回调
     * <p>
     * 接收Gitee OAuth授权回调，处理授权码并完成用户登录流程。
     * 该接口会获取用户信息、创建或更新用户账户，并生成登录凭证。
     * 处理完成后会重定向到前端页面并携带登录参数。
     *
     * @param callback OAuth授权回调信息，包含：
     *                <ul>
     *                    <li>code: Gitee返回的临时授权码，用于换取访问令牌</li>
     *                    <li>state: 防CSRF攻击的状态值，需要与发起授权时的值匹配</li>
     *                </ul>
     * @param request HTTP请求对象，用于获取客户端IP等信息
     * @param response HTTP响应对象，用于重定向到前端页面
     * @throws IOException 当重定向操作失败时抛出IO异常
     * @see RegisterOrLoginTypeEnum#GITEE Gitee登录类型枚举
     */
    @Operation(summary = "Gitee登录回调")
    @AccessLimit(seconds = AccessLimitConst.DEFAULT_SECONDS,
            maxCount = AccessLimitConst.GITEE_OR_GITHUB_LOGIN_MAX_COUNT)
    @Parameters({
            @Parameter(name = "code", description = "Gitee 授权成功后回调的临时授权码", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "state", description = "防 CSRF 的 state 值", required = true, in = ParameterIn.QUERY)
    })
    @GetMapping("/gitee/callback")
    public void giteeLogin(AuthCallback callback, HttpServletRequest request, HttpServletResponse response) throws IOException {
        AuthRequest authRequest = getGiteeAuthRequest();
        String parameter = oauthService.handleLogin(authRequest.login(callback), request, RegisterOrLoginTypeEnum.GITEE.getRegisterType());
        response.sendRedirect(path+parameter);
    }
    /**
     * 发起GitHub OAuth登录授权
     * <p>
     * 构建GitHub OAuth授权URL并重定向用户到GitHub授权页面。
     * 用户在GitHub完成授权后，会被重定向回系统的回调地址。
     * 该接口是GitHub登录流程的第一步。
     *
     * @param response HTTP响应对象，用于执行重定向操作
     *                <ul>
     *                    <li>会自动重定向到GitHub的OAuth授权页面</li>
     *                    <li>授权页面会显示应用信息和权限范围</li>
     *                </ul>
     * @throws IOException 当重定向操作失败时抛出IO异常
     * @see #getGithubAuthRequest() 获取GitHub授权请求对象
     * @see AuthStateUtils#createState() 生成防CSRF攻击的state参数
     */
    @Operation(summary = "Github登录")
    @AccessLimit(seconds = AccessLimitConst.DEFAULT_SECONDS,
            maxCount = AccessLimitConst.GITEE_OR_GITHUB_LOGIN_MAX_COUNT)
    @Parameters({
            @Parameter(name = "response", hidden = true, required = true)
    })
    @GetMapping("/github/render")
    public void githubRenderAuth(HttpServletResponse response) throws IOException {
        AuthRequest authRequest = getGithubAuthRequest();
        response.sendRedirect(authRequest.authorize(AuthStateUtils.createState()));
    }

    /**
     * 处理GitHub OAuth登录回调
     * <p>
     * 接收GitHub OAuth授权回调，处理授权码并完成用户登录流程。
     * 该接口会获取用户信息、创建或更新用户账户，并生成登录凭证。
     * 处理完成后会重定向到前端页面并携带登录参数。
     *
     * @param callback OAuth授权回调信息，包含：
     *                <ul>
     *                    <li>code: GitHub返回的临时授权码，用于换取访问令牌</li>
     *                    <li>state: 防CSRF攻击的状态值，需要与发起授权时的值匹配</li>
     *                </ul>
     * @param request HTTP请求对象，用于获取客户端IP等信息
     * @param response HTTP响应对象，用于重定向到前端页面
     * @throws IOException 当重定向操作失败时抛出IO异常
     * @see RegisterOrLoginTypeEnum#GITHUB GitHub登录类型枚举
     */
    @Operation(summary = "Github登录回调")
    @AccessLimit(seconds = AccessLimitConst.DEFAULT_SECONDS,
            maxCount = AccessLimitConst.GITEE_OR_GITHUB_LOGIN_MAX_COUNT)
    @Parameters({
            @Parameter(name = "code", description = "GitHub 授权成功后回调的临时授权码", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "state", description = "防 CSRF 的 state 值", required = true, in = ParameterIn.QUERY)
    })
    @GetMapping("/github/callback")
    public void githubLogin(AuthCallback callback, HttpServletRequest request, HttpServletResponse response) throws IOException {
        AuthRequest authRequest = getGithubAuthRequest();
        String parameter = oauthService.handleLogin(authRequest.login(callback), request,RegisterOrLoginTypeEnum.GITHUB.getRegisterType());
        response.sendRedirect(path+parameter);
    }

    /**
     * 构建Gitee OAuth授权请求对象
     * <p>
     * 使用配置的Gitee OAuth应用信息创建授权请求对象，
     * 该对象用于生成授权URL和处理授权回调。
     *
     * @return Gitee OAuth授权请求对象，包含客户端ID、客户端密钥和回调地址等配置
     * @see GiteeBody Gitee OAuth配置信息
     * @see AuthGiteeRequest Gitee授权请求实现类
     */
    private AuthRequest getGiteeAuthRequest() {
        return new AuthGiteeRequest(AuthConfig.builder()
                .clientId(giteeBody.getClientId())
                .clientSecret(giteeBody.getClientSecret())
                .redirectUri(giteeBody.getRedirectUri())
                .httpConfig(HttpConfig.builder()
                        .timeout(60000) // 60秒超时
                        .build())
                .build());
    }

    /**
     * 构建GitHub OAuth授权请求对象
     * <p>
     * 使用配置的GitHub OAuth应用信息创建授权请求对象，
     * 该对象用于生成授权URL和处理授权回调。
     *
     * @return GitHub OAuth授权请求对象，包含客户端ID、客户端密钥和回调地址等配置
     * @see GithubBody GitHub OAuth配置信息
     * @see AuthGithubRequest GitHub授权请求实现类
     */
    private AuthRequest getGithubAuthRequest() {
        return new AuthGithubRequest(AuthConfig.builder()
                .clientId(githubBody.getClientId())
                .clientSecret(githubBody.getClientSecret())
                .redirectUri(githubBody.getRedirectUri())
                .httpConfig(HttpConfig.builder()
                        .timeout(60000) // 60秒超时
                        .build())
                .build());
    }
}
