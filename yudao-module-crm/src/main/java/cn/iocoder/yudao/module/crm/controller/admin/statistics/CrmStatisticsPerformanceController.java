package cn.iocoder.yudao.module.crm.controller.admin.statistics;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.performance.CrmStatisticsPerformanceReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.performance.CrmStatisticsPerformanceRespVO;
import cn.iocoder.yudao.module.crm.service.statistics.CrmStatisticsPerformanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;


@Tag(name = "管理后台 - CRM 员工业绩统计")
@RestController
@RequestMapping("/crm/statistics-performance")
@Validated
public class CrmStatisticsPerformanceController {

    @Resource
    private CrmStatisticsPerformanceService performanceService;

    @GetMapping("/get-contract-count-performance")
    @Operation(summary = "合同数量统计", description = "用于【合同数量分析】页面")
    @PreAuthorize("@ss.hasPermission('crm:statistics-performance:query')")
    public CommonResult<List<CrmStatisticsPerformanceRespVO>> getContractCountPerformance(@Valid CrmStatisticsPerformanceReqVO performanceReqVO) {
        return success(performanceService.getContractCountPerformance(performanceReqVO));
    }

    @GetMapping("/get-contract-price-performance")
    @Operation(summary = "合同金额统计")
    @PreAuthorize("@ss.hasPermission('crm:statistics-performance:query')")
    public CommonResult<List<CrmStatisticsPerformanceRespVO>> getContractPriceStaffPerformance(@Valid CrmStatisticsPerformanceReqVO performanceReqVO) {
        return success(performanceService.getContractPricePerformance(performanceReqVO));
    }

    @GetMapping("/get-receivable-price-performance")
    @Operation(summary = "回款金额统计")
    @PreAuthorize("@ss.hasPermission('crm:statistics-performance:query')")
    public CommonResult<List<CrmStatisticsPerformanceRespVO>> getReceivablePriceStaffPerformance(@Valid CrmStatisticsPerformanceReqVO performanceReqVO) {
        return success(performanceService.getReceivablePricePerformance(performanceReqVO));
    }

}
