package cn.iocoder.yudao.module.fms.controller.admin.voucher;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherAttachmentUpdateReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherExportRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherImportExcelVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherImportRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherImportTemplateVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherMoveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherPageReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherReviewReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherSaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherStatisticsReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherStatisticsRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherSubjectBalanceRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherTidyReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.FmsLedgerListReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.subjectbalance.FmsLedgerSubjectBalanceRespVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsVoucherWordDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.voucher.FmsVoucherDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.voucher.FmsVoucherEntryDO;
import cn.iocoder.yudao.module.fms.enums.common.FmsBalanceDirectionEnum;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingVoucherService;
import cn.iocoder.yudao.module.fms.service.config.FmsVoucherWordService;
import cn.iocoder.yudao.module.fms.service.ledger.FmsLedgerService;
import cn.iocoder.yudao.module.fms.service.voucher.FmsVoucherService;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertListByFlatMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.*;

@Tag(name = "管理后台 - FMS 凭证")
@RestController
@RequestMapping("/fms/voucher")
@Validated
public class FmsVoucherController {

    @Resource
    private FmsVoucherService voucherService;
    @Resource
    private FmsClosingVoucherService closingVoucherService;
    @Resource
    private FmsVoucherWordService voucherWordService;
    @Resource
    private FmsLedgerService ledgerService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private FileApi fileApi;

