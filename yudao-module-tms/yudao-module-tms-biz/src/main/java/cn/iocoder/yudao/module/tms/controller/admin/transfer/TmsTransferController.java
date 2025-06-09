package cn.iocoder.yudao.module.tms.controller.admin.transfer;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.idempotent.core.annotation.Idempotent;
import cn.iocoder.yudao.module.tms.controller.admin.transfer.vo.*;
import cn.iocoder.yudao.module.tms.service.transfer.TmsTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.IMPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 调拨单")
@RestController
@RequestMapping("/tms/transfer")
public class TmsTransferController {

    @Resource
    private TmsTransferService transferService;

    @PostMapping("/create")
    @Operation(summary = "创建调拨单")
    @Idempotent
    @PreAuthorize("@ss.hasPermission('tms:transfer:create')")
    public CommonResult<Long> createTransfer(@RequestBody TmsTransferSaveReqVO createReqVO) {
        return success(transferService.createTransfer(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新调拨单")
    @PreAuthorize("@ss.hasPermission('tms:transfer:update')")
    public CommonResult<Boolean> updateTransfer(@RequestBody TmsTransferSaveReqVO updateReqVO) {
        transferService.updateTransfer(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除调拨单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('tms:transfer:delete')")
    public CommonResult<Boolean> deleteTransfer(@RequestParam("id") Long id) {
        transferService.deleteTransfer(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得调拨单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('tms:transfer:query')")
    public CommonResult<TmsTransferRespVO> getTransfer(@RequestParam("id") Long id) {
        return success(transferService.getTransferRespVO(id));
    }

    @PostMapping("/page")
    @Operation(summary = "获得调拨单分页")
    @PreAuthorize("@ss.hasPermission('tms:transfer:query')")
    public CommonResult<PageResult<TmsTransferRespVO>> getTransferPage(@RequestBody(required = false) TmsTransferPageReqVO pageReqVO) {
        return success(transferService.getTmsTransferRespVOPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出调拨单 Excel")
    @PreAuthorize("@ss.hasPermission('tms:transfer:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportTransferExcel(TmsTransferPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TmsTransferRespVO> respVOList = transferService.getTmsTransferRespVOPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "调拨单.xls", "数据", TmsTransferRespVO.class, respVOList);
    }

    @PostMapping("/import-excel")
    @Operation(summary = "导入调拨单 Excel")
    @ApiAccessLog(operateType = IMPORT)
    @PreAuthorize("@ss.hasPermission('tms:transfer:import')")
    public CommonResult<Boolean> importTransferExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<TmsTransferSaveReqVO> list = ExcelUtils.read(file, TmsTransferSaveReqVO.class);
        // 可根据业务需要批量保存或校验
        return success(false);
    }

    @PutMapping("/submit-audit")
    @Operation(summary = "提交审核")
    @PreAuthorize("@ss.hasPermission('tms:transfer:audit')")
    public CommonResult<Boolean> submitAudit(@RequestBody @Size(min = 1, message = "提交审核的单据数量不小于1") List<Long> transferIds) {
        transferService.submitAudit(transferIds);
        return success(true);
    }

    @PutMapping("/audit-status")
    @Operation(summary = "审核/反审核")
    @PreAuthorize("@ss.hasPermission('tms:transfer:review')")
    public CommonResult<Boolean> audit(@RequestBody TmsTransferAuditReqVO reqVO) {
        transferService.review(reqVO);
        return success(true);
    }

    //开关 item-off
    @PutMapping("/off-status")
    @Operation(summary = "开关")
    @PreAuthorize("@ss.hasPermission('tms:transfer:item-off')")
    public CommonResult<Boolean> offStatus(@RequestBody TmsTransferOffStatusReqVO reqVO) {
        transferService.switchOpen(reqVO);
        return success(true);
    }

    @PostMapping("/get-sellable-qty")
    @Operation(summary = "获取可售库存数量")
    @PreAuthorize("@ss.hasPermission('tms:transfer:query')")
    public CommonResult<TmsTransferSellableQtyRespVO> getSellableQty(@RequestBody TmsTransferSellableQtyReqVO reqVO) {
        return success(transferService.getSellableQty(reqVO));
    }
}