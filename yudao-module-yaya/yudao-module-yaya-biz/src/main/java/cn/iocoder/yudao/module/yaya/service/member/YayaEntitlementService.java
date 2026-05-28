package cn.iocoder.yudao.module.yaya.service.member;

import cn.iocoder.yudao.module.yaya.controller.admin.member.vo.YayaMemberPlanRespVO;
import cn.iocoder.yudao.module.yaya.controller.admin.member.vo.YayaMemberPlanSaveReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.member.vo.YayaAppEntitlementRespVO;

import java.util.List;

public interface YayaEntitlementService {

    void ensureDefaultPlans();

    List<YayaMemberPlanRespVO> getPlanList();

    Long createPlan(YayaMemberPlanSaveReqVO reqVO);

    void updatePlan(Long id, YayaMemberPlanSaveReqVO reqVO);

    void updatePlanStatus(Long id, Integer active);

    YayaAppEntitlementRespVO getMyEntitlement(Long memberUserId);

    void requireActiveEntitlement(Long memberUserId);

    Long grantEntitlement(Long memberUserId, String planKey, String sourceType, Long sourceId, String idempotencyKey);

}
