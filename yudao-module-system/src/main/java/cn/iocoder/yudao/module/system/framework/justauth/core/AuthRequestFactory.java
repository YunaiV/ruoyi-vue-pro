/*
 * Copyright (c) 2019-2029, xkcoding & Yangkai.Shen & 沈扬凯 (237497819@qq.com & xkcoding.com).
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package cn.iocoder.yudao.module.system.framework.justauth.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.xkcoding.http.config.HttpConfig;
import com.xkcoding.justauth.autoconfigure.ExtendProperties;
import com.xkcoding.justauth.autoconfigure.JustAuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.config.AuthDefaultSource;
import me.zhyd.oauth.config.AuthSource;
import me.zhyd.oauth.enums.AuthResponseStatus;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.request.*;
import org.springframework.util.CollectionUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// TODO @芋艿：等官方发布 1.4.1！！！
/**
 * <p>
 * AuthRequest工厂类
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2019-07-22 14:21
 */
@Slf4j
@RequiredArgsConstructor
public class AuthRequestFactory {
    private final JustAuthProperties properties;
    private final AuthStateCache authStateCache;

    /**
     * 返回当前Oauth列表
     *
     * @return Oauth列表
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<String> oauthList() {
        // 默认列表
        List<String> defaultList = new ArrayList<>(properties.getType().keySet());
        // 扩展列表
        List<String> extendList = new ArrayList<>();
        ExtendProperties extend = properties.getExtend();
        if (null != extend) {
            Class enumClass = extend.getEnumClass();
            List<String> names = EnumUtil.getNames(enumClass);
            // 扩展列表
            extendList = extend.getConfig()
                    .keySet()
                    .stream()
                    .filter(x -> names.contains(x.toUpperCase()))
                    .collect(Collectors.toList());
        }

        // 合并
        return (List<String>) CollUtil.addAll(defaultList, extendList);
    }

    /**
     * 返回AuthRequest对象
     *
     * @param source {@link AuthSource}
     * @return {@link AuthRequest}
     */
    public AuthRequest get(String source) {
        if (StrUtil.isBlank(source)) {
            throw new AuthException(AuthResponseStatus.NO_AUTH_SOURCE);
        }

        // 获取 JustAuth 中已存在的
        AuthRequest authRequest = getDefaultRequest(source);

        // 如果获取不到则尝试取自定义的
        if (authRequest == null) {
            authRequest = getExtendRequest(properties.getExtend().getEnumClass(), source);
        }

        if (authRequest == null) {
            throw new AuthException(AuthResponseStatus.UNSUPPORTED);
        }

        return authRequest;
    }

