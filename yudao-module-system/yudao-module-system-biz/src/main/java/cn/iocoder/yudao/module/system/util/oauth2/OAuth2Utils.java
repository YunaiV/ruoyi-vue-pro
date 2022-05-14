package cn.iocoder.yudao.module.system.util.oauth2;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;

import java.util.*;

/**
 * OAuth2 相关的工具类
 *
 * @author 芋道源码
 */
public class OAuth2Utils {

    /**
     * 构建简化模式下，重定向的 URI
     *
     * copy from Spring Security OAuth2 的 AuthorizationEndpoint 类的 appendAccessToken 方法
     *
     * @param redirectUri 重定向 URI
     * @param accessToken 访问令牌
     * @param state 状态
     * @param expireTime 过期时间
     * @param scopes 授权范围
     * @param additionalInformation 附加信息
     * @return 简化授权模式下的重定向 URI
     */
    public static String buildImplicitRedirectUri(String redirectUri, String accessToken, String state, Date expireTime,
                                                  Collection<String> scopes, Map<String, Object> additionalInformation) {
        Map<String, Object> vars = new LinkedHashMap<String, Object>();
        Map<String, String> keys = new HashMap<String, String>();
        vars.put("access_token", accessToken);
        vars.put("token_type", SecurityFrameworkUtils.TOKEN_TYPE.toLowerCase());
        if (state != null) {
            vars.put("state", state);
        }
        if (expireTime != null) {
            long expires_in = (expireTime.getTime() - System.currentTimeMillis()) / 1000;
            vars.put("expires_in", expires_in);
        }
        if (CollUtil.isNotEmpty(scopes)) {
            vars.put("scope", CollUtil.join(scopes, " "));
        }
        for (String key : additionalInformation.keySet()) {
            Object value = additionalInformation.get(key);
            if (value != null) {
                keys.put("extra_" + key, key);
                vars.put("extra_" + key, value);
            }
        }
        // Do not include the refresh token (even if there is one)
        return HttpUtils.append(redirectUri, vars, keys, true);
    }

}
