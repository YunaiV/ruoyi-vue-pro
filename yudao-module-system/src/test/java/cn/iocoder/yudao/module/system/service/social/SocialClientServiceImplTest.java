package cn.iocoder.yudao.module.system.service.social;

import cn.binarywang.wx.miniapp.api.WxMaOrderShippingService;
import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.WxMaUserService;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.binarywang.wx.miniapp.bean.shop.request.shipping.WxMaOrderShippingInfoUploadRequest;
import cn.binarywang.wx.miniapp.bean.shop.response.WxMaOrderShippingInfoBaseResponse;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.system.api.social.dto.SocialWxaOrderUploadShippingInfoReqDTO;
import cn.iocoder.yudao.module.system.controller.admin.social.vo.client.SocialClientPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.social.vo.client.SocialClientSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.social.SocialClientDO;
import cn.iocoder.yudao.module.system.dal.mysql.social.SocialClientMapper;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import cn.iocoder.yudao.module.system.framework.justauth.core.AuthRequestFactory;
import com.binarywang.spring.starter.wxjava.miniapp.properties.WxMaProperties;
import com.binarywang.spring.starter.wxjava.mp.properties.WxMpProperties;
import com.xkcoding.justauth.autoconfigure.JustAuthProperties;
import javax.annotation.Resource;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.error.WxError;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.zhyd.oauth.cache.AuthDefaultStateCache;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.request.AuthWechatMiniProgramRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;


import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link SocialClientServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({SocialClientServiceImpl.class, SocialClientServiceImplTest.JustAuthTestConfiguration.class})
public class SocialClientServiceImplTest extends BaseDbUnitTest {

    private static final String REDIRECT_URI = "https://cms.test:8443/portal/social-login";

    @Resource
    private SocialClientServiceImpl socialClientService;

    @Resource
    private SocialClientMapper socialClientMapper;
    @Resource
    private JustAuthProperties justAuthProperties;

    /**
     * 只用于表示已开启 JustAuth；请求由 Service 按本次合并后的配置另行构造
     */
    @MockBean
    private AuthRequestFactory authRequestFactory;

    @MockBean
    private WxMpService wxMpService;
    @MockBean
    private WxMpProperties wxMpProperties;
    @MockBean
    private StringRedisTemplate stringRedisTemplate;
    @MockBean
    private WxMaService wxMaService;
    @MockBean
    private WxMaProperties wxMaProperties;

    private final List<JustAuthProperties> constructedProperties = new ArrayList<>();

    @AfterEach
    public void tearDown() {
        justAuthProperties.getType().clear();
    }

    @Test
    public void testGetAuthorizeUrl_justAuthDisabled() {
        // 准备参数
        Integer socialType = SocialTypeEnum.WECHAT_MP.getType();
        Integer userType = UserTypeEnum.ADMIN.getValue();
        String redirectUri = "https://example.invalid";
        // mock 关闭 JustAuth 后，可选的请求工厂未注入
        ReflectUtil.setFieldValue(socialClientService, "authRequestFactory", null);
        try {
            // 调用、断言
            assertServiceException(() -> socialClientService.getAuthorizeUrl(socialType, userType, redirectUri),
                    SOCIAL_USER_AUTH_FAILURE, "社交登录未启用");
        } finally {
            ReflectUtil.setFieldValue(socialClientService, "authRequestFactory", authRequestFactory);
        }
    }

    @Test
    public void testGetAuthorizeUrl_sameRedirectUriForAccessToken() {
        // 使用真实的 SDK Request 在本地构造授权地址和换取令牌地址，不请求社交平台，不代表真实平台授权通过
        for (SocialTypeEnum socialType : java.util.Arrays.asList(SocialTypeEnum.QQ, SocialTypeEnum.WEIBO)) {
            // 准备参数：没有全局配置，只有启用的会员社交应用
            Integer userType = UserTypeEnum.MEMBER.getValue();
            insertClient(socialType.getType(), userType, CommonStatusEnum.ENABLE.getStatus());

            // 调用
            String authorizeUrl = socialClientService.getAuthorizeUrl(socialType.getType(), userType, REDIRECT_URI);
            AuthRequest authRequest = socialClientService.buildAuthRequest(socialType.getType(), userType, REDIRECT_URI);
            String accessTokenUrl = (String) ReflectUtil.invoke(authRequest, "accessTokenUrl", "C1");

            // 断言：授权地址和换取令牌地址的 redirect_uri 都是本次回调地址，凭据来自社交应用
            assertEquals(REDIRECT_URI, getQueryParam(authorizeUrl, "redirect_uri"), socialType.name());
            assertEquals(REDIRECT_URI, getQueryParam(accessTokenUrl, "redirect_uri"), socialType.name());
            assertEquals("db-id", getQueryParam(authorizeUrl, "client_id"), socialType.name());
            assertNotNull(getQueryParam(authorizeUrl, "state"), socialType.name());
        }
    }

