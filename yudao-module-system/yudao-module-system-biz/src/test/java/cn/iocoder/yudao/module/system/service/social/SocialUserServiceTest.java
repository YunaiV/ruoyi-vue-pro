package cn.iocoder.yudao.module.system.service.social;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.test.core.util.AssertUtils;
import cn.iocoder.yudao.framework.test.core.util.RandomUtils;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserBindReqDTO;
import cn.iocoder.yudao.module.system.dal.dataobject.social.SocialUserBindDO;
import cn.iocoder.yudao.module.system.dal.dataobject.social.SocialUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.social.SocialUserBindMapper;
import cn.iocoder.yudao.module.system.dal.mysql.social.SocialUserMapper;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbAndRedisUnitTest;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.xkcoding.justauth.AuthRequestFactory;
import me.zhyd.oauth.enums.AuthResponseStatus;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.MockedStatic;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import java.util.List;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.SOCIAL_USER_AUTH_FAILURE;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.SOCIAL_USER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@Import(SocialUserServiceImpl.class)
public class SocialUserServiceTest extends BaseDbAndRedisUnitTest {

    @Resource
    private SocialUserServiceImpl socialUserService;

    @Resource
    private SocialUserMapper socialUserMapper;
    @Resource
    private SocialUserBindMapper socialUserBindMapper;

    @MockBean
    private AuthRequestFactory authRequestFactory;

    @Test
    public void testGetAuthorizeUrl() {
        try (MockedStatic<AuthStateUtils> authStateUtilsMock = mockStatic(AuthStateUtils.class)) {
            // 准备参数
            Integer type = SocialTypeEnum.WECHAT_MP.getType();
            String redirectUri = "sss";
            // mock 获得对应的 AuthRequest 实现
            AuthRequest authRequest = mock(AuthRequest.class);
            when(authRequestFactory.get(eq("WECHAT_MP"))).thenReturn(authRequest);
            // mock 方法
            authStateUtilsMock.when(AuthStateUtils::createState).thenReturn("aoteman");
            when(authRequest.authorize(eq("aoteman"))).thenReturn("https://www.iocoder.cn?redirect_uri=yyy");

            // 调用
            String url = socialUserService.getAuthorizeUrl(type, redirectUri);
            // 断言
            assertEquals("https://www.iocoder.cn/?redirect_uri=sss", url);
        }
    }

    @Test
    public void testAuthSocialUser_exists() {
        // 准备参数
        Integer type = SocialTypeEnum.GITEE.getType();
        String code = "tudou";
        String state = "yuanma";
        // mock 方法
        SocialUserDO socialUser = randomPojo(SocialUserDO.class).setType(type).setCode(code).setState(state);
        socialUserMapper.insert(socialUser);

        // 调用
        SocialUserDO result = socialUserService.authSocialUser(type, code, state);
        // 断言
        assertPojoEquals(socialUser, result);
    }

    @Test
    public void testAuthSocialUser_authFailure() {
        // 准备参数
        Integer type = SocialTypeEnum.GITEE.getType();
        // mock 方法
        AuthRequest authRequest = mock(AuthRequest.class);
        when(authRequestFactory.get(anyString())).thenReturn(authRequest);
        AuthResponse<?> authResponse = new AuthResponse<>(0, "模拟失败", null);
        when(authRequest.login(any(AuthCallback.class))).thenReturn(authResponse);

        // 调用并断言
        assertServiceException(
                () -> socialUserService.authSocialUser(type, randomString(10), randomString(10)),
                SOCIAL_USER_AUTH_FAILURE, "模拟失败");
    }

    @Test
    public void testAuthSocialUser_insert() {
        // 准备参数
        Integer type = SocialTypeEnum.GITEE.getType();
        String code = "tudou";
        String state = "yuanma";
        // mock 方法
        AuthRequest authRequest = mock(AuthRequest.class);
        when(authRequestFactory.get(eq(SocialTypeEnum.GITEE.getSource()))).thenReturn(authRequest);
        AuthUser authUser = randomPojo(AuthUser.class);
        AuthResponse<AuthUser> authResponse = new AuthResponse<>(AuthResponseStatus.SUCCESS.getCode(), null, authUser);
        when(authRequest.login(any(AuthCallback.class))).thenReturn(authResponse);

        // 调用
        SocialUserDO result = socialUserService.authSocialUser(type, code, state);
        // 断言
        assertBindSocialUser(type, result, authResponse.getData());
        assertEquals(code, result.getCode());
        assertEquals(state, result.getState());
    }

