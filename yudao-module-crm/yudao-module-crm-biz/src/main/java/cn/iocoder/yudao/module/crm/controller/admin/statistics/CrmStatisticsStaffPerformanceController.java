package cn.iocoder.yudao.module.crm.controller.admin.statistics;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.CrmStatisticsStaffPerformanceReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.CrmStatisticsStaffPerformanceRespVO;
import cn.iocoder.yudao.module.crm.service.statistics.CrmStatisticsStaffPerformanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;


@Tag(name = "管理后台 - CRM 员工业绩统计")
@RestController
@RequestMapping("/crm/statistics-performance")
@Validated
public class CrmStatisticsStaffPerformanceController {

    @Resource
    private CrmStatisticsStaffPerformanceService performanceService;

    @GetMapping("/get-contract-count-performance")
    @Operation(summary = "员工业绩-签约合同数量")
    @PreAuthorize("@ss.hasPermission('crm:statistics-performance:query')")
    public CommonResult<List<CrmStatisticsStaffPerformanceRespVO>> getContractCountStaffPerformance(@Valid CrmStatisticsStaffPerformanceReqVO staffPerformanceReqVO) {
        return success(performanceService.getContractCountStaffPerformance(staffPerformanceReqVO));
    }

    @GetMapping("/get-contract-price-performance")
    @Operation(summary = "员工业绩-获得合同金额")
    @PreAuthorize("@ss.hasPermission('crm:statistics-performance:query')")
    public CommonResult<List<CrmStatisticsStaffPerformanceRespVO>> getContractPriceStaffPerformance(@Valid CrmStatisticsStaffPerformanceReqVO staffPerformanceReqVO) {
        return success(performanceService.getContractPriceStaffPerformance(staffPerformanceReqVO));
    }

    @GetMapping("/get-receivable-price-performance")
    @Operation(summary = "员工业绩-获得回款金额")
    @PreAuthorize("@ss.hasPermission('crm:statistics-performance:query')")
    public CommonResult<List<CrmStatisticsStaffPerformanceRespVO>> getReceivablePriceStaffPerformance(@Valid CrmStatisticsStaffPerformanceReqVO staffPerformanceReqVO) {
        return success(performanceService.getReceivablePriceStaffPerformance(staffPerformanceReqVO));
    }

}