    @Test
    public void testAuthSocialUser_success() {
        // 准备参数
        Integer socialType = SocialTypeEnum.WECHAT_MP.getType();
        Integer userType = randomPojo(UserTypeEnum.class).getValue();
        String code = randomString();
        String state = randomString();
        putDefaultConfig("WECHAT_MP");
        // mock 方法（AuthResponse）
        AuthRequest authRequest = mock(AuthRequest.class);
        AuthUser authUser = randomPojo(AuthUser.class);
        when(authRequest.login(argThat(authCallback -> {
            assertEquals(code, authCallback.getCode());
            assertEquals(state, authCallback.getState());
            return true;
        }))).thenReturn(new AuthResponse<>(2000, null, authUser));

        try (MockedConstruction<AuthRequestFactory> ignored = mockAuthRequest(authRequest)) {
            // 调用
            AuthUser result = socialClientService.getAuthUser(socialType, userType, code, state, null);
            // 断言
            assertSame(authUser, result);
        }
    }

    @Test
    public void testAuthSocialUser_fail() {
        // 准备参数
        Integer socialType = SocialTypeEnum.WECHAT_MP.getType();
        Integer userType = randomPojo(UserTypeEnum.class).getValue();
        putDefaultConfig("WECHAT_MP");
        // mock 方法（AuthResponse）
        AuthRequest authRequest = mock(AuthRequest.class);
        when(authRequest.login(any())).thenReturn(new AuthResponse<>(0, "模拟失败", null));

        try (MockedConstruction<AuthRequestFactory> ignored = mockAuthRequest(authRequest)) {
            // 调用并断言
            assertServiceException(
                    () -> socialClientService.getAuthUser(socialType, userType, randomString(), randomString(), null),
                    SOCIAL_USER_AUTH_FAILURE, "模拟失败");
        }
    }

    @Test
    public void testAuthSocialUser_uuidBlank() {
        // 准备参数
        Integer socialType = SocialTypeEnum.DINGTALK.getType();
        Integer userType = randomPojo(UserTypeEnum.class).getValue();
        putDefaultConfig("DINGTALK_V2");
        for (String uuid : new String[]{null, " "}) {
            // mock 方法（AuthResponse）：获取用户信息失败时，JustAuth 仍然返回成功，但 uuid 为空
            AuthRequest authRequest = mock(AuthRequest.class);
            when(authRequest.login(any())).thenReturn(new AuthResponse<>(2000, null,
                    randomPojo(AuthUser.class, o -> o.setUuid(uuid))));

            try (MockedConstruction<AuthRequestFactory> ignored = mockAuthRequest(authRequest)) {
                // 调用并断言
                assertServiceException(
                        () -> socialClientService.getAuthUser(socialType, userType, randomString(), randomString(), null),
                        SOCIAL_USER_AUTH_FAILURE, "未获取到用户标识");
            }
        }
    }

    @Test
    public void testGetAuthUser_redirectUri() {
        // 准备参数：微博只有启用的社交应用，换取令牌时传入发起授权时的回调地址
        Integer socialType = SocialTypeEnum.WEIBO.getType();
        Integer userType = UserTypeEnum.MEMBER.getValue();
        insertClient(socialType, userType, CommonStatusEnum.ENABLE.getStatus());
        AuthRequest authRequest = mock(AuthRequest.class);
        AuthUser authUser = randomPojo(AuthUser.class);
        when(authRequest.login(any())).thenReturn(new AuthResponse<>(2000, null, authUser));

        try (MockedConstruction<AuthRequestFactory> ignored = mockAuthRequest(authRequest)) {
            // 调用
            AuthUser result = socialClientService.getAuthUser(socialType, userType, "C1", "S1", REDIRECT_URI);
            // 断言：按本次合并后的配置构造，回调地址为传入的值
            assertSame(authUser, result);
            AuthConfig config = constructedProperties.get(0).getType().get("WEIBO");
            assertEquals("db-id", config.getClientId());
            assertEquals(REDIRECT_URI, config.getRedirectUri());
        }
    }

