package cn.iocoder.yudao.module.crm.controller.admin.statistics;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.business.CrmBusinessController;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.funnel.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
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
    private CrmStatisticsFunnelService funnelService;

    @GetMapping("/get-funnel-summary")
    @Operation(summary = "获取销售漏斗统计数据", description = "用于【销售漏斗】页面的【销售漏斗分析】")
    @PreAuthorize("@ss.hasPermission('crm:statistics-funnel:query')")
    public CommonResult<CrmStatisticFunnelSummaryRespVO> getFunnelSummary(@Valid CrmStatisticsFunnelReqVO reqVO) {
        return success(funnelService.getFunnelSummary(reqVO));
    }

    @GetMapping("/get-business-summary-by-end-status")
    @Operation(summary = "获取商机结束状态统计", description = "用于【销售漏斗】页面的【销售漏斗分析】")
    @PreAuthorize("@ss.hasPermission('crm:statistics-funnel:query')")
    public CommonResult<List<CrmStatisticsBusinessSummaryByEndStatusRespVO>> getBusinessSummaryByEndStatus(@Valid CrmStatisticsFunnelReqVO reqVO) {
        return success(funnelService.getBusinessSummaryByEndStatus(reqVO));
    }

    @GetMapping("/get-business-summary-by-date")
    @Operation(summary = "获取新增商机分析(按日期)", description = "用于【销售漏斗】页面")
    @PreAuthorize("@ss.hasPermission('crm:statistics-funnel:query')")
    public CommonResult<List<CrmStatisticsBusinessSummaryByDateRespVO>> getBusinessSummaryByDate(@Valid CrmStatisticsFunnelReqVO reqVO) {
        return success(funnelService.getBusinessSummaryByDate(reqVO));
    }

    @GetMapping("/get-business-inversion-rate-summary-by-date")
    @Operation(summary = "获取商机转化率分析(按日期)", description = "用于【销售漏斗】页面")
    @PreAuthorize("@ss.hasPermission('crm:statistics-funnel:query')")
    public CommonResult<List<CrmStatisticsBusinessInversionRateSummaryByDateRespVO>> getBusinessInversionRateSummaryByDate(@Valid CrmStatisticsFunnelReqVO reqVO) {
        return success(funnelService.getBusinessInversionRateSummaryByDate(reqVO));
    }

    @GetMapping("/get-business-page-by-date")
    @Operation(summary = "获得商机分页(按日期)", description = "用于【销售漏斗】页面的【新增商机分析】")
    @PreAuthorize("@ss.hasPermission('crm:business:query')")
    public CommonResult<PageResult<CrmBusinessRespVO>> getBusinessPageByDate(@Valid CrmStatisticsFunnelReqVO pageVO) {
        PageResult<CrmBusinessDO> pageResult = funnelService.getBusinessPageByDate(pageVO);
        return success(new PageResult<>(buildBusinessDetailList(pageResult.getList()), pageResult.getTotal()));
    }

    private List<CrmBusinessRespVO> buildBusinessDetailList(List<CrmBusinessDO> list) {
        return SpringUtil.getBean(CrmBusinessController.class).buildBusinessDetailList(list);
    }

}
