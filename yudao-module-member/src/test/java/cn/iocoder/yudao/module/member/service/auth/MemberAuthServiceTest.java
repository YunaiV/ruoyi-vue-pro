package cn.iocoder.yudao.module.member.service.auth;

import cn.iocoder.yudao.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import cn.iocoder.yudao.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenRespDTO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.member.controller.app.auth.vo.AppAuthLoginReqVO;
import cn.iocoder.yudao.module.member.controller.app.auth.vo.AppAuthLoginRespVO;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.service.user.MemberUserService;
import cn.iocoder.yudao.module.system.api.logger.LoginLogApi;
import cn.iocoder.yudao.module.system.api.logger.dto.LoginLogCreateReqDTO;
import cn.iocoder.yudao.module.system.api.sms.SmsCodeApi;
import cn.iocoder.yudao.module.system.api.social.SocialClientApi;
import cn.iocoder.yudao.module.system.api.social.SocialUserApi;
import cn.iocoder.yudao.module.system.enums.logger.LoginLogTypeEnum;
import cn.iocoder.yudao.module.system.enums.logger.LoginResultEnum;
import cn.iocoder.yudao.module.system.enums.oauth2.OAuth2ClientConstants;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.AUTH_LOGIN_BAD_CREDENTIALS;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.AUTH_LOGIN_USER_DISABLED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

// TODO @芋艿：单测的 review，等逻辑都达成一致后
/**
 * {@link MemberAuthService} 的单元测试类
 *
 * @author 宋天
 */
public class MemberAuthServiceTest extends BaseMockitoUnitTest {

    private static final String MOBILE = "15601691300";
    private static final String PASSWORD = "yudao123";

    @InjectMocks
    private MemberAuthServiceImpl authService;

    @Mock
    private MemberUserService userService;
    @Mock
    private SmsCodeApi smsCodeApi;
    @Mock
    private LoginLogApi loginLogApi;
    @Mock
    private OAuth2TokenCommonApi oauth2TokenApi;
    @Mock
    private SocialUserApi socialUserApi;
    @Mock
    private SocialClientApi socialClientApi;

    @Test
    public void testLogin_userNotExists() {
        // 准备参数
        AppAuthLoginReqVO reqVO = AppAuthLoginReqVO.builder().mobile(MOBILE).password(PASSWORD).build();
        // mock 方法：账号不存在
        when(userService.getUserByMobile(eq(MOBILE))).thenReturn(null);

        // 调用，并断言
        assertServiceException(() -> authService.login(reqVO), AUTH_LOGIN_BAD_CREDENTIALS);
        assertLoginLog(null, LoginResultEnum.BAD_CREDENTIALS);
        verify(userService, never()).isPasswordMatch(anyString(), anyString());
        verifyNoTokenIssued();
    }