    @Test
    public void testBuildAuthRequest_globalConfig() {
        // 准备参数：QQ 只有全局配置，没有社交应用
        Integer socialType = SocialTypeEnum.QQ.getType();
        Integer userType = UserTypeEnum.MEMBER.getValue();
        AuthConfig defaultConfig = putDefaultConfig("QQ");

        // 调用：未传回调地址时使用全局回调地址；传入时使用本次回调地址
        String globalUrl = socialClientService.buildAuthRequest(socialType, userType, null).authorize("S1");
        String requestUrl = socialClientService.buildAuthRequest(socialType, userType, REDIRECT_URI).authorize("S2");

        // 断言：使用全局凭据，不修改共享的全局配置
        assertEquals("global-id", getQueryParam(globalUrl, "client_id"));
        assertEquals("https://global.test/callback", getQueryParam(globalUrl, "redirect_uri"));
        assertEquals(REDIRECT_URI, getQueryParam(requestUrl, "redirect_uri"));
        assertEquals("https://global.test/callback", defaultConfig.getRedirectUri());
        assertSame(defaultConfig, justAuthProperties.getType().get("QQ"));
        assertEquals(1, justAuthProperties.getType().size());
    }

    @Test
    public void testBuildAuthRequest_clientDisable() {
        // 准备参数：社交应用未启用，回落全局配置
        Integer socialType = SocialTypeEnum.QQ.getType();
        Integer userType = UserTypeEnum.MEMBER.getValue();
        putDefaultConfig("QQ");
        insertClient(socialType, userType, CommonStatusEnum.DISABLE.getStatus());

        // 调用
        String url = socialClientService.buildAuthRequest(socialType, userType, REDIRECT_URI).authorize("S1");
        // 断言
        assertEquals("global-id", getQueryParam(url, "client_id"));
    }

    @Test
    public void testBuildAuthRequest_clientEnable() {
        // 准备参数：启用的社交应用覆盖全局配置的密钥
        Integer socialType = SocialTypeEnum.QQ.getType();
        Integer userType = UserTypeEnum.MEMBER.getValue();
        AuthConfig defaultConfig = putDefaultConfig("QQ");
        insertClient(socialType, userType, CommonStatusEnum.ENABLE.getStatus());

        // 调用
        String url = socialClientService.buildAuthRequest(socialType, userType, REDIRECT_URI).authorize("S1");
        // 断言：覆盖副本，不修改共享的全局配置
        assertEquals("db-id", getQueryParam(url, "client_id"));
        assertEquals("global-id", defaultConfig.getClientId());
        assertEquals("global-secret", defaultConfig.getClientSecret());
        assertNull(defaultConfig.getAgentId());
    }

    @Test
    public void testBuildAuthRequest_miniProgram() {
        // 准备参数：微信小程序没有回调地址
        Integer socialType = SocialTypeEnum.WECHAT_MINI_PROGRAM.getType();
        AuthConfig defaultConfig = AuthConfig.builder().clientId("wx-id").clientSecret("wx-secret").build();
        justAuthProperties.getType().put("WECHAT_MINI_PROGRAM", defaultConfig);

        // 调用
        AuthRequest authRequest = socialClientService.buildAuthRequest(socialType, UserTypeEnum.MEMBER.getValue(), null);
        // 断言：仍按小程序构造；不校验回调地址的设置只作用于本次副本
        assertInstanceOf(AuthWechatMiniProgramRequest.class, authRequest);
        assertFalse(defaultConfig.isIgnoreCheckRedirectUri());
    }

    @Test
    public void testBuildAuthRequest_notConfigured() {
        // 准备参数：没有全局配置，社交应用未启用
        Integer socialType = SocialTypeEnum.WEIBO.getType();
        Integer userType = UserTypeEnum.MEMBER.getValue();
        insertClient(socialType, userType, CommonStatusEnum.DISABLE.getStatus());

        // 调用并断言：业务错误，不构造 AuthRequest
        assertServiceException(() -> socialClientService.buildAuthRequest(socialType, userType, null),
                SOCIAL_USER_AUTH_FAILURE, "社交平台未配置");
    }

