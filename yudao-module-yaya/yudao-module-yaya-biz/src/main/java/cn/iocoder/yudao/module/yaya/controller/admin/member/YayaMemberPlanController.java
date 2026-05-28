package cn.iocoder.yudao.module.yaya.controller.admin.member;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.yaya.controller.admin.member.vo.YayaMemberPlanRespVO;
import cn.iocoder.yudao.module.yaya.controller.admin.member.vo.YayaMemberPlanSaveReqVO;
import cn.iocoder.yudao.module.yaya.controller.admin.member.vo.YayaMemberPlanStatusReqVO;
import cn.iocoder.yudao.module.yaya.service.member.YayaEntitlementService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/yaya/member-plans")
@Validated
public class YayaMemberPlanController {

    @Resource
    private YayaEntitlementService entitlementService;

    @GetMapping("")
    @PreAuthorize("@ss.hasPermission('yaya:member-plan:query')")
    public CommonResult<List<YayaMemberPlanRespVO>> getPlanList() {
        return success(entitlementService.getPlanList());
    }

    @PostMapping("")
    @PreAuthorize("@ss.hasPermission('yaya:member-plan:create')")
    public CommonResult<Long> createPlan(@Valid @RequestBody YayaMemberPlanSaveReqVO reqVO) {
        return success(entitlementService.createPlan(reqVO));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('yaya:member-plan:update')")
    public CommonResult<Boolean> updatePlan(@PathVariable("id") Long id,
                                            @RequestBody YayaMemberPlanSaveReqVO reqVO) {
        entitlementService.updatePlan(id, reqVO);
        return success(true);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("@ss.hasPermission('yaya:member-plan:update')")
    public CommonResult<Boolean> updatePlanStatus(@PathVariable("id") Long id,
                                                  @Valid @RequestBody YayaMemberPlanStatusReqVO reqVO) {
        entitlementService.updatePlanStatus(id, reqVO.getActive());
        return success(true);
    }

}
