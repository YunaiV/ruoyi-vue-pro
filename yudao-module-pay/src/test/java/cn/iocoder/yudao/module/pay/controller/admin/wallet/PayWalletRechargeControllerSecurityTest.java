package cn.iocoder.yudao.module.pay.controller.admin.wallet;

import cn.iocoder.yudao.framework.common.biz.system.permission.PermissionCommonApi;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkServiceImpl;
import cn.iocoder.yudao.module.pay.api.notify.dto.PayOrderNotifyReqDTO;
import cn.iocoder.yudao.module.pay.service.wallet.PayWalletRechargeService;
import javax.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link PayWalletRechargeController} 的方法级权限测试
 *
 * 通过真实的 Spring Security 方法拦截（{@link EnableMethodSecurity}）+ 真实的 {@code @ss} 权限判断，
 * 校验发起钱包充值退款需要 pay:wallet:update-balance 权限，拒绝时不会调用 Service
 *
 * @author 芋道源码
 */
@SpringJUnitConfig(PayWalletRechargeControllerSecurityTest.Config.class)
public class PayWalletRechargeControllerSecurityTest {

    private static final String PERMISSION = "pay:wallet:update-balance";
    private static final Long USER_ID = 1L;
    private static final Long RECHARGE_ID = 1024L;

    @Resource
    private PayWalletRechargeController controller;
    @Resource
    private PayWalletRechargeService walletRechargeService;
    @Resource
    private PermissionCommonApi permissionApi;

    @BeforeEach
    public void setUp() {
        reset(walletRechargeService, permissionApi);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testControllerIsProxied() {
        // 确认测试走的是方法安全代理，而不是裸对象
        assertTrue(AopUtils.isAopProxy(controller));
    }

    @Test
    public void testRefundWalletRecharge_hasPermission() {
        // mock 方法
        loginAdmin();
        when(permissionApi.hasAnyPermissions(eq(USER_ID), eq(PERMISSION))).thenReturn(true);

        // 调用
        assertTrue(controller.refundWalletRecharge(RECHARGE_ID).getData());
        // 断言
        verify(permissionApi).hasAnyPermissions(eq(USER_ID), eq(PERMISSION));
        verify(walletRechargeService).refundWalletRecharge(eq(RECHARGE_ID), isNull());
    }

    @Test
    public void testRefundWalletRecharge_noPermission() {
        // mock 方法
        loginAdmin();
        when(permissionApi.hasAnyPermissions(anyLong(), any(String[].class))).thenReturn(false);

        // 调用，并断言
        assertThrows(AccessDeniedException.class, () -> controller.refundWalletRecharge(RECHARGE_ID));
        verify(permissionApi).hasAnyPermissions(eq(USER_ID), eq(PERMISSION));
        verifyNoInteractions(walletRechargeService);
    }

    @Test
    public void testRefundWalletRecharge_anonymous() {
        // 未登录：Web 过滤链会放入匿名用户
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("key", "anonymousUser",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

        // 调用，并断言
        assertThrows(AccessDeniedException.class, () -> controller.refundWalletRecharge(RECHARGE_ID));
        verifyNoInteractions(permissionApi, walletRechargeService);
    }

    @Test
    public void testRefundWalletRecharge_noAuthentication() {
        // 没有任何认证信息，同样拒绝：取决于 Spring Security 是否读取认证信息，抛出拒绝访问或认证缺失
        Exception ex = assertThrows(Exception.class, () -> controller.refundWalletRecharge(RECHARGE_ID));
        assertTrue(ex instanceof AccessDeniedException || ex instanceof AuthenticationCredentialsNotFoundException,
                "非预期的异常：" + ex);
        verifyNoInteractions(permissionApi, walletRechargeService);
    }

    @Test
    public void testUpdateWalletRechargerPaid_callbackNotRestricted() {
        // 支付回调：仍由 @PermitAll + 内部校验，不受后台权限约束
        PayOrderNotifyReqDTO reqDTO = PayOrderNotifyReqDTO.builder().merchantOrderId("1").payOrderId(2L).build();

        // 调用
        assertTrue(controller.updateWalletRechargerPaid(reqDTO).getData());
        // 断言
        verify(walletRechargeService).updateWalletRechargerPaid(eq(1L), eq(2L));
        verifyNoInteractions(permissionApi);
    }

    private static void loginAdmin() {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(USER_ID);
        loginUser.setUserType(UserTypeEnum.ADMIN.getValue());
        loginUser.setTenantId(1L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
    }

    @Configuration
    @EnableMethodSecurity(securedEnabled = true)
    public static class Config {

        @Bean
        public PayWalletRechargeController payWalletRechargeController() {
            return new PayWalletRechargeController();
        }

        @Bean
        public PayWalletRechargeService payWalletRechargeService() {
            return mock(PayWalletRechargeService.class);
        }

        @Bean
        public PermissionCommonApi permissionCommonApi() {
            return mock(PermissionCommonApi.class);
        }

        @Bean("ss")
        public SecurityFrameworkService securityFrameworkService(PermissionCommonApi permissionApi) {
            return new SecurityFrameworkServiceImpl(permissionApi);
        }

    }

}
