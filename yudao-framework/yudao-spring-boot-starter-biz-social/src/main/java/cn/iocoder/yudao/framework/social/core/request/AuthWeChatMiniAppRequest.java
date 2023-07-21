package cn.iocoder.yudao.framework.social.core.request;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.social.core.enums.AuthExtendSource;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xingyuv.jushauth.cache.AuthStateCache;
import com.xingyuv.jushauth.config.AuthConfig;
import com.xingyuv.jushauth.exception.AuthException;
import com.xingyuv.jushauth.model.AuthCallback;
import com.xingyuv.jushauth.model.AuthToken;
import com.xingyuv.jushauth.model.AuthUser;
import com.xingyuv.jushauth.request.AuthDefaultRequest;
import com.xingyuv.jushauth.utils.HttpUtils;
import com.xingyuv.jushauth.utils.UrlBuilder;
import lombok.Data;

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
        String response = new HttpUtils(config.getHttpConfig()).get(accessTokenUrl(authCallback.getCode())).getBody();
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
                .queryParam("js_code", code)
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
