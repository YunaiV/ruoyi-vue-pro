package cn.iocoder.yudao.framework.social.core.request;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.social.core.enums.AuthExtendSource;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthDefaultRequest;
import me.zhyd.oauth.utils.HttpUtils;
import me.zhyd.oauth.utils.UrlBuilder;

/**
 * 微信小程序登陆 Request 请求
 *
 * 由于 JustAuth 定位是面向 Web 为主的三方登录，所以微信小程序只能自己封装
 *
 * @author timfruit
 * @date 2021-10-29
 */
public class AuthWeChatMiniAppRequest extends AuthDefaultRequest {

    public AuthWeChatMiniAppRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, AuthExtendSource.WECHAT_MINI_APP, authStateCache);
    }

    @Override
    protected AuthToken getAccessToken(AuthCallback authCallback) {
        // 参见 https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/login/auth.code2Session.html 文档
        // 使用 code 获取对应的 openId、unionId 等字段
        String response = new HttpUtils(config.getHttpConfig()).get(accessTokenUrl(authCallback.getCode()));
        JSCode2SessionResponse accessTokenObject = JsonUtils.parseObject(response, JSCode2SessionResponse.class);
        assert accessTokenObject != null;
        checkResponse(accessTokenObject);
        // 拼装结果
        return AuthToken.builder()
                .openId(accessTokenObject.getOpenid())
                .unionId(accessTokenObject.getUnionId())
                .build();
    }

    @Override
    protected AuthUser getUserInfo(AuthToken authToken) {
        // 参见 https://developers.weixin.qq.com/miniprogram/dev/api/open-api/user-info/wx.getUserProfile.html 文档
        // 如果需要用户信息，需要在小程序调用函数后传给后端
        return AuthUser.builder()
                .username("")
                .nickname("")
                .avatar("")
                .uuid(authToken.getOpenId())
                .token(authToken)
                .source(source.toString())
                .build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param response 请求响应内容
     */
    private void checkResponse(JSCode2SessionResponse response) {
        if (response.getErrorCode() != 0) {
            throw new AuthException(response.getErrorCode(), response.getErrorMsg());
        }
    }

    @Override
    protected String accessTokenUrl(String code) {
        return UrlBuilder.fromBaseUrl(source.accessToken())
                .queryParam("appid", config.getClientId())
                .queryParam("secret", config.getClientSecret())
                .queryParam("js_code", code) // 和父类不同，所以需要重写该方法
                .queryParam("grant_type", "authorization_code")
                .build();
    }

    @Data
    @SuppressWarnings("SpellCheckingInspection")
    private static class JSCode2SessionResponse {

        @JsonProperty("errcode")
        private int errorCode;
        @JsonProperty("errmsg")
        private String errorMsg;
        @JsonProperty("session_key")
        private String sessionKey;
        private String openid;
        @JsonProperty("unionid")
        private String unionId;

    }

}