    @Test
    public void testBuildAuthRequest_illegalRedirectUri() {
        // 使用真实的 SDK Request 在本地构造，不请求社交平台
        // 准备参数：QQ 只有启用的会员社交应用
        Integer userType = UserTypeEnum.MEMBER.getValue();
        Integer qq = SocialTypeEnum.QQ.getType();
        insertClient(qq, userType, CommonStatusEnum.ENABLE.getStatus());

        // 调用并断言：回调地址为空或不是 HTTP 地址时，SDK 构造失败按授权失败返回，不作为系统异常
        for (String redirectUri : java.util.Arrays.asList("", "javascript:alert(1)")) {
            assertAuthFailure(() -> socialClientService.getAuthorizeUrl(qq, userType, redirectUri));
            assertAuthFailure(() -> socialClientService.getAuthUser(qq, userType, "C1", "S1", redirectUri));
        }
        // 调用并断言：正常回调地址不受影响
        assertEquals(REDIRECT_URI, getQueryParam(socialClientService.getAuthorizeUrl(qq, userType, REDIRECT_URI),
                "redirect_uri"));
    }

    private static void assertAuthFailure(Runnable runnable) {
        ServiceException exception = assertThrows(ServiceException.class, runnable::run);
        assertEquals(SOCIAL_USER_AUTH_FAILURE.getCode(), exception.getCode());
    }

    @Test
    public void testJustAuthDisabled() {
        // 准备参数：未开启 JustAuth 时没有 AuthRequestFactory
        Object factory = ReflectUtil.getFieldValue(socialClientService, "authRequestFactory");
        ReflectUtil.setFieldValue(socialClientService, "authRequestFactory", null);
        try {
            // 调用并断言
            assertTrue(socialClientService.getAvailableSocialTypeList(UserTypeEnum.MEMBER.getValue()).isEmpty());
            assertServiceException(() -> socialClientService.buildAuthRequest(SocialTypeEnum.QQ.getType(),
                    UserTypeEnum.MEMBER.getValue(), null), SOCIAL_USER_AUTH_FAILURE, "社交登录未启用");
        } finally {
            ReflectUtil.setFieldValue(socialClientService, "authRequestFactory", factory);
        }
    }

    @Test
    public void testGetAvailableSocialTypeList() {
        // 准备参数
        Integer userType = UserTypeEnum.MEMBER.getValue();
        // 公众号：全局配置完整
        putDefaultConfig("WECHAT_MP");
        // 企业微信：全局配置缺少 agentId
        putDefaultConfig("WECHAT_ENTERPRISE");
        // QQ：只有启用的社交应用
        insertClient(SocialTypeEnum.QQ.getType(), userType, CommonStatusEnum.ENABLE.getStatus());
        // 微博：社交应用未启用，且没有全局配置
        insertClient(SocialTypeEnum.WEIBO.getType(), userType, CommonStatusEnum.DISABLE.getStatus());
        // 开放平台：只为管理员启用
        insertClient(SocialTypeEnum.WECHAT_OPEN.getType(), UserTypeEnum.ADMIN.getValue(), CommonStatusEnum.ENABLE.getStatus());
        // 支付宝小程序：启用的社交应用缺少公钥
        socialClientMapper.insert(insertClient(SocialTypeEnum.ALIPAY_MINI_PROGRAM.getType(), userType,
                CommonStatusEnum.ENABLE.getStatus(), false));

        // 调用
        List<Integer> socialTypes = socialClientService.getAvailableSocialTypeList(userType);
        // 断言：按平台定义顺序返回配置完整的平台
        assertEquals(java.util.Arrays.asList(SocialTypeEnum.WECHAT_MP.getType(), SocialTypeEnum.QQ.getType()), socialTypes);
        verify(authRequestFactory, never()).get(anyString());
    }

    private AuthConfig putDefaultConfig(String source) {
        AuthConfig config = AuthConfig.builder().clientId("global-id").clientSecret("global-secret")
                .redirectUri("https://global.test/callback").build();
        justAuthProperties.getType().put(source, config);
        return config;
    }

