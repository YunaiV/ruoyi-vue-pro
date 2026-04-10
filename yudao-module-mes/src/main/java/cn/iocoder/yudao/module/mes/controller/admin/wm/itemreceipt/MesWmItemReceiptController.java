package cn.iocoder.yudao.module.mes.controller.admin.wm.itemreceipt;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.itemreceipt.vo.MesWmItemReceiptPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.itemreceipt.vo.MesWmItemReceiptRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.itemreceipt.vo.MesWmItemReceiptSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.vendor.MesMdVendorDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.iqc.MesQcIqcDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.arrivalnotice.MesWmArrivalNoticeDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemreceipt.MesWmItemReceiptDO;
import cn.iocoder.yudao.module.mes.service.md.vendor.MesMdVendorService;
import cn.iocoder.yudao.module.mes.service.qc.iqc.MesQcIqcService;
import cn.iocoder.yudao.module.mes.service.wm.arrivalnotice.MesWmArrivalNoticeService;
import cn.iocoder.yudao.module.mes.service.wm.itemreceipt.MesWmItemReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - MES 采购入库单")
@RestController
@RequestMapping("/mes/wm/item-receipt")
@Validated
public class MesWmItemReceiptController {

    @Resource
    private MesWmItemReceiptService itemReceiptService;

    @Resource
    private MesMdVendorService vendorService;

    @Resource
    private MesQcIqcService iqcService;

    @Resource
    private MesWmArrivalNoticeService arrivalNoticeService;

    @PostMapping("/create")
    @Operation(summary = "创建采购入库单")
    @PreAuthorize("@ss.hasPermission('mes:wm-item-receipt:create')")
    public CommonResult<Long> createItemReceipt(@Valid @RequestBody MesWmItemReceiptSaveReqVO createReqVO) {
        return success(itemReceiptService.createItemReceipt(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改采购入库单")
    @PreAuthorize("@ss.hasPermission('mes:wm-item-receipt:update')")
    public CommonResult<Boolean> updateItemReceipt(@Valid @RequestBody MesWmItemReceiptSaveReqVO updateReqVO) {
        itemReceiptService.updateItemReceipt(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除采购入库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-item-receipt:delete')")
    public CommonResult<Boolean> deleteItemReceipt(@RequestParam("id") Long id) {
        itemReceiptService.deleteItemReceipt(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得采购入库单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm-item-receipt:query')")
    public CommonResult<MesWmItemReceiptRespVO> getItemReceipt(@RequestParam("id") Long id) {
        MesWmItemReceiptDO receipt = itemReceiptService.getItemReceipt(id);
        if (receipt == null) {
            return success(null);
        }
        return success(buildRespVOList(Collections.singletonList(receipt)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得采购入库单分页")
    @PreAuthorize("@ss.hasPermission('mes:wm-item-receipt:query')")
    public CommonResult<PageResult<MesWmItemReceiptRespVO>> getItemReceiptPage(
            @Valid MesWmItemReceiptPageReqVO pageReqVO) {
        PageResult<MesWmItemReceiptDO> pageResult = itemReceiptService.getItemReceiptPage(pageReqVO);
        return success(new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出采购入库单 Excel")
    @PreAuthorize("@ss.hasPermission('mes:wm-item-receipt:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportItemReceiptExcel(@Valid MesWmItemReceiptPageReqVO pageReqVO,
                                        HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<MesWmItemReceiptDO> pageResult = itemReceiptService.getItemReceiptPage(pageReqVO);
        ExcelUtils.write(response, "采购入库单.xls", "数据", MesWmItemReceiptRespVO.class,
                buildRespVOList(pageResult.getList()));
    }

    @PutMapping("/submit")
    @Operation(summary = "提交采购入库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-item-receipt:update')")
    public CommonResult<Boolean> submitItemReceipt(@RequestParam("id") Long id) {
        itemReceiptService.submitItemReceipt(id);
        return success(true);
    }

    @PutMapping("/stock")
    @Operation(summary = "执行上架")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-item-receipt:update')")
    public CommonResult<Boolean> stockItemReceipt(@RequestParam("id") Long id) {
        itemReceiptService.stockItemReceipt(id);
        return success(true);
    }

    @PutMapping("/finish")
    @Operation(summary = "执行入库")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-item-receipt:finish')")
    public CommonResult<Boolean> finishItemReceipt(@RequestParam("id") Long id) {
        itemReceiptService.finishItemReceipt(id);
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消采购入库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-item-receipt:update')")
    public CommonResult<Boolean> cancelItemReceipt(@RequestParam("id") Long id) {
        itemReceiptService.cancelItemReceipt(id);
        return success(true);
    }

    // ==================== 拼接 VO ====================

    private List<MesWmItemReceiptRespVO> buildRespVOList(List<MesWmItemReceiptDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 获得关联数据
        Map<Long, MesMdVendorDO> vendorMap = vendorService.getVendorMap(
                convertSet(list, MesWmItemReceiptDO::getVendorId));
        Map<Long, MesQcIqcDO> iqcMap = iqcService.getIqcMap(
                convertSet(list, MesWmItemReceiptDO::getIqcId));
        Map<Long, MesWmArrivalNoticeDO> noticeMap = arrivalNoticeService.getArrivalNoticeMap(
                convertSet(list, MesWmItemReceiptDO::getNoticeId));
        // 2. 构建结果
        return BeanUtils.toBean(list, MesWmItemReceiptRespVO.class, vo -> {
            MapUtils.findAndThen(vendorMap, vo.getVendorId(),
                    vendor -> vo.setVendorName(vendor.getName()));
            MapUtils.findAndThen(iqcMap, vo.getIqcId(),
                    iqc -> vo.setIqcCode(iqc.getCode()));
            MapUtils.findAndThen(noticeMap, vo.getNoticeId(), notice ->
                    vo.setNoticeCode(notice.getCode()).setPurchaseOrderCode(notice.getPurchaseOrderCode()));
        });
    }

}
