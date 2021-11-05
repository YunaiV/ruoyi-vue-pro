package cn.iocoder.yudao.framework.social.core.request;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.social.core.enums.AuthExtendSource;
import cn.iocoder.yudao.framework.social.core.model.AuthExtendToken;
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
 * 微信小程序登陆
 *
 * @author timfruit
 * @date 2021-10-29
 */
public class AuthWeChatMiniProgramRequest extends AuthDefaultRequest {

    public AuthWeChatMiniProgramRequest(AuthConfig config) {
        super(config, AuthExtendSource.WECHAT_MINI_PROGRAM);
    }

    public AuthWeChatMiniProgramRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, AuthExtendSource.WECHAT_MINI_PROGRAM, authStateCache);
    }

    @Override
    protected AuthToken getAccessToken(AuthCallback authCallback) {
        // https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/login/auth.code2Session.html
        String response = new HttpUtils(config.getHttpConfig()).get(accessTokenUrl(authCallback.getCode()));
        CodeSessionResponse accessTokenObject = JsonUtils.parseObject(response, CodeSessionResponse.class);

        this.checkResponse(accessTokenObject);

        AuthExtendToken token = new AuthExtendToken();
        token.setMiniSessionKey(accessTokenObject.sessionKey);
        token.setOpenId(accessTokenObject.openid);
        token.setUnionId(accessTokenObject.unionid);
        return token;
    }

    @Override
    protected AuthUser getUserInfo(AuthToken authToken) {
        // https://developers.weixin.qq.com/miniprogram/dev/api/open-api/user-info/wx.getUserProfile.html
        // 如果需要用户信息，需要在小程序调用函数后传给后端
        return AuthUser.builder()
                .uuid(authToken.getOpenId())
                //TODO 是使用默认值，还是有小程序获取用户信息 和 code 一起传过来
                .nickname("")
                .avatar("")
                .token(authToken)
                .source(source.toString())
                .build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(CodeSessionResponse object) {
        if (object.errcode != 0) {
            throw new AuthException(object.errcode, object.errmsg);
        }
    }

    /**
     * 返回获取 accessToken 的 url
     *
     * @param code 授权码
     * @return 返回获取 accessToken 的 url
     */
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
    private static class CodeSessionResponse {
        private int errcode;
        private String errmsg;
        @JsonProperty("session_key")
        private String sessionKey;
        private String openid;
        private String unionid;
    }

}
