package cn.iocoder.yudao.module.member.service.auth;

import cn.iocoder.yudao.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.member.controller.app.auth.vo.AppAuthSocialLoginReqVO;
import cn.iocoder.yudao.module.member.controller.app.auth.vo.AppAuthWeixinMiniAppLoginReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.service.user.MemberUserService;
import cn.iocoder.yudao.module.system.api.logger.LoginLogApi;
import cn.iocoder.yudao.module.system.api.social.SocialClientApi;
import cn.iocoder.yudao.module.system.api.social.SocialUserApi;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserRespDTO;
import cn.iocoder.yudao.module.system.api.social.dto.SocialWxPhoneNumberInfoRespDTO;
import cn.iocoder.yudao.module.system.enums.logger.LoginLogTypeEnum;
import cn.iocoder.yudao.module.system.enums.logger.LoginResultEnum;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.AUTH_LOGIN_USER_DISABLED;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MemberAuthServiceImplStatusTest extends BaseMockitoUnitTest {

    @InjectMocks
    private MemberAuthServiceImpl authService;

    @Mock
    private MemberUserService userService;
    @Mock
    private LoginLogApi loginLogApi;
    @Mock
    private SocialUserApi socialUserApi;
    @Mock
    private SocialClientApi socialClientApi;
    @Mock
    private OAuth2TokenCommonApi oauth2TokenApi;

    @Test
    void testSocialLogin_userDisabled() {
        // 准备参数
        AppAuthSocialLoginReqVO reqVO = AppAuthSocialLoginReqVO.builder()
                .type(SocialTypeEnum.GITEE.getType()).code("code").state("state").build();
        MemberUserDO user = MemberUserDO.builder().id(1L).mobile("15601691300")
                .status(CommonStatusEnum.DISABLE.getStatus()).build();
        when(socialUserApi.getSocialUserByCode(UserTypeEnum.MEMBER.getValue(), reqVO.getType(),
                reqVO.getCode(), reqVO.getState()))
                .thenReturn(new SocialUserRespDTO("openid", "nickname", "avatar", user.getId()));
        when(userService.getUser(user.getId())).thenReturn(user);

        // 调用，并断言
        assertServiceException(() -> authService.socialLogin(reqVO), AUTH_LOGIN_USER_DISABLED);
        verify(loginLogApi).createLoginLog(argThat(o ->
                o.getLogType().equals(LoginLogTypeEnum.LOGIN_SOCIAL.getType())
                        && o.getResult().equals(LoginResultEnum.USER_DISABLED.getResult())
                        && o.getUserId().equals(user.getId())));
        verify(oauth2TokenApi, never()).createAccessToken(any());
    }

    @Test
    void testWeixinMiniAppLogin_userDisabled() {
        // 准备参数
        AppAuthWeixinMiniAppLoginReqVO reqVO = AppAuthWeixinMiniAppLoginReqVO.builder()
                .phoneCode("phone-code").loginCode("login-code").state("state").build();
        SocialWxPhoneNumberInfoRespDTO phoneNumberInfo = new SocialWxPhoneNumberInfoRespDTO();
        phoneNumberInfo.setPurePhoneNumber("15601691300");
        when(socialClientApi.getWxMaPhoneNumberInfo(UserTypeEnum.MEMBER.getValue(), reqVO.getPhoneCode()))
                .thenReturn(phoneNumberInfo);
        MemberUserDO user = MemberUserDO.builder().id(1L).mobile(phoneNumberInfo.getPurePhoneNumber())
                .status(CommonStatusEnum.DISABLE.getStatus()).build();
        when(userService.createUserIfAbsent(eq(phoneNumberInfo.getPurePhoneNumber()), isNull(), anyInt()))
                .thenReturn(user);

        // 调用，并断言
        assertServiceException(() -> authService.weixinMiniAppLogin(reqVO), AUTH_LOGIN_USER_DISABLED);
        verify(loginLogApi).createLoginLog(argThat(o ->
                o.getLogType().equals(LoginLogTypeEnum.LOGIN_SOCIAL.getType())
                        && o.getResult().equals(LoginResultEnum.USER_DISABLED.getResult())
                        && o.getUserId().equals(user.getId())));
        verify(socialUserApi, never()).bindSocialUser(any());
        verify(oauth2TokenApi, never()).createAccessToken(any());
    }

}
