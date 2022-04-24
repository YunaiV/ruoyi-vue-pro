package cn.iocoder.yudao.module.system.service.social;

import cn.iocoder.yudao.module.system.dal.mysql.social.SocialUserMapper;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbAndRedisUnitTest;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.xkcoding.justauth.AuthRequestFactory;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.hutool.core.util.RandomUtil.randomString;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@Import(SocialUserServiceImpl.class)
public class SocialUserServiceTest extends BaseDbAndRedisUnitTest {

    @Resource
    private SocialUserServiceImpl socialUserService;

    @Resource
    private SocialUserMapper socialUserMapper;

    @MockBean
    private AuthRequestFactory authRequestFactory;

    @Test
    public void testGetAuthorizeUrl() {
        try (MockedStatic<AuthStateUtils> authStateUtilsMock = mockStatic(AuthStateUtils.class)) {
            // 准备参数
            Integer type = 31;
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

//    /**
//     * 情况一，创建 SocialUserDO 的情况
//     */
//    @Test
//    public void testBindSocialUser_create() {
//        // mock 数据
//        // 准备参数
//        Long userId = randomLongId();
//        Integer type = randomEle(SocialTypeEnum.values()).getType();
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
//    private void assertBindSocialUser(SocialUserDO socialUser, AuthUser authUser, Long userId,
//                                      Integer type) {
//        assertEquals(authUser.getToken().getAccessToken(), socialUser.getToken());
//        assertEquals(toJsonString(authUser.getToken()), socialUser.getRawTokenInfo());
//        assertEquals(authUser.getNickname(), socialUser.getNickname());
//        assertEquals(authUser.getAvatar(), socialUser.getAvatar());
//        assertEquals(toJsonString(authUser.getRawUserInfo()), socialUser.getRawUserInfo());
//        assertEquals(userId, socialUser.getUserId());
//        assertEquals(UserTypeEnum.ADMIN.getValue(), socialUser.getUserType());
//        assertEquals(type, socialUser.getType());
//        assertEquals(authUser.getUuid(), socialUser.getOpenid());
//        assertEquals(socialService.getAuthUserUnionId(authUser), socialUser.getUnionId());
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