    @Test
    public void testAuthSocialUser_update() {
        // 准备参数
        Integer type = SocialTypeEnum.GITEE.getType();
        String code = "tudou";
        String state = "yuanma";
        // mock 数据
        socialUserMapper.insert(randomPojo(SocialUserDO.class).setType(type).setOpenid("test_openid"));
        // mock 方法
        AuthRequest authRequest = mock(AuthRequest.class);
        when(authRequestFactory.get(eq(SocialTypeEnum.GITEE.getSource()))).thenReturn(authRequest);
        AuthUser authUser = randomPojo(AuthUser.class);
        authUser.getToken().setOpenId("test_openid");
        AuthResponse<AuthUser> authResponse = new AuthResponse<>(AuthResponseStatus.SUCCESS.getCode(), null, authUser);
        when(authRequest.login(any(AuthCallback.class))).thenReturn(authResponse);

        // 调用
        SocialUserDO result = socialUserService.authSocialUser(type, code, state);
        // 断言
        assertBindSocialUser(type, result, authResponse.getData());
        assertEquals(code, result.getCode());
        assertEquals(state, result.getState());
    }

    private void assertBindSocialUser(Integer type, SocialUserDO socialUser, AuthUser authUser) {
        assertEquals(authUser.getToken().getAccessToken(), socialUser.getToken());
        assertEquals(toJsonString(authUser.getToken()), socialUser.getRawTokenInfo());
        assertEquals(authUser.getNickname(), socialUser.getNickname());
        assertEquals(authUser.getAvatar(), socialUser.getAvatar());
        assertEquals(toJsonString(authUser.getRawUserInfo()), socialUser.getRawUserInfo());
        assertEquals(type, socialUser.getType());
        assertEquals(authUser.getUuid(), socialUser.getOpenid());
        assertEquals(authUser.getToken().getUnionId(), socialUser.getUnionId());
    }

    @Test
    public void testGetSocialUserList() {
        Long userId = 1L;
        Integer userType = UserTypeEnum.ADMIN.getValue();
        // mock 获得绑定
        socialUserBindMapper.insert(randomPojo(SocialUserBindDO.class) // 可被查询到
                .setUserId(userId).setUserType(userType).setPlatform(SocialTypeEnum.GITEE.getPlatform())
                .setUnionId("test_unionId"));
        socialUserBindMapper.insert(randomPojo(SocialUserBindDO.class) // 不可被查询到
                .setUserId(2L).setUserType(userType).setPlatform(SocialTypeEnum.DINGTALK.getPlatform()));
        // mock 获得社交用户
        SocialUserDO socialUser = randomPojo(SocialUserDO.class).setType(SocialTypeEnum.GITEE.getType())
                .setUnionId("test_unionId");
        socialUserMapper.insert(socialUser); // 可被查到
        socialUserMapper.insert(randomPojo(SocialUserDO.class)); // 不可被查到

        // 调用
        List<SocialUserDO> result = socialUserService.getSocialUserList(userId, userType);
        // 断言
        assertEquals(1, result.size());
        assertPojoEquals(socialUser, result.get(0));
    }