    /**
     * 拦截 Service 按本次配置构造的 AuthRequestFactory，返回指定的 AuthRequest，并记录构造时传入的配置
     */
    private MockedConstruction<AuthRequestFactory> mockAuthRequest(AuthRequest authRequest) {
        constructedProperties.clear();
        return mockConstruction(AuthRequestFactory.class, (factory, context) -> {
            constructedProperties.add((JustAuthProperties) context.arguments().get(0));
            when(factory.get(anyString())).thenReturn(authRequest);
        });
    }

    private static String getQueryParam(String url, String name) {
        CharSequence value = UrlBuilder.of(url, StandardCharsets.UTF_8).getQuery().get(name);
        return value == null ? null : value.toString();
    }

    private void insertClient(Integer socialType, Integer userType, Integer status) {
        socialClientMapper.insert(insertClient(socialType, userType, status, true));
    }

    private static SocialClientDO insertClient(Integer socialType, Integer userType, Integer status, boolean publicKey) {
        return randomPojo(SocialClientDO.class, o -> o.setId(null).setSocialType(socialType).setUserType(userType)
                .setStatus(status).setClientId("db-id").setClientSecret("db-secret").setAgentId("1001")
                .setPublicKey(publicKey ? "db-public-key" : ""));
    }

    // =================== 微信公众号独有 ===================

    @Test
    public void testCreateWxMpJsapiSignature() throws WxErrorException {
        // 准备参数
        Integer userType = randomPojo(UserTypeEnum.class).getValue();
        String url = randomString();
        // mock 方法
        WxJsapiSignature signature = randomPojo(WxJsapiSignature.class);
        when(wxMpService.createJsapiSignature(eq(url))).thenReturn(signature);

        // 调用
        WxJsapiSignature result = socialClientService.createWxMpJsapiSignature(userType, url);
        // 断言
        assertSame(signature, result);
    }

    @Test
    public void testGetWxMpService_clientNull() {
        // 准备参数
        Integer userType = randomPojo(UserTypeEnum.class).getValue();
        // mock 方法

        // 调用
        WxMpService result = socialClientService.getWxMpService(userType);
        // 断言
        assertSame(wxMpService, result);
    }

    @Test
    public void testGetWxMpService_clientDisable() {
        // 准备参数
        Integer userType = randomPojo(UserTypeEnum.class).getValue();
        // mock 数据
        SocialClientDO client = randomPojo(SocialClientDO.class, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())
                .setUserType(userType).setSocialType(SocialTypeEnum.WECHAT_MP.getType()));
        socialClientMapper.insert(client);

