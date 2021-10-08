package cn.iocoder.yudao.adminserver.modules.system.service.social;

import cn.iocoder.yudao.adminserver.BaseDbAndRedisUnitTest;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.social.SysSocialUserDO;
import cn.iocoder.yudao.adminserver.modules.system.dal.mysql.social.SysSocialUserMapper;
import cn.iocoder.yudao.adminserver.modules.system.dal.redis.social.SysSocialAuthUserRedisDAO;
import cn.iocoder.yudao.adminserver.modules.system.enums.user.SysSocialTypeEnum;
import cn.iocoder.yudao.adminserver.modules.system.service.social.impl.SysSocialServiceImpl;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.test.core.util.RandomUtils;
import com.xkcoding.justauth.AuthRequestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.hutool.core.util.RandomUtil.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link SysSocialServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({SysSocialServiceImpl.class, SysSocialAuthUserRedisDAO.class})
public class SysSocialServiceTest extends BaseDbAndRedisUnitTest {

    @Resource
    private SysSocialServiceImpl socialService;

    @Resource
    private SysSocialUserMapper socialUserMapper;

    @MockBean
    private AuthRequestFactory authRequestFactory;

    @Test
    public void testBindSocialUser_create() {
        // mock 数据
        // 准备参数
        // mock 方法

        // 调用
        // 断言
    }

    /**
     * 情况一，如果新老的 unionId 是一致的，无需解绑
     */
    @Test
    public void testUnbindOldSocialUser_no() {
        // mock 数据
        SysSocialUserDO oldSocialUser = RandomUtils.randomPojo(SysSocialUserDO.class, socialUserDO -> {
            socialUserDO.setUserType(UserTypeEnum.ADMIN.getValue());
            socialUserDO.setType(randomEle(SysSocialTypeEnum.values()).getType());
        });
        socialUserMapper.insert(oldSocialUser);
        // 准备参数
        Long userId = oldSocialUser.getUserId();
        Integer type = oldSocialUser.getType();
        String newUnionId = oldSocialUser.getUnionId();

        // 调用
        socialService.unbindOldSocialUser(userId, type, newUnionId);
        // 断言
        assertEquals(1L, socialUserMapper.selectCount(null).longValue());
    }


    /**
     * 情况二，如果新老的 unionId 不一致的，需解绑
     */
    @Test
    public void testUnbindOldSocialUser_yes() {
        // mock 数据
        SysSocialUserDO oldSocialUser = RandomUtils.randomPojo(SysSocialUserDO.class, socialUserDO -> {
            socialUserDO.setUserType(UserTypeEnum.ADMIN.getValue());
            socialUserDO.setType(randomEle(SysSocialTypeEnum.values()).getType());
        });
        socialUserMapper.insert(oldSocialUser);
        // 准备参数
        Long userId = oldSocialUser.getUserId();
        Integer type = oldSocialUser.getType();
        String newUnionId = randomString(10);

        // 调用
        socialService.unbindOldSocialUser(userId, type, newUnionId);
        // 断言
        assertEquals(0L, socialUserMapper.selectCount(null).longValue());
    }

}