    @PostMapping("/create")
    @Operation(summary = "创建凭证")
    @PreAuthorize("@ss.hasPermission('fms:voucher:create')")
    public CommonResult<Long> createVoucher(@Valid @RequestBody FmsVoucherSaveReqVO createReqVO) {
        return success(voucherService.createVoucher(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新凭证")
    @PreAuthorize("@ss.hasPermission('fms:voucher:update')")
    public CommonResult<Boolean> updateVoucher(@Valid @RequestBody FmsVoucherSaveReqVO updateReqVO) {
        voucherService.updateVoucher(updateReqVO, getLoginUserId());
        return success(true);
    }

    @PutMapping("/update-attachments")
    @Operation(summary = "更新凭证附件")
    @PreAuthorize("@ss.hasPermission('fms:voucher:update')")
    public CommonResult<Boolean> updateVoucherAttachments(
            @Valid @RequestBody FmsVoucherAttachmentUpdateReqVO updateReqVO) {
        voucherService.updateVoucherAttachments(updateReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "删除凭证")
    @PreAuthorize("@ss.hasPermission('fms:voucher:delete')")
    public CommonResult<Boolean> deleteVoucherList(
            @RequestParam("accountSetId") @NotNull Long accountSetId,
            @RequestParam("ids") @NotEmpty List<Long> ids) {
        voucherService.deleteVoucherList(accountSetId, ids, getLoginUserId());
        return success(true);
    }

    @PutMapping("/update-review-status")
    @Operation(summary = "审核或反审核凭证")
    @PreAuthorize("@ss.hasPermission('fms:voucher:review')")
    public CommonResult<Boolean> updateVoucherReviewStatus(@Valid @RequestBody FmsVoucherReviewReqVO reviewReqVO) {
        voucherService.updateVoucherReviewStatus(reviewReqVO.getAccountSetId(),
                reviewReqVO.getIds(), reviewReqVO.getStatus(), getLoginUserId());
        return success(true);
    }

    @PutMapping("/tidy")
    @Operation(summary = "整理凭证")
    @PreAuthorize("@ss.hasPermission('fms:voucher:tidy')")
    public CommonResult<Boolean> tidyVoucher(@Valid @RequestBody FmsVoucherTidyReqVO tidyReqVO) {
        voucherService.tidyVoucher(tidyReqVO, getLoginUserId());
        return success(true);
    }

    @PutMapping("/move")
    @Operation(summary = "移动凭证")
    @PreAuthorize("@ss.hasPermission('fms:voucher:move')")
    public CommonResult<Boolean> moveVoucher(@Valid @RequestBody FmsVoucherMoveReqVO moveReqVO) {
        voucherService.moveVoucher(moveReqVO, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得凭证详情")
    @PreAuthorize("@ss.hasAnyPermissions('fms:voucher:query', 'fms:voucher:update')")
    public CommonResult<FmsVoucherRespVO> getVoucher(@RequestParam("accountSetId") @NotNull Long accountSetId,
                                                     @RequestParam("id") @NotNull Long id) {
        FmsVoucherDO voucher = voucherService.getVoucher(accountSetId, id, getLoginUserId());
        return success(buildVoucherRespVO(accountSetId, voucher));
    }

    @GetMapping("/subject-balance-list")
    @Operation(summary = "获得凭证科目余额列表")
    @PreAuthorize("@ss.hasAnyPermissions('fms:voucher:query', 'fms:voucher:create', 'fms:voucher:update')")
    public CommonResult<List<FmsVoucherSubjectBalanceRespVO>> getVoucherSubjectBalanceList(
            @RequestParam("accountSetId") @NotNull Long accountSetId,
            @RequestParam("month") @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "会计期间格式不正确") String month) {
        List<FmsLedgerSubjectBalanceRespVO> balances = ledgerService.getSubjectBalanceList(
                new FmsLedgerListReqVO().setAccountSetId(accountSetId).setStartMonth(month).setEndMonth(month), getLoginUserId());
        // 拼接 VO 返回
        List<FmsVoucherSubjectBalanceRespVO> respVOList = new ArrayList<>();
        appendVoucherSubjectBalanceRespVOList(respVOList, balances);
        return success(respVOList);
    }

    @GetMapping("/auxiliary-balance")
    @Operation(summary = "获得凭证辅助核算组合余额")
    @PreAuthorize("@ss.hasAnyPermissions('fms:voucher:query', 'fms:voucher:create', 'fms:voucher:update')")
    public CommonResult<FmsVoucherSubjectBalanceRespVO> getVoucherAuxiliaryBalance(
            @RequestParam("accountSetId") @NotNull Long accountSetId,
            @RequestParam("month")
            @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "会计期间格式不正确") String month,
            @RequestParam("subjectId") @NotNull Long subjectId,
            @RequestParam("auxiliaryItemIds") @NotEmpty List<Long> auxiliaryItemIds) {
        BigDecimal signedBalance = ledgerService.getAuxiliaryCombinationBalance(
                accountSetId, month, subjectId, auxiliaryItemIds, getLoginUserId());
        return success(new FmsVoucherSubjectBalanceRespVO().setSubjectId(subjectId)
                .setBalanceDirection(FmsBalanceDirectionEnum.valueOf(signedBalance).getName())
                .setBalance(signedBalance.abs()));
    }

    @GetMapping("/page")
    @Operation(summary = "获得凭证分页")
    @PreAuthorize("@ss.hasPermission('fms:voucher:query')")
    public CommonResult<PageResult<FmsVoucherRespVO>> getVoucherPage(@Valid FmsVoucherPageReqVO pageReqVO) {
        PageResult<FmsVoucherDO> pageResult = voucherService.getVoucherPage(pageReqVO, getLoginUserId());
        return success(new PageResult<>(
                buildVoucherRespVOList(pageReqVO.getAccountSetId(), pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/print-list")
    @Operation(summary = "获得待打印凭证列表")
    @PreAuthorize("@ss.hasPermission('fms:voucher:print')")
    public CommonResult<List<FmsVoucherRespVO>> getVoucherPrintList(@Valid FmsVoucherPageReqVO queryReqVO) {
        queryReqVO.setPageSize(PAGE_SIZE_NONE);
        List<FmsVoucherDO> vouchers = voucherService.getVoucherPage(queryReqVO, getLoginUserId()).getList();
        return success(buildVoucherRespVOList(queryReqVO.getAccountSetId(), vouchers));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出凭证")
    @PreAuthorize("@ss.hasPermission('fms:voucher:export')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void exportVoucherExcel(@Valid FmsVoucherPageReqVO exportReqVO, HttpServletResponse response)
            throws IOException {
        exportReqVO.setPageSize(PAGE_SIZE_NONE);
        List<FmsVoucherDO> list = voucherService.getVoucherPage(exportReqVO, getLoginUserId()).getList();
        List<FmsVoucherRespVO> vouchers = buildVoucherRespVOList(exportReqVO.getAccountSetId(), list);
        ExcelUtils.write(response, "凭证列表.xls", "凭证列表", FmsVoucherExportRespVO.class,
                buildVoucherExportRespVOList(vouchers));
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得凭证导入模板")
    @PreAuthorize("@ss.hasPermission('fms:voucher:import')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void getVoucherImportTemplate(@RequestParam("accountSetId") @NotNull Long accountSetId,
                                         HttpServletResponse response) throws IOException {
        FmsVoucherImportTemplateVO templateData = voucherService.getVoucherImportTemplateData(accountSetId, getLoginUserId());
        ServletUtils.writeAttachment(response, "凭证导入模板.xlsx", FmsVoucherImportExcelHelper.writeTemplate(templateData));
    }

    @PostMapping("/import")
    @Operation(summary = "导入凭证")
    @Parameters({
            @Parameter(name = "accountSetId", description = "账套编号", required = true),
            @Parameter(name = "file", description = "Excel 文件", required = true)
    })
    @PreAuthorize("@ss.hasPermission('fms:voucher:import')")
    @ApiAccessLog(operateType = OperateTypeEnum.IMPORT)
    public CommonResult<FmsVoucherImportRespVO> importVoucher(@RequestParam("accountSetId") @NotNull Long accountSetId,
                                                              @RequestParam("file") MultipartFile file) throws IOException {
        FmsVoucherImportTemplateVO templateData = voucherService.getVoucherImportTemplateData(accountSetId, getLoginUserId());
        // 1. 解析导入文件
        List<FmsVoucherImportExcelVO> rows;
        try {
            rows = FmsVoucherImportExcelHelper.read(file, templateData.getAuxiliaryTypes());
        } catch (IllegalArgumentException | IOException exception) {
            throw exception(VOUCHER_IMPORT_TEMPLATE_INVALID);
        }
        // 2. 导入凭证
        FmsVoucherImportRespVO respVO = voucherService.importVoucher(
                accountSetId, rows, getLoginUserId());

        // 3. 生成错误数据文件
        if (CollUtil.isNotEmpty(respVO.getErrorRows())) {
            byte[] errorFile = FmsVoucherImportExcelHelper.writeErrorFile(respVO.getErrorRows());
            respVO.setErrorFileUrl(fileApi.createFile(errorFile,
                    "凭证导入错误数据.xlsx", "fms/voucher/import",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        }
        return success(respVO);
    }

    @GetMapping("/statistics/list")
    @Operation(summary = "获得凭证汇总")
    @PreAuthorize("@ss.hasPermission('fms:voucher:statistics:query')")
    public CommonResult<List<FmsVoucherStatisticsRespVO>> getVoucherStatisticsList(@Valid FmsVoucherStatisticsReqVO queryReqVO) {
        return success(voucherService.getVoucherStatisticsList(queryReqVO, getLoginUserId()));
    }

    @GetMapping("/statistics/export-excel")
    @Operation(summary = "导出凭证汇总")
    @PreAuthorize("@ss.hasPermission('fms:voucher:statistics:export')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void exportVoucherStatisticsExcel(
            @Valid FmsVoucherStatisticsReqVO queryReqVO, HttpServletResponse response) throws IOException {
        List<FmsVoucherStatisticsRespVO> list = voucherService.getVoucherStatisticsList(queryReqVO, getLoginUserId());
        ExcelUtils.write(response, "凭证汇总表.xls", "凭证汇总", FmsVoucherStatisticsRespVO.class, list);
    }

    @GetMapping("/next-number")
    @Operation(summary = "获得下一凭证号")
    @Parameters({
            @Parameter(name = "accountSetId", description = "账套编号", required = true),
            @Parameter(name = "voucherWordId", description = "凭证字编号", required = true),
            @Parameter(name = "voucherTime", description = "凭证日期", required = true)
    })
    @PreAuthorize("@ss.hasAnyPermissions('fms:voucher:query', 'fms:voucher:create', 'fms:voucher:update')")
    public CommonResult<Integer> getNextVoucherNumber(
            @RequestParam("accountSetId") @NotNull Long accountSetId,
            @RequestParam("voucherWordId") @NotNull Long voucherWordId,
            @RequestParam("voucherTime") @NotNull
            @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND) LocalDateTime voucherTime) {
        return success(voucherService.getNextVoucherNumber(accountSetId, voucherWordId, voucherTime, getLoginUserId()));
    }

    // ==================== 拼接 VO ====================

    private FmsVoucherRespVO buildVoucherRespVO(Long accountSetId, FmsVoucherDO voucher) {
        return CollUtil.getFirst(buildVoucherRespVOList(
                accountSetId, Collections.singletonList(voucher)));
    }

    private void appendVoucherSubjectBalanceRespVOList(
            List<FmsVoucherSubjectBalanceRespVO> respVOList,
            List<FmsLedgerSubjectBalanceRespVO> balances) {
        balances.forEach(balance -> {
            BigDecimal debitAmount = balance.getEndingDebitAmount() != null
                    ? balance.getEndingDebitAmount() : BigDecimal.ZERO;
            BigDecimal creditAmount = balance.getEndingCreditAmount() != null
                    ? balance.getEndingCreditAmount() : BigDecimal.ZERO;
            respVOList.add(new FmsVoucherSubjectBalanceRespVO().setSubjectId(balance.getSubjectId())
                    .setBalanceDirection(balance.getEndingBalanceDirection())
                    .setBalance(debitAmount.add(creditAmount)));
            appendVoucherSubjectBalanceRespVOList(respVOList, balance.getChildren());
        });
    }

    private List<FmsVoucherRespVO> buildVoucherRespVOList(
            Long accountSetId, List<FmsVoucherDO> vouchers) {
        if (CollUtil.isEmpty(vouchers)) {
            return Collections.emptyList();
        }
        // 1. 批量查询凭证字、分录和用户
        Set<Long> voucherIds = convertSet(vouchers, FmsVoucherDO::getId);
        Map<Long, FmsVoucherWordDO> voucherWordMap = voucherWordService.getVoucherWordMap(accountSetId);
        Map<Long, List<FmsVoucherEntryDO>> entryMap = voucherService.getVoucherEntryMap(voucherIds);
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertListByFlatMap(
                vouchers, voucher -> Stream.of(NumberUtils.parseLong(voucher.getCreator()),
                        voucher.getReviewerUserId())));
        Set<Long> closingVoucherIds = closingVoucherService.getClosingVoucherIdSet(accountSetId, voucherIds);

        // 2. 拼接凭证响应
        return convertList(vouchers, voucher -> {
            FmsVoucherRespVO respVO = BeanUtils.toBean(voucher, FmsVoucherRespVO.class);
            respVO.setEntries(BeanUtils.toBean(
                    entryMap.getOrDefault(voucher.getId(), Collections.emptyList()),
                    FmsVoucherRespVO.Entry.class));
            respVO.setCreatorUserId(NumberUtils.parseLong(voucher.getCreator()));
            respVO.setClosingGenerated(closingVoucherIds.contains(voucher.getId()));
            MapUtils.findAndThen(voucherWordMap, voucher.getVoucherWordId(),
                    word -> respVO.setVoucherWordName(word.getName()));
            MapUtils.findAndThen(userMap, respVO.getCreatorUserId(),
                    user -> respVO.setCreatorUserName(user.getNickname()));
            MapUtils.findAndThen(userMap, respVO.getReviewerUserId(),
                    user -> respVO.setReviewerUserName(user.getNickname()));
            return respVO;
        });
    }

    private List<FmsVoucherExportRespVO> buildVoucherExportRespVOList(
            List<FmsVoucherRespVO> vouchers) {
        return convertListByFlatMap(vouchers, voucher -> voucher.getEntries().stream().map(entry -> {
            String subjectName = entry.getSubjectName();
            if (CollUtil.isNotEmpty(entry.getAuxiliaries())) {
                subjectName += " / " + String.join("、", convertList(
                        entry.getAuxiliaries(), FmsVoucherRespVO.Entry.AuxiliaryItem::getName));
            }
            return new FmsVoucherExportRespVO().setVoucherTime(voucher.getVoucherTime())
                    .setVoucherWordName(voucher.getVoucherWordName())
                    .setVoucherNumber(voucher.getVoucherNumber()).setDigest(entry.getDigest())
                    .setSubjectCode(entry.getSubjectCode()).setSubjectName(subjectName)
                    .setDebitAmount(entry.getDebitAmount()).setCreditAmount(entry.getCreditAmount())
                    .setAttachmentCount(voucher.getAttachmentCount())
                    .setCreatorUserName(voucher.getCreatorUserName())
                    .setReviewerUserName(voucher.getReviewerUserName());
        }));
    }

}
