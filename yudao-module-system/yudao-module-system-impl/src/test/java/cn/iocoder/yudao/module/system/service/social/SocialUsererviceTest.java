package cn.iocoder.yudao.module.system.service.social;

import cn.iocoder.yudao.module.system.dal.dataobject.social.SysSocialUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.social.SysSocialUserMapper;
import cn.iocoder.yudao.module.system.dal.redis.social.SocialAuthUserRedisDAO;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.system.test.BaseDbAndRedisUnitTest;
import com.xkcoding.justauth.AuthRequestFactory;
import me.zhyd.oauth.model.AuthUser;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.List;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.hutool.core.util.RandomUtil.randomString;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Import({SocialUserServiceImpl.class, SocialAuthUserRedisDAO.class})
public class SocialUsererviceTest extends BaseDbAndRedisUnitTest {

    @Resource
    private SocialUserServiceImpl socialService;

    @Resource
    private SysSocialUserMapper socialUserMapper;

    @MockBean
    private AuthRequestFactory authRequestFactory;

    /**
     * 情况一，创建 SysSocialUserDO 的情况
     */
    @Test
    public void testBindSocialUser_create() {
        // mock 数据
        // 准备参数
        Long userId = randomLongId();
        Integer type = randomEle(SocialTypeEnum.values()).getType();
        AuthUser authUser = randomPojo(AuthUser.class);
        // mock 方法

        // 调用
        socialService.bindSocialUser(userId, type, authUser, UserTypeEnum.ADMIN);
        // 断言
        List<SysSocialUserDO> socialUsers = socialUserMapper.selectList("user_id", userId);
        assertEquals(1, socialUsers.size());
        assertBindSocialUser(socialUsers.get(0), authUser, userId, type);
    }

    /**
     * 情况二，更新 SysSocialUserDO 的情况
     */
    @Test
    public void testBindSocialUser_update() {
        // mock 数据
        SysSocialUserDO dbSocialUser = randomPojo(SysSocialUserDO.class, socialUserDO -> {
            socialUserDO.setUserType(UserTypeEnum.ADMIN.getValue());
            socialUserDO.setType(randomEle(SocialTypeEnum.values()).getType());
        });
        socialUserMapper.insert(dbSocialUser);
        // 准备参数
        Long userId = dbSocialUser.getUserId();
        Integer type = dbSocialUser.getType();
        AuthUser authUser = randomPojo(AuthUser.class);
        // mock 方法

        // 调用
        socialService.bindSocialUser(userId, type, authUser, UserTypeEnum.ADMIN);
        // 断言
        List<SysSocialUserDO> socialUsers = socialUserMapper.selectList("user_id", userId);
        assertEquals(1, socialUsers.size());
        assertBindSocialUser(socialUsers.get(0), authUser, userId, type);
    }

    /**
     * 情况一和二都存在的，逻辑二的场景
     */
    @Test
    public void testBindSocialUser_userId() {
        // mock 数据
        SysSocialUserDO dbSocialUser = randomPojo(SysSocialUserDO.class, socialUserDO -> {
            socialUserDO.setUserType(UserTypeEnum.ADMIN.getValue());
            socialUserDO.setType(randomEle(SocialTypeEnum.values()).getType());
        });
        socialUserMapper.insert(dbSocialUser);
        // 准备参数
        Long userId = randomLongId();
        Integer type = dbSocialUser.getType();
        AuthUser authUser = randomPojo(AuthUser.class);
        // mock 方法

        // 调用
        socialService.bindSocialUser(userId, type, authUser, UserTypeEnum.ADMIN);
        // 断言
        List<SysSocialUserDO> socialUsers = socialUserMapper.selectList("user_id", userId);
        assertEquals(1, socialUsers.size());
    }

    private void assertBindSocialUser(SysSocialUserDO socialUser, AuthUser authUser, Long userId,
                                             Integer type) {
        assertEquals(authUser.getToken().getAccessToken(), socialUser.getToken());
        assertEquals(toJsonString(authUser.getToken()), socialUser.getRawTokenInfo());
        assertEquals(authUser.getNickname(), socialUser.getNickname());
        assertEquals(authUser.getAvatar(), socialUser.getAvatar());
        assertEquals(toJsonString(authUser.getRawUserInfo()), socialUser.getRawUserInfo());
        assertEquals(userId, socialUser.getUserId());
        assertEquals(UserTypeEnum.ADMIN.getValue(), socialUser.getUserType());
        assertEquals(type, socialUser.getType());
        assertEquals(authUser.getUuid(), socialUser.getOpenid());
        assertEquals(socialService.getAuthUserUnionId(authUser), socialUser.getUnionId());
    }

    /**
     * 情况一，如果新老的 unionId 是一致的，无需解绑
     */
    @Test
    public void testUnbindOldSocialUser_no() {
        // mock 数据
        SysSocialUserDO oldSocialUser = randomPojo(SysSocialUserDO.class, socialUserDO -> {
            socialUserDO.setUserType(UserTypeEnum.ADMIN.getValue());
            socialUserDO.setType(randomEle(SocialTypeEnum.values()).getType());
        });
        socialUserMapper.insert(oldSocialUser);
        // 准备参数
        Long userId = oldSocialUser.getUserId();
        Integer type = oldSocialUser.getType();
        String newUnionId = oldSocialUser.getUnionId();

        // 调用
        socialService.unbindOldSocialUser(userId, type, newUnionId, UserTypeEnum.ADMIN);
        // 断言
        assertEquals(1L, socialUserMapper.selectCount(null).longValue());
    }


    /**
     * 情况二，如果新老的 unionId 不一致的，需解绑
     */
    @Test
    public void testUnbindOldSocialUser_yes() {
        // mock 数据
        SysSocialUserDO oldSocialUser = randomPojo(SysSocialUserDO.class, socialUserDO -> {
            socialUserDO.setUserType(UserTypeEnum.ADMIN.getValue());
            socialUserDO.setType(randomEle(SocialTypeEnum.values()).getType());
        });
        socialUserMapper.insert(oldSocialUser);
        // 准备参数
        Long userId = oldSocialUser.getUserId();
        Integer type = oldSocialUser.getType();
        String newUnionId = randomString(10);

        // 调用
        socialService.unbindOldSocialUser(userId, type, newUnionId, UserTypeEnum.ADMIN);
        // 断言
        assertEquals(0L, socialUserMapper.selectCount(null).longValue());
    }

}
