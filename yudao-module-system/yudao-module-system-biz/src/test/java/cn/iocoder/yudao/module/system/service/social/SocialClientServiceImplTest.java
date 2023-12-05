package cn.iocoder.yudao.module.system.service.social;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.WxMaUserService;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.system.controller.admin.socail.vo.client.SocialClientPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.socail.vo.client.SocialClientSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.social.SocialClientDO;
import cn.iocoder.yudao.module.system.dal.mysql.social.SocialClientMapper;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import com.binarywang.spring.starter.wxjava.miniapp.properties.WxMaProperties;
import com.binarywang.spring.starter.wxjava.mp.properties.WxMpProperties;
import com.xingyuv.jushauth.config.AuthConfig;
import com.xingyuv.jushauth.model.AuthResponse;
import com.xingyuv.jushauth.model.AuthUser;
import com.xingyuv.jushauth.request.AuthDefaultRequest;
import com.xingyuv.jushauth.request.AuthRequest;
import com.xingyuv.jushauth.utils.AuthStateUtils;
import com.xingyuv.justauth.AuthRequestFactory;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;

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
@Import(SocialClientServiceImpl.class)
public class SocialClientServiceImplTest extends BaseDbUnitTest {

    @Resource
    private SocialClientServiceImpl socialClientService;

    @Resource
    private SocialClientMapper socialClientMapper;

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

    @Test
    public void testGetAuthorizeUrl() {
        try (MockedStatic<AuthStateUtils> authStateUtilsMock = mockStatic(AuthStateUtils.class)) {
            // 准备参数
            Integer socialType = SocialTypeEnum.WECHAT_MP.getType();
            Integer userType = randomPojo(UserTypeEnum.class).getValue();
            String redirectUri = "sss";
            // mock 获得对应的 AuthRequest 实现
            AuthRequest authRequest = mock(AuthRequest.class);
            when(authRequestFactory.get(eq("WECHAT_MP"))).thenReturn(authRequest);
            // mock 方法
            authStateUtilsMock.when(AuthStateUtils::createState).thenReturn("aoteman");
            when(authRequest.authorize(eq("aoteman"))).thenReturn("https://www.iocoder.cn?redirect_uri=yyy");

            // 调用
            String url = socialClientService.getAuthorizeUrl(socialType, userType, redirectUri);
            // 断言
            assertEquals("https://www.iocoder.cn?redirect_uri=sss", url);
        }
    }

    @Test
    public void testAuthSocialUser_success() {
        // 准备参数
        Integer socialType = SocialTypeEnum.WECHAT_MP.getType();
        Integer userType = randomPojo(UserTypeEnum.class).getValue();
        String code = randomString();
        String state = randomString();
        // mock 方法（AuthRequest）
        AuthRequest authRequest = mock(AuthRequest.class);
        when(authRequestFactory.get(eq("WECHAT_MP"))).thenReturn(authRequest);
        // mock 方法（AuthResponse）
        AuthUser authUser = randomPojo(AuthUser.class);
        AuthResponse<?> authResponse = new AuthResponse<>(2000, null, authUser);
        when(authRequest.login(argThat(authCallback -> {
            assertEquals(code, authCallback.getCode());
            assertEquals(state, authCallback.getState());
            return true;
        }))).thenReturn(authResponse);

        // 调用
        AuthUser result = socialClientService.getAuthUser(socialType, userType, code, state);
        // 断言
        assertSame(authUser, result);
    }

