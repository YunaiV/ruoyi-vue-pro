package cn.iocoder.yudao.module.member.service.auth;

import cn.iocoder.yudao.coreservice.modules.system.service.auth.SysUserSessionCoreService;
import cn.iocoder.yudao.coreservice.modules.system.service.logger.SysLoginLogCoreService;
import cn.iocoder.yudao.coreservice.modules.system.service.social.SysSocialCoreService;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.redis.config.YudaoRedisAutoConfiguration;
import cn.iocoder.yudao.module.member.controller.app.auth.vo.AppAuthResetPasswordReqVO;
import cn.iocoder.yudao.module.member.controller.app.auth.vo.AppAuthUpdatePasswordReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.user.UserDO;
import cn.iocoder.yudao.module.member.dal.mysql.user.UserMapper;
import cn.iocoder.yudao.module.member.service.sms.SysSmsCodeService;
import cn.iocoder.yudao.module.member.service.user.UserService;
import cn.iocoder.yudao.module.member.test.BaseDbAndRedisUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;
import java.util.function.Consumer;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.hutool.core.util.RandomUtil.randomNumbers;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

// TODO @芋艿：单测的 review，等逻辑都达成一致后
/**
 * {@link AuthService} 的单元测试类
 *
 * @author 宋天
 */
@Import({AuthServiceImpl.class, YudaoRedisAutoConfiguration.class})
public class SysAuthServiceTest extends BaseDbAndRedisUnitTest {

    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private UserService userService;
    @MockBean
    private SysSmsCodeService smsCodeService;
    @MockBean
    private SysLoginLogCoreService loginLogCoreService;
    @MockBean
    private SysUserSessionCoreService userSessionCoreService;
    @MockBean
    private SysSocialCoreService socialService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @Resource
    private UserMapper mbrUserMapper;
    @Resource
    private AuthServiceImpl authService;

    @Test
    public void testUpdatePassword_success(){
        // 准备参数
        UserDO userDO = randomUserDO();
        mbrUserMapper.insert(userDO);

        // 新密码
        String newPassword = randomString();

        // 请求实体
        AppAuthUpdatePasswordReqVO reqVO = AppAuthUpdatePasswordReqVO.builder()
                .oldPassword(userDO.getPassword())
                .password(newPassword)
                .build();

        // 测试桩
        // 这两个相等是为了返回ture这个结果
        when(passwordEncoder.matches(reqVO.getOldPassword(),reqVO.getOldPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(newPassword);

        // 更新用户密码
        authService.updatePassword(userDO.getId(), reqVO);
        assertEquals(mbrUserMapper.selectById(userDO.getId()).getPassword(),newPassword);
    }

    @Test
    public void testResetPassword_success(){
        // 准备参数
        UserDO userDO = randomUserDO();
        mbrUserMapper.insert(userDO);

        // 随机密码
        String password = randomNumbers(11);
        // 随机验证码
        String code = randomNumbers(4);

        // mock
        when(passwordEncoder.encode(password)).thenReturn(password);

        // 更新用户密码
        AppAuthResetPasswordReqVO reqVO = new AppAuthResetPasswordReqVO();
        reqVO.setMobile(userDO.getMobile());
        reqVO.setPassword(password);
        reqVO.setCode(code);

        authService.resetPassword(reqVO);
        assertEquals(mbrUserMapper.selectById(userDO.getId()).getPassword(),password);
    }


    // ========== 随机对象 ==========

    @SafeVarargs
    private static UserDO randomUserDO(Consumer<UserDO>... consumers) {
        Consumer<UserDO> consumer = (o) -> {
            o.setStatus(randomEle(CommonStatusEnum.values()).getStatus()); // 保证 status 的范围
            o.setPassword(randomString());
        };
        return randomPojo(UserDO.class, ArrayUtils.append(consumer, consumers));
    }


}
