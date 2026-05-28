package cn.iocoder.yudao.module.yaya.service.member;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.yaya.controller.admin.member.vo.YayaMemberPlanRespVO;
import cn.iocoder.yudao.module.yaya.controller.admin.member.vo.YayaMemberPlanSaveReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.member.vo.YayaAppEntitlementRespVO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.member.YayaMemberEntitlementDO;
import cn.iocoder.yudao.module.yaya.dal.dataobject.member.YayaMemberPlanDO;
import cn.iocoder.yudao.module.yaya.dal.mysql.member.YayaMemberEntitlementMapper;
import cn.iocoder.yudao.module.yaya.dal.mysql.member.YayaMemberPlanMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.yaya.enums.YayaErrorCodeConstants.*;

@Service
@Validated
public class YayaEntitlementServiceImpl implements YayaEntitlementService {

    private static final String STATUS_ACTIVE = "active";
    private static final String DEFAULT_CURRENCY = "CNY";

    private static final List<DefaultPlan> DEFAULT_PLANS = List.of(
            new DefaultPlan("free-trial", "Free Trial", "7-day limited trial", 0L, 7,
                    Map.of("evaluationLimit", 3, "fullAccess", false)),
            new DefaultPlan("monthly-pro", "Monthly Pro", "30-day full access", 3900L, 30,
                    Map.of("fullAccess", true)),
            new DefaultPlan("quarterly-pro", "Quarterly Pro", "90-day full access", 9900L, 90,
                    Map.of("fullAccess", true)));

    @Resource
    private YayaMemberPlanMapper planMapper;
    @Resource
    private YayaMemberEntitlementMapper entitlementMapper;

    @Override
    public void ensureDefaultPlans() {
        for (DefaultPlan defaultPlan : DEFAULT_PLANS) {
            if (planMapper.selectByPlanKey(defaultPlan.planKey()) != null) {
                continue;
            }
            YayaMemberPlanDO plan = new YayaMemberPlanDO();
            plan.setPlanKey(defaultPlan.planKey());
            plan.setName(defaultPlan.name());
            plan.setDescription(defaultPlan.description());
            plan.setPriceCent(defaultPlan.priceCent());
            plan.setCurrency(DEFAULT_CURRENCY);
            plan.setDurationDays(defaultPlan.durationDays());
            plan.setActive(1);
            plan.setBenefits(defaultPlan.benefits());
            planMapper.insert(plan);
        }
    }

    @Override
    public List<YayaMemberPlanRespVO> getPlanList() {
        ensureDefaultPlans();
        return planMapper.selectListOrderByPrice().stream().map(this::toPlanResp).toList();
    }

    @Override
    public Long createPlan(YayaMemberPlanSaveReqVO reqVO) {
        validatePlanKeyUnique(null, reqVO.getPlanKey());
        YayaMemberPlanDO plan = new YayaMemberPlanDO();
        copyPlanFields(reqVO, plan, true);
        planMapper.insert(plan);
        return plan.getId();
    }

    @Override
    public void updatePlan(Long id, YayaMemberPlanSaveReqVO reqVO) {
        validatePlanExists(id);
        if (StrUtil.isNotBlank(reqVO.getPlanKey())) {
            validatePlanKeyUnique(id, reqVO.getPlanKey());
        }
        YayaMemberPlanDO update = new YayaMemberPlanDO();
        update.setId(id);
        copyPlanFields(reqVO, update, false);
        planMapper.updateById(update);
    }

    @Override
    public void updatePlanStatus(Long id, Integer active) {
        validatePlanExists(id);
        YayaMemberPlanDO update = new YayaMemberPlanDO();
        update.setId(id);
        update.setActive(active);
        planMapper.updateById(update);
    }

    @Override
    public YayaAppEntitlementRespVO getMyEntitlement(Long memberUserId) {
        requireMember(memberUserId);
        YayaMemberEntitlementDO entitlement = entitlementMapper.selectActiveByMemberUserId(memberUserId,
                LocalDateTime.now());
        if (entitlement == null) {
            return new YayaAppEntitlementRespVO().setActive(false);
        }
        YayaMemberPlanDO plan = planMapper.selectById(entitlement.getPlanId());
        return new YayaAppEntitlementRespVO()
                .setActive(true)
                .setPlanKey(plan == null ? null : plan.getPlanKey())
                .setEndsAt(entitlement.getEndsAt());
    }

