package cn.iocoder.yudao.module.fms.controller.admin.report;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.balance.FmsBalanceSheetCheckRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.balance.FmsBalanceSheetRowRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportFormulaUpdateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.report.vo.FmsReportListReqVO;
import cn.iocoder.yudao.module.fms.service.report.FmsBalanceSheetService;
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

@Tag(name = "管理后台 - FMS 资产负债表")
@RestController
@RequestMapping("/fms/report/balance-sheet")
@Validated
public class FmsBalanceSheetController {

    @Resource
    private FmsBalanceSheetService balanceSheetService;

    @GetMapping("/get")
    @Operation(summary = "获得资产负债表")
    @PreAuthorize("@ss.hasPermission('fms:report:balance-sheet:query')")
    public CommonResult<List<FmsBalanceSheetRowRespVO>> getBalanceSheet(@Valid FmsReportListReqVO listReqVO) {
        return success(balanceSheetService.getBalanceSheet(listReqVO, getLoginUserId()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出资产负债表")
    @PreAuthorize("@ss.hasPermission('fms:report:balance-sheet:export')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void exportBalanceSheet(@Valid FmsReportListReqVO listReqVO,
                                   HttpServletResponse response) throws IOException {
        List<FmsBalanceSheetRowRespVO> list = balanceSheetService.getBalanceSheet(listReqVO, getLoginUserId());
        ExcelUtils.write(response, "资产负债表.xls", "资产负债表", FmsBalanceSheetRowRespVO.class, list);
    }

    @PutMapping("/update")
    @Operation(summary = "更新资产负债表公式")
    @PreAuthorize("@ss.hasPermission('fms:report:balance-sheet:update')")
    public CommonResult<Boolean> updateBalanceSheetFormula(@Valid @RequestBody FmsReportFormulaUpdateReqVO updateReqVO) {
        balanceSheetService.updateBalanceSheetFormula(updateReqVO, getLoginUserId());
        return success(true);
    }

    @GetMapping("/check")
    @Operation(summary = "检查资产负债表")
    @PreAuthorize("@ss.hasPermission('fms:report:balance-sheet:query')")
    public CommonResult<FmsBalanceSheetCheckRespVO> checkBalanceSheet(@Valid FmsReportListReqVO listReqVO) {
        return success(balanceSheetService.checkBalanceSheet(listReqVO, getLoginUserId()));
    }

}
