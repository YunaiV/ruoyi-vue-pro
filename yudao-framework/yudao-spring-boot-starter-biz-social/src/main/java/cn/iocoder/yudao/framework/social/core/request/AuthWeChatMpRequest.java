package cn.iocoder.yudao.framework.social.core.request;

import com.alibaba.fastjson.JSONObject;
import com.xingyuv.jushauth.cache.AuthStateCache;
import com.xingyuv.jushauth.config.AuthConfig;
import com.xingyuv.jushauth.config.AuthDefaultSource;
import com.xingyuv.jushauth.enums.AuthResponseStatus;
import com.xingyuv.jushauth.enums.AuthUserGender;
import com.xingyuv.jushauth.enums.scope.AuthWechatMpScope;
import com.xingyuv.jushauth.exception.AuthException;
import com.xingyuv.jushauth.model.AuthCallback;
import com.xingyuv.jushauth.model.AuthResponse;
import com.xingyuv.jushauth.model.AuthToken;
import com.xingyuv.jushauth.model.AuthUser;
import com.xingyuv.jushauth.request.AuthDefaultRequest;
import com.xingyuv.jushauth.utils.AuthScopeUtils;
import com.xingyuv.jushauth.utils.GlobalAuthUtils;
import com.xingyuv.jushauth.utils.HttpUtils;
import com.xingyuv.jushauth.utils.UrlBuilder;

/**
 * 微信公众平台登录
 *
 * @author yangkai.shen (https://xkcoding.com)
 * @since 1.1.0
 */
public class AuthWeChatMpRequest extends AuthDefaultRequest {
    public AuthWeChatMpRequest(AuthConfig config) {
        super(config, AuthDefaultSource.WECHAT_MP);
    }

    public AuthWeChatMpRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, AuthDefaultSource.WECHAT_MP, authStateCache);
    }

    /**
     * 微信的特殊性，此时返回的信息同时包含 openid 和 access_token
     *
     * @param authCallback 回调返回的参数
     * @return 所有信息
     */
    @Override
    protected AuthToken getAccessToken(AuthCallback authCallback) {
        return this.getToken(accessTokenUrl(authCallback.getCode()));
    }

    @Override
    protected AuthUser getUserInfo(AuthToken authToken) {
        String openId = authToken.getOpenId();

        String response = doGetUserInfo(authToken);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        String location = String.format("%s-%s-%s", object.getString("country"), object.getString("province"), object.getString("city"));

        if (object.containsKey("unionid")) {
            authToken.setUnionId(object.getString("unionid"));
        }

        return AuthUser.builder()
                .rawUserInfo(object)
                .username(object.getString("nickname"))
                .nickname(object.getString("nickname"))
                .avatar(object.getString("headimgurl"))
                .location(location)
                .uuid(openId)
                .gender(AuthUserGender.getWechatRealGender(object.getString("sex")))
                .token(authToken)
                .source(source.toString())
                .build();
    }

    @Override
    public AuthResponse refresh(AuthToken oldToken) {
        return AuthResponse.builder()
                .code(AuthResponseStatus.SUCCESS.getCode())
                .data(this.getToken(refreshTokenUrl(oldToken.getRefreshToken())))
                .build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        if (object.containsKey("errcode")) {
            throw new AuthException(object.getIntValue("errcode"), object.getString("errmsg"));
        }
    }

    /**
     * 获取token，适用于获取access_token和刷新token
     *
     * @param accessTokenUrl 实际请求token的地址
     * @return token对象
     */
    private AuthToken getToken(String accessTokenUrl) {
        String response = new HttpUtils(config.getHttpConfig()).get(accessTokenUrl).getBody();
        JSONObject accessTokenObject = JSONObject.parseObject(response);

        this.checkResponse(accessTokenObject);

        return AuthToken.builder()
                .accessToken(accessTokenObject.getString("access_token"))
                .refreshToken(accessTokenObject.getString("refresh_token"))
                .expireIn(accessTokenObject.getIntValue("expires_in"))
                .openId(accessTokenObject.getString("openid"))
                .scope(accessTokenObject.getString("scope"))
                .build();
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     * @since 1.9.3
     */
    @Override
    public String authorize(String state) {
        return UrlBuilder.fromBaseUrl(source.authorize())
                .queryParam("appid", config.getClientId())
                .queryParam("redirect_uri", GlobalAuthUtils.urlEncode(config.getRedirectUri()))
                .queryParam("response_type", "code")
                .queryParam("scope", this.getScopes(",", false, AuthScopeUtils.getDefaultScopes(AuthWechatMpScope.values())))
                .queryParam("state", getRealState(state).concat("#wechat_redirect"))
                .build();
    }

    /**
     * 返回获取accessToken的url
     *
     * @param code 授权码
     * @return 返回获取accessToken的url
     */
    @Override
    protected String accessTokenUrl(String code) {
        return UrlBuilder.fromBaseUrl(source.accessToken())
                .queryParam("appid", config.getClientId())
                .queryParam("secret", config.getClientSecret())
                .queryParam("code", code)
                .queryParam("grant_type", "authorization_code")
                .build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param authToken 用户授权后的token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AuthToken authToken) {
        return UrlBuilder.fromBaseUrl(source.userInfo())
                .queryParam("access_token", authToken.getAccessToken())
                .queryParam("openid", authToken.getOpenId())
                .queryParam("lang", "zh_CN")
                .build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param refreshToken getAccessToken方法返回的refreshToken
     * @return 返回获取userInfo的url
     */
    @Override
    protected String refreshTokenUrl(String refreshToken) {
        return UrlBuilder.fromBaseUrl(source.refresh())
                .queryParam("appid", config.getClientId())
                .queryParam("grant_type", "refresh_token")
                .queryParam("refresh_token", refreshToken)
                .build();
    }
}