    @Test
    public void testBindSocialUser_create() {
        // 准备参数
        SocialUserBindReqDTO reqDTO = new SocialUserBindReqDTO()
                .setUserId(1L).setUserType(UserTypeEnum.ADMIN.getValue())
                .setType(SocialTypeEnum.GITEE.getType()).setCode("test_code").setState("test_state");
        // mock 数据：获得社交用户
        SocialUserDO socialUser = randomPojo(SocialUserDO.class).setType(reqDTO.getType())
                .setCode(reqDTO.getCode()).setState(reqDTO.getState()).setUnionId("test_unionId");
        socialUserMapper.insert(socialUser);
        // mock 数据：解绑其它账号
        socialUserBindMapper.insert(randomPojo(SocialUserBindDO.class).setUserId(1L).setUserType(UserTypeEnum.ADMIN.getValue())
                .setPlatform(SocialTypeEnum.GITEE.getPlatform()).setUnionId("test_delete_unionId"));

        // 调用
        socialUserService.bindSocialUser(reqDTO);
        // 断言
        List<SocialUserBindDO> socialUserBinds = socialUserBindMapper.selectList();
        assertEquals(1, socialUserBinds.size());

    }
//
//    /**
//     * 情况二，更新 SocialUserDO 的情况
//     */
//    @Test
//    public void testBindSocialUser_update() {
//        // mock 数据
//        SocialUserDO dbSocialUser = randomPojo(SocialUserDO.class, socialUserDO -> {
//            socialUserDO.setUserType(UserTypeEnum.ADMIN.getValue());
//            socialUserDO.setType(randomEle(SocialTypeEnum.values()).getType());
//        });
//        socialUserMapper.insert(dbSocialUser);
//        // 准备参数
//        Long userId = dbSocialUser.getUserId();
//        Integer type = dbSocialUser.getType();
//        AuthUser authUser = randomPojo(AuthUser.class);
//        // mock 方法
//
//        // 调用
//        socialService.bindSocialUser(userId, UserTypeEnum.ADMIN.getValue(), type, authUser);
//        // 断言
//        List<SocialUserDO> socialUsers = socialUserMapper.selectList("user_id", userId);
//        assertEquals(1, socialUsers.size());
//        assertBindSocialUser(socialUsers.get(0), authUser, userId, type);
//    }
//
//    /**
//     * 情况一和二都存在的，逻辑二的场景
//     */
//    @Test
//    public void testBindSocialUser_userId() {
//        // mock 数据
//        SocialUserDO dbSocialUser = randomPojo(SocialUserDO.class, socialUserDO -> {
//            socialUserDO.setUserType(UserTypeEnum.ADMIN.getValue());
//            socialUserDO.setType(randomEle(SocialTypeEnum.values()).getType());
//        });
//        socialUserMapper.insert(dbSocialUser);
//        // 准备参数
//        Long userId = randomLongId();
//        Integer type = dbSocialUser.getType();
//        AuthUser authUser = randomPojo(AuthUser.class);
//        // mock 方法
//
//        // 调用
//        socialService.bindSocialUser(userId, UserTypeEnum.ADMIN.getValue(), type, authUser);
//        // 断言
//        List<SocialUserDO> socialUsers = socialUserMapper.selectList("user_id", userId);
//        assertEquals(1, socialUsers.size());
//    }

//
//    /**
//     * 情况一，如果新老的 unionId 是一致的，无需解绑
//     */
//    @Test
//    public void testUnbindOldSocialUser_no() {
//        // mock 数据
//        SocialUserDO oldSocialUser = randomPojo(SocialUserDO.class, socialUserDO -> {
//            socialUserDO.setUserType(UserTypeEnum.ADMIN.getValue());
//            socialUserDO.setType(randomEle(SocialTypeEnum.values()).getType());
//        });
//        socialUserMapper.insert(oldSocialUser);
//        // 准备参数
//        Long userId = oldSocialUser.getUserId();
//        Integer type = oldSocialUser.getType();
//        String newUnionId = oldSocialUser.getUnionId();
//
//        // 调用
//        socialService.unbindOldSocialUser(userId, UserTypeEnum.ADMIN.getValue(), type, newUnionId);
//        // 断言
//        assertEquals(1L, socialUserMapper.selectCount(null).longValue());
//    }
//
//
//    /**
//     * 情况二，如果新老的 unionId 不一致的，需解绑
//     */
//    @Test
//    public void testUnbindOldSocialUser_yes() {
//        // mock 数据
//        SocialUserDO oldSocialUser = randomPojo(SocialUserDO.class, socialUserDO -> {
//            socialUserDO.setUserType(UserTypeEnum.ADMIN.getValue());
//            socialUserDO.setType(randomEle(SocialTypeEnum.values()).getType());
//        });
//        socialUserMapper.insert(oldSocialUser);
//        // 准备参数
//        Long userId = oldSocialUser.getUserId();
//        Integer type = oldSocialUser.getType();
//        String newUnionId = randomString(10);
//
//        // 调用
//        socialService.unbindOldSocialUser(userId, UserTypeEnum.ADMIN.getValue(), type, newUnionId);
//        // 断言
//        assertEquals(0L, socialUserMapper.selectCount(null).longValue());
//    }

}
