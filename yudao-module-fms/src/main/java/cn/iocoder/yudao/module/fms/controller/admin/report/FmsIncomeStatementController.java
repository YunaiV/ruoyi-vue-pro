package cn.iocoder.yudao.module.fms.controller.admin.report;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.income.FmsIncomeStatementCheckRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportFormulaUpdateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportItemRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportListReqVO;
import cn.iocoder.yudao.module.fms.service.report.FmsIncomeStatementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
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

@Tag(name = "管理后台 - FMS 利润表")
@RestController
@RequestMapping("/fms/report/income-statement")
@Validated
public class FmsIncomeStatementController {

    @Resource
    private FmsIncomeStatementService incomeStatementService;

    @GetMapping("/get")
    @Operation(summary = "获得利润表")
    @PreAuthorize("@ss.hasPermission('fms:report:income-statement:query')")
    public CommonResult<List<FmsReportItemRespVO>> getIncomeStatement(@Valid FmsReportListReqVO listReqVO) {
        return success(incomeStatementService.getIncomeStatement(listReqVO, getLoginUserId()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出利润表")
    @PreAuthorize("@ss.hasPermission('fms:report:income-statement:export')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void exportIncomeStatement(@Valid FmsReportListReqVO listReqVO, HttpServletResponse response) throws IOException {
        List<FmsReportItemRespVO> list = incomeStatementService.getIncomeStatement(listReqVO, getLoginUserId());
        ExcelUtils.write(response, "利润表.xls", "利润表", FmsReportItemRespVO.class, list);
    }

    @PutMapping("/update")
    @Operation(summary = "更新利润表公式")
    @PreAuthorize("@ss.hasPermission('fms:report:income-statement:update')")
    public CommonResult<Boolean> updateIncomeStatementFormula(@Valid @RequestBody FmsReportFormulaUpdateReqVO updateReqVO) {
        incomeStatementService.updateIncomeStatementFormula(updateReqVO, getLoginUserId());
        return success(true);
    }

    @GetMapping("/check")
    @Operation(summary = "检查利润表")
    @PreAuthorize("@ss.hasPermission('fms:report:income-statement:query')")
    public CommonResult<FmsIncomeStatementCheckRespVO> checkIncomeStatement(@Valid FmsReportListReqVO listReqVO) {
        return success(incomeStatementService.checkIncomeStatement(listReqVO, getLoginUserId()));
    }

}
