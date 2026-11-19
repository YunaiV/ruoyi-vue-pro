package cn.iocoder.yudao.module.member.service.user;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.member.controller.app.user.vo.AppMemberUserUpdateReqVO;
import cn.iocoder.yudao.module.member.convert.user.MemberUserConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.dal.mysql.user.MemberUserMapper;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class MemberUserIntroductionTest extends BaseMockitoUnitTest {

    @InjectMocks
    private MemberUserServiceImpl userService;
    @Mock
    private MemberUserMapper memberUserMapper;

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"热爱阅读\n分享技术"})
    void shouldUpdateIntroductionWithoutChangingOtherFields(String introduction) {
        when(memberUserMapper.selectById(1L)).thenReturn(MemberUserDO.builder().id(1L).introduction("原简介").build());
        AppMemberUserUpdateReqVO request = new AppMemberUserUpdateReqVO().setIntroduction(introduction);
        userService.updateUser(1L, request);
        verify(memberUserMapper).updateById(argThat((MemberUserDO user) -> user.getId().equals(1L)
                && java.util.Objects.equals(introduction, user.getIntroduction()) && user.getMobile() == null
                && user.getNickname() == null && user.getAreaId() == null));
    }

    @Test
    void shouldLimitIntroductionTo500Characters() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var request = new AppMemberUserUpdateReqVO().setIntroduction("简".repeat(500));
            assertTrue(validator.validate(request).isEmpty());
            request.setIntroduction("简".repeat(501));
            assertTrue(validator.validate(request).stream().anyMatch(v -> v.getPropertyPath().toString().equals("introduction")));
            request.setIntroduction("");
            assertTrue(validator.validate(request).isEmpty());
        }
    }

    @Test
    void shouldReturnIntroductionToAppAndCms() {
        var user = MemberUserDO.builder().id(1L).introduction("热爱阅读").build();
        assertEquals(user.getIntroduction(), MemberUserConvert.INSTANCE.convert(user).getIntroduction());
        assertEquals(user.getIntroduction(), MemberUserConvert.INSTANCE.convert(user, null).getIntroduction());
        assertEquals(user.getIntroduction(), MemberUserConvert.INSTANCE.convert2(user).getIntroduction());
        assertEquals(user.getIntroduction(), MemberUserConvert.INSTANCE.convert03(user).getIntroduction());
    }
}