    @Test
    public void testAuthSocialUser_fail() {
        // 准备参数
        Integer socialType = SocialTypeEnum.WECHAT_MP.getType();
        Integer userType = randomPojo(UserTypeEnum.class).getValue();
        String code = randomString();
        String state = randomString();
        // mock 方法（AuthRequest）
        AuthRequest authRequest = mock(AuthRequest.class);
        when(authRequestFactory.get(eq("WECHAT_MP"))).thenReturn(authRequest);
        // mock 方法（AuthResponse）
        AuthResponse<?> authResponse = new AuthResponse<>(0, "模拟失败", null);
        when(authRequest.login(argThat(authCallback -> {
            assertEquals(code, authCallback.getCode());
            assertEquals(state, authCallback.getState());
            return true;
        }))).thenReturn(authResponse);

        // 调用并断言
        assertServiceException(
                () -> socialClientService.getAuthUser(socialType, userType, code, state),
                SOCIAL_USER_AUTH_FAILURE, "模拟失败");
    }

    @Test
    public void testBuildAuthRequest_clientNull() {
        // 准备参数
        Integer socialType = SocialTypeEnum.WECHAT_MP.getType();
        Integer userType = randomPojo(SocialTypeEnum.class).getType();
        // mock 获得对应的 AuthRequest 实现
        AuthRequest authRequest = mock(AuthDefaultRequest.class);
        AuthConfig authConfig = (AuthConfig) ReflectUtil.getFieldValue(authRequest, "config");
        when(authRequestFactory.get(eq("WECHAT_MP"))).thenReturn(authRequest);

        // 调用
        AuthRequest result = socialClientService.buildAuthRequest(socialType, userType);
        // 断言
        assertSame(authRequest, result);
        assertSame(authConfig, ReflectUtil.getFieldValue(authConfig, "config"));
    }

    @Test
    public void testBuildAuthRequest_clientDisable() {
        // 准备参数
        Integer socialType = SocialTypeEnum.WECHAT_MP.getType();
        Integer userType = randomPojo(SocialTypeEnum.class).getType();
        // mock 获得对应的 AuthRequest 实现
        AuthRequest authRequest = mock(AuthDefaultRequest.class);
        AuthConfig authConfig = (AuthConfig) ReflectUtil.getFieldValue(authRequest, "config");
        when(authRequestFactory.get(eq("WECHAT_MP"))).thenReturn(authRequest);
        // mock 数据
        SocialClientDO client = randomPojo(SocialClientDO.class, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())
                .setUserType(userType).setSocialType(socialType));
        socialClientMapper.insert(client);

        // 调用
        AuthRequest result = socialClientService.buildAuthRequest(socialType, userType);
        // 断言
        assertSame(authRequest, result);
        assertSame(authConfig, ReflectUtil.getFieldValue(authConfig, "config"));
    }

    @Test
    public void testBuildAuthRequest_clientEnable() {
        // 准备参数
        Integer socialType = SocialTypeEnum.WECHAT_MP.getType();
        Integer userType = randomPojo(SocialTypeEnum.class).getType();
        // mock 获得对应的 AuthRequest 实现
        AuthConfig authConfig = mock(AuthConfig.class);
        AuthRequest authRequest = mock(AuthDefaultRequest.class);
        ReflectUtil.setFieldValue(authRequest, "config", authConfig);
        when(authRequestFactory.get(eq("WECHAT_MP"))).thenReturn(authRequest);
        // mock 数据
        SocialClientDO client = randomPojo(SocialClientDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setUserType(userType).setSocialType(socialType));
        socialClientMapper.insert(client);

        // 调用
        AuthRequest result = socialClientService.buildAuthRequest(socialType, userType);
        // 断言
        assertSame(authRequest, result);
        assertNotSame(authConfig, ReflectUtil.getFieldValue(authRequest, "config"));
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
        when(userService.getPhoneNoInfo(eq(phoneCode))).thenReturn(phoneNumber);

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
        WxErrorException wxErrorException = randomPojo(WxErrorException.class);
        when(userService.getPhoneNoInfo(eq(phoneCode))).thenThrow(wxErrorException);

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
                .setUserType(userType).setSocialType(SocialTypeEnum.WECHAT_MINI_APP.getType()));
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
                .setUserType(userType).setSocialType(SocialTypeEnum.WECHAT_MINI_APP.getType()));
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

}
