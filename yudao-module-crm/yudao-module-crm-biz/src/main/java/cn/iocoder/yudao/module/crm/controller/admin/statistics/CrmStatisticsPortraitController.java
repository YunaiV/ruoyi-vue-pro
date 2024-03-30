package cn.iocoder.yudao.module.crm.controller.admin.statistics;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.CrmStatisticsCustomerReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.portrait.CrmStatisticCustomerAreaRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.portrait.CrmStatisticCustomerIndustryRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.portrait.CrmStatisticCustomerLevelRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.portrait.CrmStatisticCustomerSourceRespVO;
import cn.iocoder.yudao.module.crm.service.statistics.CrmStatisticsPortraitService;
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

@Tag(name = "管理后台 - CRM 客户画像")
@RestController
@RequestMapping("/crm/statistics-portrait")
@Validated
public class CrmStatisticsPortraitController {

    @Resource
    private CrmStatisticsPortraitService statisticsPortraitService;

    // TODO @puhui999：搞个属于自己的 CrmStatisticsCustomerReqVO 类哈

    @GetMapping("/get-customer-area-summary")
    @Operation(summary = "获取客户地区统计数据", description = "用于【城市分布分析】页面")
    @PreAuthorize("@ss.hasPermission('crm:statistics-portrait:query')")
    public CommonResult<List<CrmStatisticCustomerAreaRespVO>> getCustomerAreaSummary(@Valid CrmStatisticsCustomerReqVO reqVO) {
        return success(statisticsPortraitService.getCustomerAreaSummary(reqVO));
    }

    @GetMapping("/get-customer-industry-summary")
    @Operation(summary = "获取客户行业统计数据", description = "用于【客户行业分析】页面")
    @PreAuthorize("@ss.hasPermission('crm:statistics-portrait:query')")
    public CommonResult<List<CrmStatisticCustomerIndustryRespVO>> getCustomerIndustrySummary(@Valid CrmStatisticsCustomerReqVO reqVO) {
        return success(statisticsPortraitService.getCustomerIndustrySummary(reqVO));
    }

    @GetMapping("/get-customer-level-summary")
    @Operation(summary = "获取客户级别统计数据", description = "用于【客户级别分析】页面")
    @PreAuthorize("@ss.hasPermission('crm:statistics-portrait:query')")
    public CommonResult<List<CrmStatisticCustomerLevelRespVO>> getCustomerLevelSummary(@Valid CrmStatisticsCustomerReqVO reqVO) {
        return success(statisticsPortraitService.getCustomerLevelSummary(reqVO));
    }

    @GetMapping("/get-customer-source-summary")
    @Operation(summary = "获取客户来源统计数据", description = "用于【客户来源分析】页面")
    @PreAuthorize("@ss.hasPermission('crm:statistics-portrait:query')")
    public CommonResult<List<CrmStatisticCustomerSourceRespVO>> getCustomerSourceSummary(@Valid CrmStatisticsCustomerReqVO reqVO) {
        return success(statisticsPortraitService.getCustomerSourceSummary(reqVO));
    }

}
