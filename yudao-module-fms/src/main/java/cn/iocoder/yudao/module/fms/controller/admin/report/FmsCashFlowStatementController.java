package cn.iocoder.yudao.module.fms.controller.admin.report;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.cashflow.FmsCashFlowAdjustmentRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.cashflow.FmsCashFlowAdjustmentUpdateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.cashflow.FmsCashFlowCheckRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.cashflow.FmsCashFlowStatementUpdateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportFormulaUpdateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportItemRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportListReqVO;
import cn.iocoder.yudao.module.fms.service.report.FmsCashFlowStatementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - FMS 现金流量表")
@RestController
@RequestMapping("/fms/report/cash-flow-statement")
@Validated
public class FmsCashFlowStatementController {

    @Resource
    private FmsCashFlowStatementService cashFlowStatementService;

    @GetMapping("/get")
    @Operation(summary = "获得现金流量表")
    @PreAuthorize("@ss.hasPermission('fms:report:cash-flow-statement:query')")
    public CommonResult<List<FmsReportItemRespVO>> getCashFlowStatement(
            @Valid FmsReportListReqVO listReqVO) {
        return success(cashFlowStatementService.getCashFlowStatement(listReqVO, getLoginUserId()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出现金流量表")
    @PreAuthorize("@ss.hasPermission('fms:report:cash-flow-statement:export')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void exportCashFlowStatement(@Valid FmsReportListReqVO listReqVO,
                                        HttpServletResponse response) throws IOException {
        List<FmsReportItemRespVO> list = cashFlowStatementService.getCashFlowStatement(
                listReqVO, getLoginUserId());
        ExcelUtils.write(response, "现金流量表.xls", "现金流量表", FmsReportItemRespVO.class, list);
    }

    @PutMapping("/update")
    @Operation(summary = "更新现金流量表")
    @PreAuthorize("@ss.hasPermission('fms:report:cash-flow-statement:update')")
    public CommonResult<Boolean> updateCashFlowStatement(
            @Valid @RequestBody FmsCashFlowStatementUpdateReqVO updateReqVO) {
        cashFlowStatementService.updateCashFlowStatement(updateReqVO, getLoginUserId());
        return success(true);
    }

    @GetMapping("/check")
    @Operation(summary = "检查现金流量表")
    @PreAuthorize("@ss.hasPermission('fms:report:cash-flow-statement:query')")
    public CommonResult<FmsCashFlowCheckRespVO> checkCashFlowStatement(
            @Valid FmsReportListReqVO listReqVO) {
        return success(cashFlowStatementService.checkCashFlowStatement(listReqVO, getLoginUserId()));
    }

    @GetMapping("/adjustment/list")
    @Operation(summary = "获得现金流量辅助数据列表")
    @PreAuthorize("@ss.hasPermission('fms:report:cash-flow-statement:query')")
    public CommonResult<List<FmsCashFlowAdjustmentRespVO>> getCashFlowAdjustmentList(@Valid FmsReportListReqVO listReqVO) {
        return success(cashFlowStatementService.getCashFlowAdjustmentList(listReqVO, getLoginUserId()));
    }

    @PutMapping("/adjustment/update")
    @Operation(summary = "更新现金流量辅助数据")
    @PreAuthorize("@ss.hasPermission('fms:report:cash-flow-statement:update')")
    public CommonResult<Boolean> updateCashFlowAdjustment(@Valid @RequestBody FmsCashFlowAdjustmentUpdateReqVO updateReqVO) {
        cashFlowStatementService.updateCashFlowAdjustment(updateReqVO, getLoginUserId());
        return success(true);
    }

    @PutMapping("/adjustment/update-formula")
    @Operation(summary = "更新现金流量辅助数据公式")
    @PreAuthorize("@ss.hasPermission('fms:report:cash-flow-statement:update')")
    public CommonResult<Boolean> updateCashFlowAdjustmentFormula(@Valid @RequestBody FmsReportFormulaUpdateReqVO updateReqVO) {
        cashFlowStatementService.updateCashFlowAdjustmentFormula(updateReqVO, getLoginUserId());
        return success(true);
    }

}