    @Test
    public void testLogin_badPassword() {
        // 准备参数
        AppAuthLoginReqVO reqVO = AppAuthLoginReqVO.builder().mobile(MOBILE).password(PASSWORD).build();
        // mock 方法：密码不匹配
        MemberUserDO user = randomUserDO(o -> o.setMobile(MOBILE).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(userService.getUserByMobile(eq(MOBILE))).thenReturn(user);
        when(userService.isPasswordMatch(eq(PASSWORD), eq(user.getPassword()))).thenReturn(false);

        // 调用，并断言
        assertServiceException(() -> authService.login(reqVO), AUTH_LOGIN_BAD_CREDENTIALS);
        assertLoginLog(user.getId(), LoginResultEnum.BAD_CREDENTIALS);
        verifyNoTokenIssued();
    }

    @Test
    public void testLogin_userDisabled() {
        // 准备参数
        AppAuthLoginReqVO reqVO = AppAuthLoginReqVO.builder().mobile(MOBILE).password(PASSWORD).build();
        // mock 方法：密码正确，但用户被禁用
        MemberUserDO user = randomUserDO(o -> o.setMobile(MOBILE).setStatus(CommonStatusEnum.DISABLE.getStatus()));
        when(userService.getUserByMobile(eq(MOBILE))).thenReturn(user);
        when(userService.isPasswordMatch(eq(PASSWORD), eq(user.getPassword()))).thenReturn(true);

        // 调用，并断言
        assertServiceException(() -> authService.login(reqVO), AUTH_LOGIN_USER_DISABLED);
        assertLoginLog(user.getId(), LoginResultEnum.USER_DISABLED);
        verifyNoTokenIssued();
    }

    @Test
    public void testLogin_success() {
        // 准备参数
        AppAuthLoginReqVO reqVO = AppAuthLoginReqVO.builder().mobile(MOBILE).password(PASSWORD).build();
        // mock 方法：用户
        MemberUserDO user = randomUserDO(o -> o.setMobile(MOBILE).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(userService.getUserByMobile(eq(MOBILE))).thenReturn(user);
        when(userService.isPasswordMatch(eq(PASSWORD), eq(user.getPassword()))).thenReturn(true);
        // mock 方法：令牌
        OAuth2AccessTokenRespDTO accessToken = new OAuth2AccessTokenRespDTO().setAccessToken("access-token")
                .setRefreshToken("refresh-token").setUserId(user.getId())
                .setUserType(UserTypeEnum.MEMBER.getValue()).setExpiresTime(LocalDateTime.of(2026, 1, 2, 3, 4, 5));
        when(oauth2TokenApi.createAccessToken(argThat(o -> user.getId().equals(o.getUserId())
                && UserTypeEnum.MEMBER.getValue().equals(o.getUserType())
                && OAuth2ClientConstants.CLIENT_ID_DEFAULT.equals(o.getClientId())))).thenReturn(accessToken);

        // 调用
        AppAuthLoginRespVO respVO = authService.login(reqVO);
        // 断言：返回结果
        assertEquals(user.getId(), respVO.getUserId());
        assertEquals("access-token", respVO.getAccessToken());
        assertEquals("refresh-token", respVO.getRefreshToken());
        assertEquals(LocalDateTime.of(2026, 1, 2, 3, 4, 5), respVO.getExpiresTime());
        assertNull(respVO.getOpenid());
        // 断言：登录日志、最后登录时间
        assertLoginLog(user.getId(), LoginResultEnum.SUCCESS);
        verify(userService).updateUserLogin(eq(user.getId()), isNull());
        verify(oauth2TokenApi).createAccessToken(any());
        verify(socialUserApi, never()).bindSocialUser(any());
        verifyNoInteractions(smsCodeApi);
    }

    private void assertLoginLog(Long userId, LoginResultEnum result) {
        ArgumentCaptor<LoginLogCreateReqDTO> captor = ArgumentCaptor.forClass(LoginLogCreateReqDTO.class);
        verify(loginLogApi).createLoginLog(captor.capture());
        LoginLogCreateReqDTO reqDTO = captor.getValue();
        assertEquals(LoginLogTypeEnum.LOGIN_MOBILE.getType(), reqDTO.getLogType());
        assertEquals(result.getResult(), reqDTO.getResult());
        assertEquals(userId, reqDTO.getUserId());
        assertEquals(UserTypeEnum.MEMBER.getValue(), reqDTO.getUserType());
        assertEquals(MOBILE, reqDTO.getUsername());
    }

    private void verifyNoTokenIssued() {
        verify(oauth2TokenApi, never()).createAccessToken(any());
        verify(userService, never()).updateUserLogin(any(), any());
        verifyNoInteractions(socialUserApi, smsCodeApi);
    }

    // TODO 芋艿：后续重构这个单测
//    @Test
//    public void testUpdatePassword_success(){
//        // 准备参数
//        MemberUserDO userDO = randomUserDO();
//        memberUserMapper.insert(userDO);
//
//        // 新密码
//        String newPassword = randomString();
//
//        // 请求实体
//        AppMemberUserUpdatePasswordReqVO reqVO = AppMemberUserUpdatePasswordReqVO.builder()
//                .oldPassword(userDO.getPassword())
//                .password(newPassword)
//                .build();
//
//        // 测试桩
//        // 这两个相等是为了返回ture这个结果
//        when(passwordEncoder.matches(reqVO.getOldPassword(),reqVO.getOldPassword())).thenReturn(true);
//        when(passwordEncoder.encode(newPassword)).thenReturn(newPassword);
//
//        // 更新用户密码
//        authService.updatePassword(userDO.getId(), reqVO);
//        assertEquals(memberUserMapper.selectById(userDO.getId()).getPassword(),newPassword);
//    }

    // TODO 芋艿：后续重构这个单测
//    @Test
//    public void testResetPassword_success(){
//        // 准备参数
//        MemberUserDO userDO = randomUserDO();
//        memberUserMapper.insert(userDO);
//
//        // 随机密码
//        String password = randomNumbers(11);
//        // 随机验证码
//        String code = randomNumbers(4);
//
//        // mock
//        when(passwordEncoder.encode(password)).thenReturn(password);
//
//        // 更新用户密码
//        AppMemberUserResetPasswordReqVO reqVO = new AppMemberUserResetPasswordReqVO();
//        reqVO.setMobile(userDO.getMobile());
//        reqVO.setPassword(password);
//        reqVO.setCode(code);
//
//        authService.resetPassword(reqVO);
//        assertEquals(memberUserMapper.selectById(userDO.getId()).getPassword(),password);
//    }

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
