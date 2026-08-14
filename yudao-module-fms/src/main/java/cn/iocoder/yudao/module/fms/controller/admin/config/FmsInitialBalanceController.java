package cn.iocoder.yudao.module.fms.controller.admin.config;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.initialbalance.FmsInitialBalanceRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.initialbalance.FmsInitialBalanceSaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.initialbalance.FmsTrialBalanceRespVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.initialbalance.FmsInitialBalanceExcelVO;
import cn.iocoder.yudao.module.fms.service.config.FmsInitialBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.INITIAL_BALANCE_IMPORT_TEMPLATE_INVALID;

@Tag(name = "管理后台 - FMS 初始余额")
@RestController
@RequestMapping("/fms/config/initial-balance")
@Validated
public class FmsInitialBalanceController {

    @Resource
    private FmsInitialBalanceService initialBalanceService;
    @Resource
    private FmsAccountSetService accountSetService;

    @GetMapping("/list")
    @Operation(summary = "获得初始余额列表")
    @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024")
    @Parameter(name = "subjectType", description = "科目类型", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('fms:config:initial-balance:query')")
    public CommonResult<List<FmsInitialBalanceRespVO>> getInitialBalanceList(
            @RequestParam("accountSetId") @NotNull Long accountSetId,
            @RequestParam("subjectType") @NotNull Integer subjectType) {
        return success(initialBalanceService.getInitialBalanceList(accountSetId, subjectType, getLoginUserId()));
    }

    @PutMapping("/save")
    @Operation(summary = "保存初始余额")
    @PreAuthorize("@ss.hasPermission('fms:config:initial-balance:update')")
    public CommonResult<Boolean> saveInitialBalance(@Valid @RequestBody FmsInitialBalanceSaveReqVO saveReqVO) {
        initialBalanceService.saveInitialBalance(saveReqVO, getLoginUserId());
        return success(true);
    }

    @GetMapping("/trial-balance")
    @Operation(summary = "获得试算平衡结果")
    @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('fms:config:initial-balance:query')")
    public CommonResult<FmsTrialBalanceRespVO> getTrialBalance(
            @RequestParam("accountSetId") @NotNull Long accountSetId) {
        return success(initialBalanceService.getTrialBalance(accountSetId, getLoginUserId()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出初始余额")
    @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('fms:config:initial-balance:export')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void exportInitialBalance(
            @RequestParam("accountSetId") @NotNull Long accountSetId,
            HttpServletResponse response) throws IOException {
        // 1. 读取初始余额列表，并展开为 Excel 行
        List<FmsInitialBalanceExcelVO> rows = FmsInitialBalanceExcelHelper.toRows(
                initialBalanceService.getInitialBalanceList(accountSetId, null, getLoginUserId()), true);
        // 2. 输出 Excel
        ExcelUtils.write(response, "财务初始余额.xlsx", "财务初始余额",
                FmsInitialBalanceExcelVO.class, rows);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得初始余额导入模板")
    @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('fms:config:initial-balance:import')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void getInitialBalanceImportTemplate(@RequestParam("accountSetId") @NotNull Long accountSetId,
                                                HttpServletResponse response)
            throws IOException {
        // 1. 读取初始余额列表，并展开为 Excel 行
        Long userId = getLoginUserId();
        List<FmsInitialBalanceRespVO> initialBalances = initialBalanceService.getInitialBalanceList(accountSetId, null, userId);
        List<FmsInitialBalanceExcelVO> rows = FmsInitialBalanceExcelHelper.toRows(initialBalances, false);
        // 2. 生成导入模板，一月启用时只需要录入期初余额
        FmsAccountSetDO accountSet = accountSetService.validateAccountSetReadPermission(accountSetId, userId);
        byte[] content = FmsInitialBalanceExcelHelper.writeTemplate(
                rows, LocalDateTimeUtils.isJanuary(accountSet.getStartTime()));
        // 3. 写入响应
        ServletUtils.writeAttachment(response, "财务初始余额导入模板.xlsx", content);
    }

    @PostMapping("/import")
    @Operation(summary = "导入初始余额")
    @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('fms:config:initial-balance:import')")
    @ApiAccessLog(operateType = OperateTypeEnum.IMPORT)
    public CommonResult<Integer> importInitialBalance(
            @RequestParam("accountSetId") @NotNull Long accountSetId,
            @RequestParam("file") MultipartFile file) throws IOException {
        // 1. 读取 Excel
        List<FmsInitialBalanceExcelVO> rows;
        try {
            rows = FmsInitialBalanceExcelHelper.read(file);
        } catch (IllegalArgumentException | IOException ex) {
            throw exception(INITIAL_BALANCE_IMPORT_TEMPLATE_INVALID);
        }
        // 2. 导入数据
        return success(initialBalanceService.importInitialBalance(accountSetId, rows, getLoginUserId()));
    }

}
