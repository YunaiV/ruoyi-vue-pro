package cn.iocoder.yudao.module.fms.controller.admin.ledger;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.FmsLedgerAuxiliaryListReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.FmsLedgerListReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.auxiliarybalance.FmsLedgerAuxiliaryBalanceRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.auxiliarydetail.FmsLedgerAuxiliaryDetailRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.detail.FmsLedgerDetailExportRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.detail.FmsLedgerDetailRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.general.FmsLedgerGeneralRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.multicolumn.FmsLedgerMultiColumnRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.quantitydetail.FmsLedgerQuantityDetailRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.quantitygeneral.FmsLedgerQuantityGeneralRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.subjectbalance.FmsLedgerSubjectBalanceRespVO;
import cn.iocoder.yudao.module.fms.service.ledger.FmsLedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - FMS 账簿")
@RestController
@RequestMapping("/fms/ledger")
@Validated
public class FmsLedgerController {

    @Resource
    private FmsLedgerService ledgerService;

    // ==================== 明细账 ====================

    @GetMapping("/detail/list")
    @Operation(summary = "获得明细账")
    @PreAuthorize("@ss.hasPermission('fms:ledger:detail:query')")
    public CommonResult<List<FmsLedgerDetailRespVO>> getDetailList(@Valid FmsLedgerListReqVO listReqVO) {
        return success(ledgerService.getDetailList(listReqVO, getLoginUserId()));
    }

    @GetMapping("/detail/subject-list")
    @Operation(summary = "获得指定期间有发生额的明细账科目")
    public CommonResult<List<FmsSubjectRespVO>> getDetailSubjectList(@Valid FmsLedgerListReqVO listReqVO) {
        // TODO @AI：先查询出来，list 变量；然后在去 convert；字段不多，倒是可以考虑 beanutils 去 copy 下？
        return success(convertList(ledgerService.getDetailSubjectList(listReqVO, getLoginUserId()), subject ->
                new FmsSubjectRespVO().setId(subject.getId()).setParentId(subject.getParentId())
                        .setCode(subject.getCode()).setName(subject.getName()).setType(subject.getType())
                        .setBalanceDirection(subject.getBalanceDirection()).setStatus(subject.getStatus())
                        .setLevel(subject.getLevel()).setAuxiliaryTypeIds(subject.getAuxiliaryTypeIds())
                        .setQuantityAccounting(subject.getQuantityAccounting()).setQuantityUnit(subject.getQuantityUnit())));
    }

    @GetMapping("/detail/export-excel")
    @Operation(summary = "导出明细账")
    @PreAuthorize("@ss.hasPermission('fms:ledger:detail:export')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void exportDetail(@Valid FmsLedgerListReqVO listReqVO, HttpServletResponse response) throws IOException {
        List<FmsLedgerDetailExportRespVO> rows = convertList(
                ledgerService.getDetailList(listReqVO, getLoginUserId()), item ->
                        BeanUtils.toBean(item, FmsLedgerDetailExportRespVO.class)
                                .setSubject(item.getSubjectCode() + " " + item.getSubjectName()));
        ExcelUtils.write(response, "明细账.xls", "明细账", FmsLedgerDetailExportRespVO.class, rows);
    }

    // ==================== 总账 ====================

    @GetMapping("/general/list")
    @Operation(summary = "获得总账")
    @PreAuthorize("@ss.hasPermission('fms:ledger:general:query')")
    public CommonResult<List<FmsLedgerGeneralRespVO>> getGeneralList(@Valid FmsLedgerListReqVO listReqVO) {
        return success(ledgerService.getGeneralList(listReqVO, getLoginUserId()));
    }

