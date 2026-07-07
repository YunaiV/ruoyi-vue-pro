package cn.iocoder.yudao.module.crm.controller.admin.statistics;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.performance.CrmStatisticsPerformanceTargetReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.performance.CrmStatisticsPerformanceTargetRespVO;
import cn.iocoder.yudao.module.crm.service.statistics.CrmStatisticsPerformanceTargetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - CRM 业绩目标统计")
@RestController
@RequestMapping("/crm/statistics-performance-target")
@Validated
public class CrmStatisticsPerformanceTargetController {

    @Resource
    private CrmStatisticsPerformanceTargetService performanceTargetService;

    @GetMapping("/get-performance-target-summary")
    @Operation(summary = "获取业绩目标完成情况")
    @PreAuthorize("@ss.hasPermission('crm:statistics-performance-target:query')")
    public CommonResult<List<CrmStatisticsPerformanceTargetRespVO>> getPerformanceTargetSummary(@Valid CrmStatisticsPerformanceTargetReqVO reqVO) {
        return success(performanceTargetService.getPerformanceTargetSummary(reqVO));
    }

}
