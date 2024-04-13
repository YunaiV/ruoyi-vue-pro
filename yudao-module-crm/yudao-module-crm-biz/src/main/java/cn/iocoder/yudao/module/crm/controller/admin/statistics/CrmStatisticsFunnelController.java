package cn.iocoder.yudao.module.crm.controller.admin.statistics;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.funnel.CrmStatisticBusinessEndStatusRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.funnel.CrmStatisticFunnelRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.funnel.CrmStatisticsFunnelReqVO;
import cn.iocoder.yudao.module.crm.service.statistics.CrmStatisticsFunnelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - CRM 销售漏斗")
@RestController
@RequestMapping("/crm/statistics-funnel")
@Validated
public class CrmStatisticsFunnelController {

    @Resource
    private CrmStatisticsFunnelService crmStatisticsFunnelService;

    @GetMapping("/get-funnel-summary")
    @Operation(summary = "获取销售漏斗统计数据", description = "用于【销售漏斗】页面")
    @PreAuthorize("@ss.hasPermission('crm:statistics-funnel:query')")
    public CommonResult<CrmStatisticFunnelRespVO> getFunnelSummary(@Valid CrmStatisticsFunnelReqVO reqVO) {
        return success(crmStatisticsFunnelService.getFunnelSummary(reqVO));
    }


    @GetMapping("/get-business-end-status-summary")
    @Operation(summary = "获取商机结束状态统计", description = "用于【销售漏斗】页面")
    @PreAuthorize("@ss.hasPermission('crm:statistics-funnel:query')")
    public CommonResult<List<CrmStatisticBusinessEndStatusRespVO>> getBusinessEndStatusSummary(@Valid CrmStatisticsFunnelReqVO reqVO) {
        return success(crmStatisticsFunnelService.getBusinessEndStatusSummary(reqVO));
    }

}
