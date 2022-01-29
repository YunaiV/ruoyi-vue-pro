package cn.iocoder.yudao.module.member.service.user;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.coreservice.modules.infra.service.file.InfFileCoreService;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.redis.config.YudaoRedisAutoConfiguration;
import cn.iocoder.yudao.module.member.controller.app.user.vo.AppUserUpdateMobileReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.sms.SysSmsCodeDO;
import cn.iocoder.yudao.module.member.dal.dataobject.user.UserDO;
import cn.iocoder.yudao.module.member.dal.mysql.user.UserMapper;
import cn.iocoder.yudao.module.member.enums.sms.SysSmsSceneEnum;
import cn.iocoder.yudao.module.member.service.auth.AuthServiceImpl;
import cn.iocoder.yudao.module.member.service.sms.SysSmsCodeService;
import cn.iocoder.yudao.module.member.test.BaseDbAndRedisUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.util.function.Consumer;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

// TODO @芋艿：单测的 review，等逻辑都达成一致后
/**
 * {@link UserServiceImpl} 的单元测试类
 *
 * @author 宋天
 */
@Import({UserServiceImpl.class, YudaoRedisAutoConfiguration.class})
public class MbrUserServiceImplTest extends BaseDbAndRedisUnitTest {

    @Resource
    private UserServiceImpl mbrUserService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserMapper userMapper;

    @MockBean
    private AuthServiceImpl authService;

    @MockBean
    private InfFileCoreService fileCoreService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private SysSmsCodeService sysSmsCodeService;

    @Test
    public void testUpdateNickName_success(){
        // mock 数据
        UserDO userDO = randomUserDO();
        userMapper.insert(userDO);

        // 随机昵称
        String newNickName = randomString();

        // 调用接口修改昵称
        mbrUserService.updateUserNickname(userDO.getId(),newNickName);
        // 查询新修改后的昵称
        String nickname = mbrUserService.getUser(userDO.getId()).getNickname();
        // 断言
        assertEquals(newNickName,nickname);
    }

    @Test
    public void testUpdateAvatar_success(){
        // mock 数据
        UserDO dbUser = randomUserDO();
        userMapper.insert(dbUser);

        // 准备参数
        Long userId = dbUser.getId();
        byte[] avatarFileBytes = randomBytes(10);
        ByteArrayInputStream avatarFile = new ByteArrayInputStream(avatarFileBytes);
        // mock 方法
        String avatar = randomString();
        when(fileCoreService.createFile(anyString(), eq(avatarFileBytes))).thenReturn(avatar);
        // 调用
        String str = mbrUserService.updateUserAvatar(userId, avatarFile);
        // 断言
        assertEquals(avatar, str);
    }

    @Test
    public void updateMobile_success(){
        // mock数据
        String oldMobile = randomNumbers(11);
        UserDO userDO = randomUserDO();
        userDO.setMobile(oldMobile);
        userMapper.insert(userDO);

        // 旧手机和旧验证码
        SysSmsCodeDO codeDO = new SysSmsCodeDO();
        String oldCode = RandomUtil.randomString(4);
        codeDO.setMobile(userDO.getMobile());
        codeDO.setCode(oldCode);
        codeDO.setScene(SysSmsSceneEnum.CHANGE_MOBILE_BY_SMS.getScene());
        codeDO.setUsed(Boolean.FALSE);
        when(sysSmsCodeService.checkCodeIsExpired(codeDO.getMobile(),codeDO.getCode(),codeDO.getScene())).thenReturn(codeDO);

        // 更新手机号
        String newMobile = randomNumbers(11);
        String newCode = randomNumbers(4);
        AppUserUpdateMobileReqVO reqVO = new AppUserUpdateMobileReqVO();
        reqVO.setMobile(newMobile);
        reqVO.setCode(newCode);
        reqVO.setOldMobile(oldMobile);
        reqVO.setOldCode(oldCode);
        mbrUserService.updateUserMobile(userDO.getId(),reqVO);

        assertEquals(mbrUserService.getUser(userDO.getId()).getMobile(),newMobile);
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static UserDO randomUserDO(Consumer<UserDO>... consumers) {
        Consumer<UserDO> consumer = (o) -> {
            o.setStatus(randomEle(CommonStatusEnum.values()).getStatus()); // 保证 status 的范围
        };
        return randomPojo(UserDO.class, ArrayUtils.append(consumer, consumers));
    }

}
