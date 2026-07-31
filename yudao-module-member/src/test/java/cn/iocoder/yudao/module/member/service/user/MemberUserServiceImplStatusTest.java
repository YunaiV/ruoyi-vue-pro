package cn.iocoder.yudao.module.member.service.user;

import cn.iocoder.yudao.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.member.controller.admin.user.vo.MemberUserUpdateReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.dal.mysql.user.MemberUserMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MemberUserServiceImplStatusTest extends BaseMockitoUnitTest {

    @InjectMocks
    private MemberUserServiceImpl userService;

    @Mock
    private MemberUserMapper memberUserMapper;
    @Mock
    private OAuth2TokenCommonApi oauth2TokenApi;

    @Test
    void testUpdateUser_disableRevokesToken() {
        // 准备参数
        Long userId = 1L;
        when(memberUserMapper.selectById(userId)).thenReturn(MemberUserDO.builder().id(userId).build());
        MemberUserUpdateReqVO reqVO = new MemberUserUpdateReqVO();
        reqVO.setId(userId);
        reqVO.setStatus(CommonStatusEnum.DISABLE.getStatus().byteValue());

        // 调用
        userService.updateUser(reqVO);

        // 断言
        verify(memberUserMapper).updateById(argThat((MemberUserDO user) -> user.getId().equals(userId)
                && CommonStatusEnum.isDisable(user.getStatus())));
        verify(oauth2TokenApi).removeAccessToken(userId, UserTypeEnum.MEMBER.getValue());
    }

    @Test
    void testUpdateUser_enableKeepsToken() {
        // 准备参数
        Long userId = 1L;
        when(memberUserMapper.selectById(userId)).thenReturn(MemberUserDO.builder().id(userId).build());
        MemberUserUpdateReqVO reqVO = new MemberUserUpdateReqVO();
        reqVO.setId(userId);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus().byteValue());

        // 调用
        userService.updateUser(reqVO);

        // 断言
        verify(oauth2TokenApi, never()).removeAccessToken(userId, UserTypeEnum.MEMBER.getValue());
    }

}
