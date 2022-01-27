package cn.iocoder.yudao.module.member.service.user;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.coreservice.modules.infra.service.file.InfFileCoreService;
import cn.iocoder.yudao.coreservice.modules.member.dal.dataobject.user.MbrUserDO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.redis.config.YudaoRedisAutoConfiguration;
import cn.iocoder.yudao.userserver.BaseDbAndRedisUnitTest;
import cn.iocoder.yudao.userserver.modules.member.controller.user.vo.MbrUserInfoRespVO;
import cn.iocoder.yudao.userserver.modules.member.controller.user.vo.MbrUserUpdateMobileReqVO;
import cn.iocoder.yudao.userserver.modules.member.dal.mysql.user.MbrUserMapper;
import cn.iocoder.yudao.userserver.modules.member.service.user.impl.MbrUserServiceImpl;
import cn.iocoder.yudao.userserver.modules.system.dal.dataobject.sms.SysSmsCodeDO;
import cn.iocoder.yudao.userserver.modules.system.service.auth.impl.SysAuthServiceImpl;
import cn.iocoder.yudao.userserver.modules.system.service.sms.SysSmsCodeService;
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
import static cn.iocoder.yudao.userserver.modules.system.enums.sms.SysSmsSceneEnum.CHANGE_MOBILE_BY_SMS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

// TODO @芋艿：单测的 review，等逻辑都达成一致后
/**
 * {@link MbrUserServiceImpl} 的单元测试类
 *
 * @author 宋天
 */
@Import({MbrUserServiceImpl.class, YudaoRedisAutoConfiguration.class})
public class MbrUserServiceImplTest extends BaseDbAndRedisUnitTest {

    @Resource
    private MbrUserServiceImpl mbrUserService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private MbrUserMapper userMapper;

    @MockBean
    private SysAuthServiceImpl authService;

    @MockBean
    private InfFileCoreService fileCoreService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private SysSmsCodeService sysSmsCodeService;

    @Test
    public void testUpdateNickName_success(){
        // mock 数据
        MbrUserDO userDO = randomMbrUserDO();
        userMapper.insert(userDO);

        // 随机昵称
        String newNickName = randomString();

        // 调用接口修改昵称
        mbrUserService.updateNickname(userDO.getId(),newNickName);
        // 查询新修改后的昵称
        String nickname = mbrUserService.getUser(userDO.getId()).getNickname();
        // 断言
        assertEquals(newNickName,nickname);
    }

    @Test
    public void testGetUserInfo_success(){
        // mock 数据
        MbrUserDO userDO = randomMbrUserDO();
        userMapper.insert(userDO);

        // 查询用户昵称与头像
        MbrUserInfoRespVO userInfo = mbrUserService.getUserInfo(userDO.getId());
        System.out.println(userInfo);
    }

    @Test
    public void testUpdateAvatar_success(){
        // mock 数据
        MbrUserDO dbUser = randomMbrUserDO();
        userMapper.insert(dbUser);

        // 准备参数
        Long userId = dbUser.getId();
        byte[] avatarFileBytes = randomBytes(10);
        ByteArrayInputStream avatarFile = new ByteArrayInputStream(avatarFileBytes);
        // mock 方法
        String avatar = randomString();
        when(fileCoreService.createFile(anyString(), eq(avatarFileBytes))).thenReturn(avatar);
        // 调用
        String str = mbrUserService.updateAvatar(userId, avatarFile);
        // 断言
        assertEquals(avatar, str);
    }

    @Test
    public void updateMobile_success(){
        // mock数据
        String oldMobile = randomNumbers(11);
        MbrUserDO userDO = randomMbrUserDO();
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
        MbrUserUpdateMobileReqVO reqVO = new MbrUserUpdateMobileReqVO();
        reqVO.setMobile(newMobile);
        reqVO.setCode(newCode);
        reqVO.setOldMobile(oldMobile);
        reqVO.setOldCode(oldCode);
        mbrUserService.updateMobile(userDO.getId(),reqVO);

        assertEquals(mbrUserService.getUser(userDO.getId()).getMobile(),newMobile);
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static MbrUserDO randomMbrUserDO(Consumer<MbrUserDO>... consumers) {
        Consumer<MbrUserDO> consumer = (o) -> {
            o.setStatus(randomEle(CommonStatusEnum.values()).getStatus()); // 保证 status 的范围
        };
        return randomPojo(MbrUserDO.class, ArrayUtils.append(consumer, consumers));
    }

}