    @Override
    public void requireActiveEntitlement(Long memberUserId) {
        requireMember(memberUserId);
        YayaMemberEntitlementDO entitlement = entitlementMapper.selectActiveByMemberUserId(memberUserId,
                LocalDateTime.now());
        if (entitlement == null) {
            throw exception(YAYA_ENTITLEMENT_REQUIRED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long grantEntitlement(Long memberUserId, String planKey, String sourceType, Long sourceId,
                                 String idempotencyKey) {
        requireMember(memberUserId);
        YayaMemberEntitlementDO existing = entitlementMapper.selectByIdempotencyKey(idempotencyKey);
        if (existing != null) {
            return existing.getId();
        }
        YayaMemberPlanDO plan = planMapper.selectActiveByPlanKey(planKey);
        if (plan == null) {
            throw exception(YAYA_MEMBER_PLAN_NOT_EXISTS);
        }

        LocalDateTime now = LocalDateTime.now();
        YayaMemberEntitlementDO active = entitlementMapper.selectActiveByMemberUserId(memberUserId, now);
        LocalDateTime startsAt = active != null && active.getEndsAt().isAfter(now) ? active.getEndsAt() : now;

        YayaMemberEntitlementDO entitlement = new YayaMemberEntitlementDO();
        entitlement.setMemberUserId(memberUserId);
        entitlement.setPlanId(plan.getId());
        entitlement.setSourceType(StrUtil.blankToDefault(sourceType, "manual"));
        entitlement.setSourceId(sourceId);
        entitlement.setStatus(STATUS_ACTIVE);
        entitlement.setStartsAt(startsAt);
        entitlement.setEndsAt(startsAt.plusDays(plan.getDurationDays()));
        entitlement.setIdempotencyKey(idempotencyKey);
        entitlement.setMetadata(Map.of("planKey", plan.getPlanKey()));
        entitlementMapper.insert(entitlement);
        return entitlement.getId();
    }

    private void copyPlanFields(YayaMemberPlanSaveReqVO reqVO, YayaMemberPlanDO plan, boolean create) {
        if (create || reqVO.getPlanKey() != null) {
            plan.setPlanKey(reqVO.getPlanKey());
        }
        if (create || reqVO.getName() != null) {
            plan.setName(StrUtil.blankToDefault(reqVO.getName(), reqVO.getPlanKey()));
        }
        if (create || reqVO.getDescription() != null) {
            plan.setDescription(StrUtil.blankToDefault(reqVO.getDescription(), ""));
        }
        if (create || reqVO.getPriceCent() != null) {
            plan.setPriceCent(reqVO.getPriceCent() == null ? 0L : reqVO.getPriceCent());
        }
        if (create || reqVO.getCurrency() != null) {
            plan.setCurrency(StrUtil.blankToDefault(reqVO.getCurrency(), DEFAULT_CURRENCY));
        }
        if (create || reqVO.getDurationDays() != null) {
            plan.setDurationDays(reqVO.getDurationDays() == null ? 30 : reqVO.getDurationDays());
        }
        if (create || reqVO.getActive() != null) {
            plan.setActive(reqVO.getActive() == null ? 1 : reqVO.getActive());
        }
        if (create || reqVO.getBenefits() != null) {
            plan.setBenefits(reqVO.getBenefits() == null ? Map.of() : reqVO.getBenefits());
        }
    }

    private void validatePlanExists(Long id) {
        if (planMapper.selectById(id) == null) {
            throw exception(YAYA_MEMBER_PLAN_NOT_EXISTS);
        }
    }

    private void validatePlanKeyUnique(Long id, String planKey) {
        YayaMemberPlanDO plan = planMapper.selectByPlanKey(planKey);
        if (plan != null && (id == null || !id.equals(plan.getId()))) {
            throw exception(YAYA_MEMBER_PLAN_KEY_DUPLICATE);
        }
    }

    private YayaMemberPlanRespVO toPlanResp(YayaMemberPlanDO plan) {
        return new YayaMemberPlanRespVO()
                .setId(plan.getId())
                .setPlanKey(plan.getPlanKey())
                .setName(plan.getName())
                .setDescription(plan.getDescription())
                .setPriceCent(plan.getPriceCent())
                .setCurrency(plan.getCurrency())
                .setDurationDays(plan.getDurationDays())
                .setActive(plan.getActive())
                .setBenefits(plan.getBenefits());
    }

    private static void requireMember(Long memberUserId) {
        if (memberUserId == null) {
            throw exception(YAYA_MEMBER_NOT_LOGIN);
        }
    }

    private record DefaultPlan(String planKey, String name, String description, Long priceCent, Integer durationDays,
                               Map<String, Object> benefits) {
    }

}