        // 调用
        WxMpService result = socialClientService.getWxMpService(userType);
        // 断言
        assertSame(wxMpService, result);
    }

    @Test
    public void testGetWxMpService_clientEnable() {
        // 准备参数
        Integer userType = randomPojo(UserTypeEnum.class).getValue();
        // mock 数据
        SocialClientDO client = randomPojo(SocialClientDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setUserType(userType).setSocialType(SocialTypeEnum.WECHAT_MP.getType()));
        socialClientMapper.insert(client);
        // mock 方法
        WxMpProperties.ConfigStorage configStorage = mock(WxMpProperties.ConfigStorage.class);
        when(wxMpProperties.getConfigStorage()).thenReturn(configStorage);

        // 调用
        WxMpService result = socialClientService.getWxMpService(userType);
        // 断言
        assertNotSame(wxMpService, result);
        assertEquals(client.getClientId(), result.getWxMpConfigStorage().getAppId());
        assertEquals(client.getClientSecret(), result.getWxMpConfigStorage().getSecret());
    }

    // =================== 微信小程序独有 ===================

    @Test
    public void testGetWxMaPhoneNumberInfo_success() throws WxErrorException {
        // 准备参数
        Integer userType = randomPojo(UserTypeEnum.class).getValue();
        String phoneCode = randomString();
        // mock 方法
        WxMaUserService userService = mock(WxMaUserService.class);
        when(wxMaService.getUserService()).thenReturn(userService);
        WxMaPhoneNumberInfo phoneNumber = randomPojo(WxMaPhoneNumberInfo.class);
        when(userService.getPhoneNumber(eq(phoneCode))).thenReturn(phoneNumber);

        // 调用
        WxMaPhoneNumberInfo result = socialClientService.getWxMaPhoneNumberInfo(userType, phoneCode);
        // 断言
        assertSame(phoneNumber, result);
    }

    @Test
    public void testGetWxMaPhoneNumberInfo_exception() throws WxErrorException {
        // 准备参数
        Integer userType = randomPojo(UserTypeEnum.class).getValue();
        String phoneCode = randomString();
        // mock 方法
        WxMaUserService userService = mock(WxMaUserService.class);
        when(wxMaService.getUserService()).thenReturn(userService);
        WxErrorException wxErrorException = new WxErrorException(new NullPointerException());
        when(userService.getPhoneNumber(eq(phoneCode))).thenThrow(wxErrorException);

        // 调用并断言异常
        assertServiceException(() -> socialClientService.getWxMaPhoneNumberInfo(userType, phoneCode),
                SOCIAL_CLIENT_WEIXIN_MINI_APP_PHONE_CODE_ERROR);
    }

    @Test
    public void testGetWxMaService_clientNull() {
        // 准备参数
        Integer userType = randomPojo(UserTypeEnum.class).getValue();
        // mock 方法

        // 调用
        WxMaService result = socialClientService.getWxMaService(userType);
        // 断言
        assertSame(wxMaService, result);
    }

    @Test
    public void testGetWxMaService_clientDisable() {
        // 准备参数
        Integer userType = randomPojo(UserTypeEnum.class).getValue();
        // mock 数据
        SocialClientDO client = randomPojo(SocialClientDO.class, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())
                .setUserType(userType).setSocialType(SocialTypeEnum.WECHAT_MINI_PROGRAM.getType()));
        socialClientMapper.insert(client);

        // 调用
        WxMaService result = socialClientService.getWxMaService(userType);
        // 断言
        assertSame(wxMaService, result);
    }

    @Test
    public void testGetWxMaService_clientEnable() {
        // 准备参数
        Integer userType = randomPojo(UserTypeEnum.class).getValue();
        // mock 数据
        SocialClientDO client = randomPojo(SocialClientDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setUserType(userType).setSocialType(SocialTypeEnum.WECHAT_MINI_PROGRAM.getType()));
        socialClientMapper.insert(client);
        // mock 方法
        WxMaProperties.ConfigStorage configStorage = mock(WxMaProperties.ConfigStorage.class);
        when(wxMaProperties.getConfigStorage()).thenReturn(configStorage);

        // 调用
        WxMaService result = socialClientService.getWxMaService(userType);
        // 断言
        assertNotSame(wxMaService, result);
        assertEquals(client.getClientId(), result.getWxMaConfig().getAppid());
        assertEquals(client.getClientSecret(), result.getWxMaConfig().getSecret());
    }

    // =================== 客户端管理 ===================

    @Test
    public void testCreateSocialClient_success() {
        // 准备参数
        SocialClientSaveReqVO reqVO = randomPojo(SocialClientSaveReqVO.class,
                o -> o.setSocialType(randomEle(SocialTypeEnum.values()).getType())
                        .setUserType(randomEle(UserTypeEnum.values()).getValue())
                        .setStatus(randomCommonStatus()))
                .setId(null); // 防止 id 被赋值

        // 调用
        Long socialClientId = socialClientService.createSocialClient(reqVO);
        // 断言
        assertNotNull(socialClientId);
        // 校验记录的属性是否正确
        SocialClientDO socialClient = socialClientMapper.selectById(socialClientId);
        assertPojoEquals(reqVO, socialClient, "id");
    }

    @Test
    public void testUpdateSocialClient_success() {
        // mock 数据
        SocialClientDO dbSocialClient = randomPojo(SocialClientDO.class);
        socialClientMapper.insert(dbSocialClient);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SocialClientSaveReqVO reqVO = randomPojo(SocialClientSaveReqVO.class, o -> {
            o.setId(dbSocialClient.getId()); // 设置更新的 ID
            o.setSocialType(randomEle(SocialTypeEnum.values()).getType())
                    .setUserType(randomEle(UserTypeEnum.values()).getValue())
                    .setStatus(randomCommonStatus());
        });

        // 调用
        socialClientService.updateSocialClient(reqVO);
        // 校验是否更新正确
        SocialClientDO socialClient = socialClientMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, socialClient);
    }

    @Test
    public void testUpdateSocialClient_notExists() {
        // 准备参数
        SocialClientSaveReqVO reqVO = randomPojo(SocialClientSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> socialClientService.updateSocialClient(reqVO), SOCIAL_CLIENT_NOT_EXISTS);
    }

    @Test
    public void testDeleteSocialClient_success() {
        // mock 数据
        SocialClientDO dbSocialClient = randomPojo(SocialClientDO.class);
        socialClientMapper.insert(dbSocialClient);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbSocialClient.getId();

        // 调用
        socialClientService.deleteSocialClient(id);
        // 校验数据不存在了
        assertNull(socialClientMapper.selectById(id));
    }

    @Test
    public void testDeleteSocialClient_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> socialClientService.deleteSocialClient(id), SOCIAL_CLIENT_NOT_EXISTS);
    }

    @Test
    public void testGetSocialClient() {
        // mock 数据
        SocialClientDO dbSocialClient = randomPojo(SocialClientDO.class);
        socialClientMapper.insert(dbSocialClient);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbSocialClient.getId();

        // 调用
        SocialClientDO socialClient = socialClientService.getSocialClient(id);
        // 校验数据正确
        assertPojoEquals(dbSocialClient, socialClient);
    }

    @Test
    public void testGetSocialClientPage() {
        // mock 数据
        SocialClientDO dbSocialClient = randomPojo(SocialClientDO.class, o -> { // 等会查询到
            o.setName("芋头");
            o.setSocialType(SocialTypeEnum.GITEE.getType());
            o.setUserType(UserTypeEnum.ADMIN.getValue());
            o.setClientId("yudao");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        socialClientMapper.insert(dbSocialClient);
        // 测试 name 不匹配
        socialClientMapper.insert(cloneIgnoreId(dbSocialClient, o -> o.setName(randomString())));
        // 测试 socialType 不匹配
        socialClientMapper.insert(cloneIgnoreId(dbSocialClient, o -> o.setSocialType(SocialTypeEnum.DINGTALK.getType())));
        // 测试 userType 不匹配
        socialClientMapper.insert(cloneIgnoreId(dbSocialClient, o -> o.setUserType(UserTypeEnum.MEMBER.getValue())));
        // 测试 clientId 不匹配
        socialClientMapper.insert(cloneIgnoreId(dbSocialClient, o -> o.setClientId("dao")));
        // 测试 status 不匹配
        socialClientMapper.insert(cloneIgnoreId(dbSocialClient, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 准备参数
        SocialClientPageReqVO reqVO = new SocialClientPageReqVO();
        reqVO.setName("芋");
        reqVO.setSocialType(SocialTypeEnum.GITEE.getType());
        reqVO.setUserType(UserTypeEnum.ADMIN.getValue());
        reqVO.setClientId("yu");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        // 调用
        PageResult<SocialClientDO> pageResult = socialClientService.getSocialClientPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbSocialClient, pageResult.getList().get(0));
    }

    // =================== 微信小程序订单发货 ===================

    @BeforeAll
    public static void setUpUploadShippingBackoff() {
        // 测试场景下把退避数组的每一项改为 1ms，避免拖慢用例（数组引用是 final 但元素可改）
        long[] backoff = (long[]) ReflectUtil.getFieldValue(SocialClientServiceImpl.class,
                "UPLOAD_SHIPPING_INFO_RETRY_BACKOFF_MILLIS");
        for (int i = 0; i < backoff.length; i++) {
            backoff[i] = 1L;
        }
    }

    @Test
    public void testUploadWxaOrderShippingInfo_success() throws WxErrorException {
        // 准备参数
        Integer userType = randomPojo(UserTypeEnum.class).getValue();
        SocialWxaOrderUploadShippingInfoReqDTO reqDTO = randomUploadShippingReqDTO();
        // mock 方法：首次调用就成功
        WxMaOrderShippingService shippingService = mockWxMaOrderShippingService();
        when(shippingService.upload(any(WxMaOrderShippingInfoUploadRequest.class)))
                .thenReturn(new WxMaOrderShippingInfoBaseResponse());

        // 调用
        socialClientService.uploadWxaOrderShippingInfo(userType, reqDTO);
        // 断言：仅调用 1 次，无重试
        verify(shippingService, times(1)).upload(any(WxMaOrderShippingInfoUploadRequest.class));
    }

    @Test
    public void testUploadWxaOrderShippingInfo_retryThenSuccess() throws WxErrorException {
        // 准备参数
        Integer userType = randomPojo(UserTypeEnum.class).getValue();
        SocialWxaOrderUploadShippingInfoReqDTO reqDTO = randomUploadShippingReqDTO();
        // mock 方法：首次抛 10060001，第二次成功
        WxMaOrderShippingService shippingService = mockWxMaOrderShippingService();
        when(shippingService.upload(any(WxMaOrderShippingInfoUploadRequest.class)))
                .thenThrow(buildWxErrorException(10060001))
                .thenReturn(new WxMaOrderShippingInfoBaseResponse());

        // 调用
        socialClientService.uploadWxaOrderShippingInfo(userType, reqDTO);
        // 断言：上传调用了 2 次，触发了 1 次重试
        verify(shippingService, times(2)).upload(any(WxMaOrderShippingInfoUploadRequest.class));
    }

    @Test
    public void testUploadWxaOrderShippingInfo_retryExhausted() throws WxErrorException {
        // 准备参数
        Integer userType = randomPojo(UserTypeEnum.class).getValue();
        SocialWxaOrderUploadShippingInfoReqDTO reqDTO = randomUploadShippingReqDTO();
        // mock 方法：始终抛 10060001
        WxMaOrderShippingService shippingService = mockWxMaOrderShippingService();
        when(shippingService.upload(any(WxMaOrderShippingInfoUploadRequest.class)))
                .thenThrow(buildWxErrorException(10060001));

        // 调用并断言：重试用尽抛业务异常
        assertServiceException(() -> socialClientService.uploadWxaOrderShippingInfo(userType, reqDTO),
                SOCIAL_CLIENT_WEIXIN_MINI_APP_ORDER_UPLOAD_SHIPPING_INFO_ERROR);
        // 断言：共 4 次尝试（1 次首发 + 3 次重试）
        verify(shippingService, times(4)).upload(any(WxMaOrderShippingInfoUploadRequest.class));
    }

    @Test
    public void testUploadWxaOrderShippingInfo_otherErrorNoRetry() throws WxErrorException {
        // 准备参数
        Integer userType = randomPojo(UserTypeEnum.class).getValue();
        SocialWxaOrderUploadShippingInfoReqDTO reqDTO = randomUploadShippingReqDTO();
        // mock 方法：抛非 10060001 错误（如 access_token 失效）
        WxMaOrderShippingService shippingService = mockWxMaOrderShippingService();
        when(shippingService.upload(any(WxMaOrderShippingInfoUploadRequest.class)))
                .thenThrow(buildWxErrorException(40001));

        // 调用并断言：立即抛业务异常，无重试
        assertServiceException(() -> socialClientService.uploadWxaOrderShippingInfo(userType, reqDTO),
                SOCIAL_CLIENT_WEIXIN_MINI_APP_ORDER_UPLOAD_SHIPPING_INFO_ERROR);
        verify(shippingService, times(1)).upload(any(WxMaOrderShippingInfoUploadRequest.class));
    }

    /** 构造一个发货上传请求 */
    private SocialWxaOrderUploadShippingInfoReqDTO randomUploadShippingReqDTO() {
        return randomPojo(SocialWxaOrderUploadShippingInfoReqDTO.class,
                o -> o.setLogisticsType(SocialWxaOrderUploadShippingInfoReqDTO.LOGISTICS_TYPE_EXPRESS));
    }

    /** mock 出 wxMaService.getWxMaOrderShippingService() 的返回值并返回该 mock */
    private WxMaOrderShippingService mockWxMaOrderShippingService() {
        WxMaOrderShippingService shippingService = mock(WxMaOrderShippingService.class);
        when(wxMaService.getWxMaOrderShippingService()).thenReturn(shippingService);
        return shippingService;
    }

    /** 构造指定 errorCode 的 WxErrorException */
    private WxErrorException buildWxErrorException(int errorCode) {
        return new WxErrorException(WxError.builder().errorCode(errorCode).errorMsg("mock error").build());
    }

    @Configuration(proxyBeanMethods = false)
    public static class JustAuthTestConfiguration {

        @Bean
        public JustAuthProperties justAuthProperties() {
            JustAuthProperties properties = new JustAuthProperties();
            properties.setType(new HashMap<>());
            return properties;
        }

        @Bean
        public AuthStateCache authStateCache() {
            return AuthDefaultStateCache.INSTANCE;
        }

    }

}