    /**
     * 获取自定义的 request
     *
     * @param clazz  枚举类 {@link AuthSource}
     * @param source {@link AuthSource}
     * @return {@link AuthRequest}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private AuthRequest getExtendRequest(Class clazz, String source) {
        String upperSource = source.toUpperCase();
        try {
            EnumUtil.fromString(clazz, upperSource);
        } catch (IllegalArgumentException e) {
            // 无自定义匹配
            return null;
        }

        Map<String, ExtendProperties.ExtendRequestConfig> extendConfig = properties.getExtend().getConfig();

        // key 转大写
        Map<String, ExtendProperties.ExtendRequestConfig> upperConfig = new HashMap<>(6);
        extendConfig.forEach((k, v) -> upperConfig.put(k.toUpperCase(), v));

        ExtendProperties.ExtendRequestConfig extendRequestConfig = upperConfig.get(upperSource);
        if (extendRequestConfig != null) {

            // 配置 http config
            configureHttpConfig(upperSource, extendRequestConfig, properties.getHttpConfig());

            Class<? extends AuthRequest> requestClass = extendRequestConfig.getRequestClass();

            if (requestClass != null) {
                // 反射获取 Request 对象，所以必须实现 2 个参数的构造方法
                return ReflectUtil.newInstance(requestClass, (AuthConfig) extendRequestConfig, authStateCache);
            }
        }

        return null;
    }


    /**
     * 获取默认的 Request
     *
     * @param source {@link AuthSource}
     * @return {@link AuthRequest}
     */
    private AuthRequest getDefaultRequest(String source) {
        AuthDefaultSource authDefaultSource;

        try {
            authDefaultSource = EnumUtil.fromString(AuthDefaultSource.class, source.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 无自定义匹配
            return null;
        }

        AuthConfig config = properties.getType().get(authDefaultSource.name());
        // 找不到对应关系，直接返回空
        if (config == null) {
            return null;
        }

        // 配置 http config
        configureHttpConfig(authDefaultSource.name(), config, properties.getHttpConfig());

        switch (authDefaultSource) {
            case GITHUB:
                return new AuthGithubRequest(config, authStateCache);
            case WEIBO:
                return new AuthWeiboRequest(config, authStateCache);
            case GITEE:
                return new AuthGiteeRequest(config, authStateCache);
            case DINGTALK:
                return new AuthDingTalkRequest(config, authStateCache);
            case DINGTALK_V2:
                return new AuthDingTalkV2Request(config, authStateCache);
            case DINGTALK_ACCOUNT:
                return new AuthDingTalkAccountRequest(config, authStateCache);
            case BAIDU:
                return new AuthBaiduRequest(config, authStateCache);
            case CSDN:
                return new AuthCsdnRequest(config, authStateCache);
            case CODING:
                return new AuthCodingRequest(config, authStateCache);
            case OSCHINA:
                return new AuthOschinaRequest(config, authStateCache);
            case ALIPAY:
                return new AuthAlipayRequest(config, authStateCache);
            case QQ:
                return new AuthQqRequest(config, authStateCache);
            case WECHAT_OPEN:
                return new AuthWeChatOpenRequest(config, authStateCache);
            case WECHAT_MP:
                return new AuthWeChatMpRequest(config, authStateCache);
            case TAOBAO:
                return new AuthTaobaoRequest(config, authStateCache);
            case GOOGLE:
                return new AuthGoogleRequest(config, authStateCache);
            case FACEBOOK:
                return new AuthFacebookRequest(config, authStateCache);
            case DOUYIN:
                return new AuthDouyinRequest(config, authStateCache);
            case LINKEDIN:
                return new AuthLinkedinRequest(config, authStateCache);
            case MICROSOFT:
                return new AuthMicrosoftRequest(config, authStateCache);
            case MICROSOFT_CN:
                return new AuthMicrosoftCnRequest(config, authStateCache);

            case MI:
                return new AuthMiRequest(config, authStateCache);
            case TOUTIAO:
                return new AuthToutiaoRequest(config, authStateCache);
            case TEAMBITION:
                return new AuthTeambitionRequest(config, authStateCache);
            case RENREN:
                return new AuthRenrenRequest(config, authStateCache);
            case PINTEREST:
                return new AuthPinterestRequest(config, authStateCache);
            case STACK_OVERFLOW:
                return new AuthStackOverflowRequest(config, authStateCache);
            case HUAWEI:
                return new AuthHuaweiRequest(config, authStateCache);
            case HUAWEI_V3:
                return new AuthHuaweiV3Request(config, authStateCache);
            case WECHAT_ENTERPRISE:
                return new AuthWeChatEnterpriseQrcodeRequest(config, authStateCache);
            case WECHAT_ENTERPRISE_V2:
                return new AuthWeChatEnterpriseQrcodeV2Request(config, authStateCache);
            case WECHAT_ENTERPRISE_QRCODE_THIRD:
                return new AuthWeChatEnterpriseThirdQrcodeRequest(config, authStateCache);
            case WECHAT_ENTERPRISE_WEB:
                return new AuthWeChatEnterpriseWebRequest(config, authStateCache);
            case KUJIALE:
                return new AuthKujialeRequest(config, authStateCache);
            case GITLAB:
                return new AuthGitlabRequest(config, authStateCache);
            case MEITUAN:
                return new AuthMeituanRequest(config, authStateCache);
            case ELEME:
                return new AuthElemeRequest(config, authStateCache);
            case TWITTER:
                return new AuthTwitterRequest(config, authStateCache);
            case FEISHU:
                return new AuthFeishuRequest(config, authStateCache);
            case JD:
                return new AuthJdRequest(config, authStateCache);
            case ALIYUN:
                return new AuthAliyunRequest(config, authStateCache);
            case XMLY:
                return new AuthXmlyRequest(config, authStateCache);
            case AMAZON:
                return new AuthAmazonRequest(config, authStateCache);
            case SLACK:
                return new AuthSlackRequest(config, authStateCache);
            case LINE:
                return new AuthLineRequest(config, authStateCache);
            case OKTA:
                return new AuthOktaRequest(config, authStateCache);
            case PROGINN:
                return new AuthProginnRequest(config,authStateCache);
            case AFDIAN:
                return new AuthAfDianRequest(config,authStateCache);
            case APPLE:
                return new AuthAppleRequest(config,authStateCache);
            case FIGMA:
                return new AuthFigmaRequest(config,authStateCache);
            case WECHAT_MINI_PROGRAM:
                config.setIgnoreCheckRedirectUri(true);
                config.setIgnoreCheckState(true);
                return new AuthWechatMiniProgramRequest(config, authStateCache);
            case QQ_MINI_PROGRAM:
                config.setIgnoreCheckRedirectUri(true);
                config.setIgnoreCheckState(true);
                return new AuthQQMiniProgramRequest(config, authStateCache);
            default:
                return null;
        }
    }

    /**
     * 配置 http 相关的配置
     *
     * @param authSource {@link AuthSource}
     * @param authConfig {@link AuthConfig}
     */
    private void configureHttpConfig(String authSource, AuthConfig authConfig, JustAuthProperties.JustAuthHttpConfig httpConfig) {
        if (null == httpConfig) {
            return;
        }
        Map<String, JustAuthProperties.JustAuthProxyConfig> proxyConfigMap = httpConfig.getProxy();
        if (CollectionUtils.isEmpty(proxyConfigMap)) {
            return;
        }
        JustAuthProperties.JustAuthProxyConfig proxyConfig = proxyConfigMap.get(authSource);

        if (null == proxyConfig) {
            return;
        }

        authConfig.setHttpConfig(HttpConfig.builder()
                .timeout(httpConfig.getTimeout())
                .proxy(new Proxy(Proxy.Type.valueOf(proxyConfig.getType()), new InetSocketAddress(proxyConfig.getHostname(), proxyConfig.getPort())))
                .build());
    }
}