    @GetMapping("/general/export-excel")
    @Operation(summary = "导出总账")
    @PreAuthorize("@ss.hasPermission('fms:ledger:general:export')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void exportGeneral(@Valid FmsLedgerListReqVO listReqVO, HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "总账.xls", "总账", FmsLedgerGeneralRespVO.class,
                ledgerService.getGeneralList(listReqVO, getLoginUserId()));
    }

    // ==================== 科目余额表 ====================

    @GetMapping("/subject-balance/list")
    @Operation(summary = "获得科目余额表")
    @PreAuthorize("@ss.hasPermission('fms:ledger:subject-balance:query')")
    public CommonResult<List<FmsLedgerSubjectBalanceRespVO>> getSubjectBalanceList(@Valid FmsLedgerListReqVO listReqVO) {
        return success(ledgerService.getSubjectBalanceList(listReqVO, getLoginUserId()));
    }

    @GetMapping("/subject-balance/export-excel")
    @Operation(summary = "导出科目余额表")
    @PreAuthorize("@ss.hasPermission('fms:ledger:subject-balance:export')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void exportSubjectBalance(@Valid FmsLedgerListReqVO listReqVO, HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "科目余额表.xls", "科目余额表", FmsLedgerSubjectBalanceRespVO.class,
                flattenSubjectBalanceList(ledgerService.getSubjectBalanceList(listReqVO, getLoginUserId())));
    }

    // ==================== 多栏账 ====================

    @GetMapping("/multi-column/list")
    @Operation(summary = "获得多栏账")
    @PreAuthorize("@ss.hasPermission('fms:ledger:multi-column:query')")
    public CommonResult<FmsLedgerMultiColumnRespVO> getMultiColumn(@Valid FmsLedgerListReqVO listReqVO) {
        return success(ledgerService.getMultiColumn(listReqVO, getLoginUserId()));
    }

    @GetMapping("/multi-column/export-excel")
    @Operation(summary = "导出多栏账")
    @PreAuthorize("@ss.hasPermission('fms:ledger:multi-column:export')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void exportMultiColumn(@Valid FmsLedgerListReqVO listReqVO, HttpServletResponse response) throws IOException {
        FmsLedgerMultiColumnRespVO result = ledgerService.getMultiColumn(listReqVO, getLoginUserId());
        ExcelUtils.write(response, "多栏账.xls", "多栏账",
                FmsLedgerMultiColumnExcelHelper.buildHead(result),
                FmsLedgerMultiColumnExcelHelper.buildData(result));
    }

    // ==================== 核算项目明细账 ====================

    @GetMapping("/auxiliary-detail/list")
    @Operation(summary = "获得核算项目明细账")
    @PreAuthorize("@ss.hasPermission('fms:ledger:detail:query')")
    public CommonResult<List<FmsLedgerAuxiliaryDetailRespVO>> getAuxiliaryDetailList(@Valid FmsLedgerAuxiliaryListReqVO listReqVO) {
        return success(ledgerService.getAuxiliaryDetailList(listReqVO, getLoginUserId()));
    }

    @GetMapping("/auxiliary-detail/export-excel")
    @Operation(summary = "导出核算项目明细账")
    @PreAuthorize("@ss.hasPermission('fms:ledger:detail:export')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void exportAuxiliaryDetail(@Valid FmsLedgerAuxiliaryListReqVO listReqVO, HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "核算项目明细账.xls", "核算项目明细账",
                FmsLedgerAuxiliaryDetailRespVO.class,
                ledgerService.getAuxiliaryDetailList(listReqVO, getLoginUserId()));
    }

    // ==================== 核算项目余额表 ====================

    @GetMapping("/auxiliary-balance/list")
    @Operation(summary = "获得核算项目余额表")
    @PreAuthorize("@ss.hasPermission('fms:ledger:subject-balance:query')")
    public CommonResult<List<FmsLedgerAuxiliaryBalanceRespVO>> getAuxiliaryBalanceList(@Valid FmsLedgerAuxiliaryListReqVO listReqVO) {
        return success(ledgerService.getAuxiliaryBalanceList(listReqVO, getLoginUserId()));
    }

    @GetMapping("/auxiliary-balance/export-excel")
    @Operation(summary = "导出核算项目余额表")
    @PreAuthorize("@ss.hasPermission('fms:ledger:subject-balance:export')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void exportAuxiliaryBalance(@Valid FmsLedgerAuxiliaryListReqVO listReqVO, HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "核算项目余额表.xls", "核算项目余额表",
                FmsLedgerAuxiliaryBalanceRespVO.class,
                ledgerService.getAuxiliaryBalanceList(listReqVO, getLoginUserId()));
    }

    // ==================== 数量金额明细账 ====================

    @GetMapping("/quantity-detail/list")
    @Operation(summary = "获得数量金额明细账")
    @PreAuthorize("@ss.hasPermission('fms:ledger:detail:query')")
    public CommonResult<List<FmsLedgerQuantityDetailRespVO>> getQuantityDetailList(@Valid FmsLedgerListReqVO listReqVO) {
        return success(ledgerService.getQuantityDetailList(listReqVO, getLoginUserId()));
    }

    @GetMapping("/quantity-detail/export-excel")
    @Operation(summary = "导出数量金额明细账")
    @PreAuthorize("@ss.hasPermission('fms:ledger:detail:export')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void exportQuantityDetail(@Valid FmsLedgerListReqVO listReqVO, HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "数量金额明细账.xls", "数量金额明细账",
                FmsLedgerQuantityDetailRespVO.class,
                ledgerService.getQuantityDetailList(listReqVO, getLoginUserId()));
    }

    // ==================== 数量金额总账 ====================

    @GetMapping("/quantity-general/list")
    @Operation(summary = "获得数量金额总账")
    @PreAuthorize("@ss.hasPermission('fms:ledger:general:query')")
    public CommonResult<List<FmsLedgerQuantityGeneralRespVO>> getQuantityGeneralList(@Valid FmsLedgerListReqVO listReqVO) {
        return success(ledgerService.getQuantityGeneralList(listReqVO, getLoginUserId()));
    }

    @GetMapping("/quantity-general/export-excel")
    @Operation(summary = "导出数量金额总账")
    @PreAuthorize("@ss.hasPermission('fms:ledger:general:export')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void exportQuantityGeneral(@Valid FmsLedgerListReqVO listReqVO, HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "数量金额总账.xls", "数量金额总账",
                FmsLedgerQuantityGeneralRespVO.class,
                flattenQuantityGeneralList(ledgerService.getQuantityGeneralList(
                        listReqVO, getLoginUserId())));
    }

    private static List<FmsLedgerSubjectBalanceRespVO> flattenSubjectBalanceList(List<FmsLedgerSubjectBalanceRespVO> list) {
        List<FmsLedgerSubjectBalanceRespVO> result = new ArrayList<>();
        list.forEach(item -> {
            result.add(item);
            result.addAll(flattenSubjectBalanceList(item.getChildren()));
        });
        return result;
    }

    private static List<FmsLedgerQuantityGeneralRespVO> flattenQuantityGeneralList(List<FmsLedgerQuantityGeneralRespVO> list) {
        List<FmsLedgerQuantityGeneralRespVO> result = new ArrayList<>();
        list.forEach(item -> {
            if (Boolean.TRUE.equals(item.getQuantityAccounting()) || CollUtil.isNotEmpty(item.getChildren())) {
                result.add(item);
            }
            result.addAll(flattenQuantityGeneralList(item.getChildren()));
        });
        return result;
    }

}
