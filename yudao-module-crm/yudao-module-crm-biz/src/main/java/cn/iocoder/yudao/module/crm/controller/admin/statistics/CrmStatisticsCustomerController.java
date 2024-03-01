package cn.iocoder.yudao.module.crm.controller.admin.statistics;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.CrmStatisticsCustomerCountVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.CrmStatisticsCustomerReqVO;
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

@Tag(name = "管理后台 - CRM 数据统计 员工客户分析")
@RestController
@RequestMapping("/crm/statistics-customer")
@Validated
public class CrmStatisticsCustomerController {

    @Resource
    private CrmStatisticsCustomerService customerService;

    @GetMapping("/get-total-customer-count")
    @Operation(summary = "获得新建客户数量")
    @PreAuthorize("@ss.hasPermission('crm:statistics-customer:query')")
    public CommonResult<List<CrmStatisticsCustomerCountVO>> getTotalCustomerCount(@Valid CrmStatisticsCustomerReqVO reqVO) {
        return success(customerService.getTotalCustomerCount(reqVO));
    }

    @GetMapping("/get-deal-total-customer-count")
    @Operation(summary = "获得成交客户数量")
    @PreAuthorize("@ss.hasPermission('crm:statistics-customer:query')")
    public CommonResult<List<CrmStatisticsCustomerCountVO>> getDealTotalCustomerCount(@Valid CrmStatisticsCustomerReqVO reqVO) {
        return success(customerService.getDealTotalCustomerCount(reqVO));
    }

    @GetMapping("/get-record-count")
    @Operation(summary = "获取客户跟进次数")
    @PreAuthorize("@ss.hasPermission('crm:statistics-customer:query')")
    public CommonResult<List<CrmStatisticsCustomerCountVO>> getRecordCount(@Valid CrmStatisticsCustomerReqVO reqVO) {
        return success(customerService.getRecordCount(reqVO));
    }

    @GetMapping("/get-distinct-record-count")
    @Operation(summary = "获取已跟进客户数")
    @PreAuthorize("@ss.hasPermission('crm:statistics-customer:query')")
    public CommonResult<List<CrmStatisticsCustomerCountVO>> getDistinctRecordCount(@Valid CrmStatisticsCustomerReqVO reqVO) {
        return success(customerService.getDistinctRecordCount(reqVO));
    }

    @GetMapping("/get-record-type-count")
    @Operation(summary = "获取客户跟进方式统计数")
    @PreAuthorize("@ss.hasPermission('crm:statistics-customer:query')")
    public CommonResult<List<CrmStatisticsCustomerCountVO>> getRecordTypeCount(@Valid CrmStatisticsCustomerReqVO reqVO) {
        return success(customerService.getRecordTypeCount(reqVO));
    }

    @GetMapping("/get-customer-cycle")
    @Operation(summary = "获取客户成交周期")
    @PreAuthorize("@ss.hasPermission('crm:statistics-customer:query')")
    public CommonResult<List<CrmStatisticsCustomerCountVO>> getCustomerCycle(@Valid CrmStatisticsCustomerReqVO reqVO) {
        return success(customerService.getCustomerCycle(reqVO));
    }

}
