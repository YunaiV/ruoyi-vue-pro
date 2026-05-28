package cn.iocoder.yudao.module.yaya.service.member;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.yaya.controller.admin.member.vo.YayaMemberPlanSaveReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.member.vo.YayaAppEntitlementRespVO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.member.YayaMemberEntitlementDO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.member.YayaMemberPlanDO;
import cn.iocoder.yudao.module.yaya.dal.mysql.member.YayaMemberEntitlementMapper;
import cn.iocoder.yudao.module.yaya.dal.mysql.member.YayaMemberPlanMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.module.yaya.enums.YayaErrorCodeConstants.YAYA_ENTITLEMENT_REQUIRED;
import static org.junit.jupiter.api.Assertions.*;

@Import(YayaEntitlementServiceImpl.class)
class YayaEntitlementServiceImplTest extends BaseDbUnitTest {

    @Resource
    private YayaEntitlementServiceImpl entitlementService;
    @Resource
    private YayaMemberPlanMapper planMapper;
    @Resource
    private YayaMemberEntitlementMapper entitlementMapper;

    @Test
    void memberPlanCatalogShouldBeTenantIgnored() {
        assertNotNull(YayaMemberPlanDO.class.getAnnotation(TenantIgnore.class));
    }

    @Test
    void ensureDefaultPlansShouldCreateConfiguredPlans() {
        entitlementService.ensureDefaultPlans();

        List<YayaMemberPlanDO> plans = planMapper.selectList();

        assertEquals(3, plans.size());
        YayaMemberPlanDO trial = planMapper.selectByPlanKey("free-trial");
        YayaMemberPlanDO monthly = planMapper.selectByPlanKey("monthly-pro");
        YayaMemberPlanDO quarterly = planMapper.selectByPlanKey("quarterly-pro");
        assertEquals(0L, trial.getPriceCent());
        assertEquals(7, trial.getDurationDays());
        assertEquals(3900L, monthly.getPriceCent());
        assertEquals(30, monthly.getDurationDays());
        assertEquals(9900L, quarterly.getPriceCent());
        assertEquals(90, quarterly.getDurationDays());
        assertEquals(Boolean.TRUE, monthly.getBenefits().get("fullAccess"));
    }

    @Test
    void createUpdateAndDisablePlanShouldManagePlanConfiguration() {
        Long planId = entitlementService.createPlan(new YayaMemberPlanSaveReqVO()
                .setPlanKey("annual-pro")
                .setName("Annual Pro")
                .setDescription("Annual access")
                .setPriceCent(29900L)
                .setCurrency("CNY")
                .setDurationDays(365)
                .setActive(1)
                .setBenefits(Map.of("fullAccess", true)));

        entitlementService.updatePlan(planId, new YayaMemberPlanSaveReqVO()
                .setName("Annual Pro Plus")
                .setPriceCent(39900L));
        entitlementService.updatePlanStatus(planId, 0);

        YayaMemberPlanDO plan = planMapper.selectById(planId);
        assertEquals("Annual Pro Plus", plan.getName());
        assertEquals(39900L, plan.getPriceCent());
        assertEquals(0, plan.getActive());
        assertEquals(365, plan.getDurationDays());
    }

    @Test
    void grantEntitlementShouldUsePlanDurationAndBeIdempotent() {
        entitlementService.ensureDefaultPlans();
        LocalDateTime before = LocalDateTime.now();

        Long entitlementId = entitlementService.grantEntitlement(10001L, "monthly-pro",
                "manual", null, "manual:10001:monthly");
        Long duplicateId = entitlementService.grantEntitlement(10001L, "monthly-pro",
                "manual", null, "manual:10001:monthly");

        assertEquals(entitlementId, duplicateId);
        YayaMemberEntitlementDO entitlement = entitlementMapper.selectById(entitlementId);
        assertEquals(10001L, entitlement.getMemberUserId());
        assertEquals("active", entitlement.getStatus());
        assertTrue(entitlement.getEndsAt().isAfter(before.plusDays(29)));
        assertTrue(entitlement.getEndsAt().isBefore(before.plusDays(31)));
        YayaAppEntitlementRespVO response = entitlementService.getMyEntitlement(10001L);
        assertTrue(response.getActive());
        assertEquals("monthly-pro", response.getPlanKey());
        assertEquals(entitlement.getEndsAt(), response.getEndsAt());
    }

    @Test
    void requireActiveEntitlementShouldRejectMissingAccess() {
        ServiceException exception = assertThrows(ServiceException.class,
                () -> entitlementService.requireActiveEntitlement(10002L));

        assertEquals(YAYA_ENTITLEMENT_REQUIRED.getCode(), exception.getCode());
        YayaAppEntitlementRespVO response = entitlementService.getMyEntitlement(10002L);
        assertFalse(response.getActive());
        assertNull(response.getPlanKey());
        assertNull(response.getEndsAt());
    }

}
