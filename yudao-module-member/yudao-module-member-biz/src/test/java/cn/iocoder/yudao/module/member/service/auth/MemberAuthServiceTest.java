package cn.iocoder.yudao.module.member.service.auth;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.redis.config.YudaoRedisAutoConfiguration;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbAndRedisUnitTest;
import cn.iocoder.yudao.module.member.controller.app.auth.vo.AppAuthResetPasswordReqVO;
import cn.iocoder.yudao.module.member.controller.app.auth.vo.AppAuthUpdatePasswordReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.dal.mysql.user.MemberUserMapper;
import cn.iocoder.yudao.module.member.service.user.MemberUserService;
import cn.iocoder.yudao.module.system.api.oauth2.OAuth2TokenApi;
import cn.iocoder.yudao.module.system.api.logger.LoginLogApi;
import cn.iocoder.yudao.module.system.api.sms.SmsCodeApi;
import cn.iocoder.yudao.module.system.api.social.SocialUserApi;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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
 * {@link MemberAuthService} 的单元测试类
 *
 * @author 宋天
 */
@Import({MemberAuthServiceImpl.class, YudaoRedisAutoConfiguration.class})
public class MemberAuthServiceTest extends BaseDbAndRedisUnitTest {

    // TODO @芋艿：登录相关的单测，待补全

    @MockBean
    private MemberUserService userService;
    @MockBean
    private SmsCodeApi smsCodeApi;
    @MockBean
    private LoginLogApi loginLogApi;
    @MockBean
    private OAuth2TokenApi oauth2TokenApi;
    @MockBean
    private SocialUserApi socialUserApi;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @Resource
    private MemberUserMapper memberUserMapper;
    @Resource
    private MemberAuthServiceImpl authService;

    @Test
    public void testUpdatePassword_success(){
        // 准备参数
        MemberUserDO userDO = randomUserDO();
        memberUserMapper.insert(userDO);

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
        assertEquals(memberUserMapper.selectById(userDO.getId()).getPassword(),newPassword);
    }

    @Test
    public void testResetPassword_success(){
        // 准备参数
        MemberUserDO userDO = randomUserDO();
        memberUserMapper.insert(userDO);

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
        assertEquals(memberUserMapper.selectById(userDO.getId()).getPassword(),password);
    }


    // ========== 随机对象 ==========

    @SafeVarargs
    private static MemberUserDO randomUserDO(Consumer<MemberUserDO>... consumers) {
        Consumer<MemberUserDO> consumer = (o) -> {
            o.setStatus(randomEle(CommonStatusEnum.values()).getStatus()); // 保证 status 的范围
            o.setPassword(randomString());
        };
        return randomPojo(MemberUserDO.class, ArrayUtils.append(consumer, consumers));
    }


}
