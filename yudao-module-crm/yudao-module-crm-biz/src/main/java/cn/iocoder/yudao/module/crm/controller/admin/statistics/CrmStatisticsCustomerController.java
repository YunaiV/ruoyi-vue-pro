package cn.iocoder.yudao.module.crm.controller.admin.statistics;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.*;
import cn.iocoder.yudao.module.crm.service.statistics.CrmStatisticsCustomerService;
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

@Tag(name = "管理后台 - CRM 客户统计")
@RestController
@RequestMapping("/crm/statistics-customer")
@Validated
public class CrmStatisticsCustomerController {

    @Resource
    private CrmStatisticsCustomerService customerService;

    @GetMapping("/get-customer-summary-by-date")
    @Operation(summary = "获取客户总量分析(按日期)")
    @PreAuthorize("@ss.hasPermission('crm:statistics-customer:query')")
    public CommonResult<List<CrmStatisticsCustomerSummaryByDateRespVO>> getCustomerSummaryByDate(@Valid CrmStatisticsCustomerReqVO reqVO) {
        return success(customerService.getCustomerSummaryByDate(reqVO));
    }

    @GetMapping("/get-customer-summary-by-user")
    @Operation(summary = "获取客户总量分析(按用户)")
    @PreAuthorize("@ss.hasPermission('crm:statistics-customer:query')")
    public CommonResult<List<CrmStatisticsCustomerSummaryByUserRespVO>> getCustomerSummaryByUser(@Valid CrmStatisticsCustomerReqVO reqVO) {
        return success(customerService.getCustomerSummaryByUser(reqVO));
    }

    @GetMapping("/get-followup-summary-by-date")
    @Operation(summary = "获取客户跟进次数分析(按日期)")
    @PreAuthorize("@ss.hasPermission('crm:statistics-customer:query')")
    public CommonResult<List<CrmStatisticsFollowupSummaryByDateRespVO>> getFollowupSummaryByDate(@Valid CrmStatisticsCustomerReqVO reqVO) {
        return success(customerService.getFollowupSummaryByDate(reqVO));
    }

    @GetMapping("/get-followup-summary-by-user")
    @Operation(summary = "获取客户跟进次数分析(按用户)")
    @PreAuthorize("@ss.hasPermission('crm:statistics-customer:query')")
    public CommonResult<List<CrmStatisticsFollowupSummaryByUserRespVO>> getFollowupSummaryByUser(@Valid CrmStatisticsCustomerReqVO reqVO) {
        return success(customerService.getFollowupSummaryByUser(reqVO));
    }

    @GetMapping("/get-followup-summary-by-type")
    @Operation(summary = "获取客户跟进次数分析(按类型)")
    @PreAuthorize("@ss.hasPermission('crm:statistics-customer:query')")
    public CommonResult<List<CrmStatisticsFollowupSummaryByTypeRespVO>> getFollowupSummaryByType(@Valid CrmStatisticsCustomerReqVO reqVO) {
        return success(customerService.getFollowupSummaryByType(reqVO));
    }

    @GetMapping("/get-contract-summary")
    @Operation(summary = "获取合同摘要信息(客户转化率页面)")
    @PreAuthorize("@ss.hasPermission('crm:statistics-customer:query')")
    public CommonResult<List<CrmStatisticsCustomerContractSummaryRespVO>> getContractSummary(@Valid CrmStatisticsCustomerReqVO reqVO) {
        return success(customerService.getContractSummary(reqVO));
    }

    @GetMapping("/get-customer-deal-cycle-by-date")
    @Operation(summary = "获取客户成交周期(按日期)")
    @PreAuthorize("@ss.hasPermission('crm:statistics-customer:query')")
    public CommonResult<List<CrmStatisticsCustomerDealCycleByDateRespVO>> getCustomerDealCycleByDate(@Valid CrmStatisticsCustomerReqVO reqVO) {
        return success(customerService.getCustomerDealCycleByDate(reqVO));
    }

    @GetMapping("/get-customer-deal-cycle-by-user")
    @Operation(summary = "获取客户成交周期(按用户)")
    @PreAuthorize("@ss.hasPermission('crm:statistics-customer:query')")
    public CommonResult<List<CrmStatisticsCustomerDealCycleByUserRespVO>> getCustomerDealCycleByUser(@Valid CrmStatisticsCustomerReqVO reqVO) {
        return success(customerService.getCustomerDealCycleByUser(reqVO));
    }

}
