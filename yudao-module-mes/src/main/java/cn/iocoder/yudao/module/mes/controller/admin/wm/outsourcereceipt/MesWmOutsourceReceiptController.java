package cn.iocoder.yudao.module.mes.controller.admin.wm.outsourcereceipt;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.outsourcereceipt.vo.MesWmOutsourceReceiptPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.outsourcereceipt.vo.MesWmOutsourceReceiptRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.outsourcereceipt.vo.MesWmOutsourceReceiptSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.vendor.MesMdVendorDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.outsourcereceipt.MesWmOutsourceReceiptDO;
import cn.iocoder.yudao.module.mes.service.md.vendor.MesMdVendorService;
import cn.iocoder.yudao.module.mes.service.pro.workorder.MesProWorkOrderService;
import cn.iocoder.yudao.module.mes.service.wm.outsourcereceipt.MesWmOutsourceReceiptService;
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

@Tag(name = "管理后台 - MES 外协入库单")
@RestController
@RequestMapping("/mes/wm/outsource-receipt")
@Validated
public class MesWmOutsourceReceiptController {

    @Resource
    private MesWmOutsourceReceiptService outsourceReceiptService;

    @Resource
    private MesMdVendorService vendorService;

    @Resource
    private MesProWorkOrderService workOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建外协入库单")
    @PreAuthorize("@ss.hasPermission('mes:wm-outsource-receipt:create')")
    public CommonResult<Long> createOutsourceReceipt(@Valid @RequestBody MesWmOutsourceReceiptSaveReqVO createReqVO) {
        return success(outsourceReceiptService.createOutsourceReceipt(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改外协入库单")
    @PreAuthorize("@ss.hasPermission('mes:wm-outsource-receipt:update')")
    public CommonResult<Boolean> updateOutsourceReceipt(@Valid @RequestBody MesWmOutsourceReceiptSaveReqVO updateReqVO) {
        outsourceReceiptService.updateOutsourceReceipt(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除外协入库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-outsource-receipt:delete')")
    public CommonResult<Boolean> deleteOutsourceReceipt(@RequestParam("id") Long id) {
        outsourceReceiptService.deleteOutsourceReceipt(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得外协入库单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm-outsource-receipt:query')")
    public CommonResult<MesWmOutsourceReceiptRespVO> getOutsourceReceipt(@RequestParam("id") Long id) {
        MesWmOutsourceReceiptDO receipt = outsourceReceiptService.getOutsourceReceipt(id);
        if (receipt == null) {
            return success(null);
        }
        return success(buildRespVOList(Collections.singletonList(receipt)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得外协入库单分页")
    @PreAuthorize("@ss.hasPermission('mes:wm-outsource-receipt:query')")
    public CommonResult<PageResult<MesWmOutsourceReceiptRespVO>> getOutsourceReceiptPage(
            @Valid MesWmOutsourceReceiptPageReqVO pageReqVO) {
        PageResult<MesWmOutsourceReceiptDO> pageResult = outsourceReceiptService.getOutsourceReceiptPage(pageReqVO);
        return success(new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出外协入库单 Excel")
    @PreAuthorize("@ss.hasPermission('mes:wm-outsource-receipt:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOutsourceReceiptExcel(@Valid MesWmOutsourceReceiptPageReqVO pageReqVO,
                                             HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<MesWmOutsourceReceiptDO> pageResult = outsourceReceiptService.getOutsourceReceiptPage(pageReqVO);
        ExcelUtils.write(response, "外协入库单.xls", "数据", MesWmOutsourceReceiptRespVO.class,
                buildRespVOList(pageResult.getList()));
    }

    @PutMapping("/submit")
    @Operation(summary = "提交外协入库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-outsource-receipt:update')")
    public CommonResult<Boolean> submitOutsourceReceipt(@RequestParam("id") Long id) {
        outsourceReceiptService.submitOutsourceReceipt(id);
        return success(true);
    }

    @PutMapping("/stock")
    @Operation(summary = "执行上架")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-outsource-receipt:update')")
    public CommonResult<Boolean> stockOutsourceReceipt(@RequestParam("id") Long id) {
        outsourceReceiptService.stockOutsourceReceipt(id);
        return success(true);
    }

    @PutMapping("/finish")
    @Operation(summary = "完成入库")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-outsource-receipt:finish')")
    public CommonResult<Boolean> finishOutsourceReceipt(@RequestParam("id") Long id) {
        outsourceReceiptService.finishOutsourceReceipt(id);
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消外协入库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-outsource-receipt:update')")
    public CommonResult<Boolean> cancelOutsourceReceipt(@RequestParam("id") Long id) {
        outsourceReceiptService.cancelOutsourceReceipt(id);
        return success(true);
    }

    // ==================== 拼接 VO ====================

    private List<MesWmOutsourceReceiptRespVO> buildRespVOList(List<MesWmOutsourceReceiptDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 获得关联数据
        Map<Long, MesMdVendorDO> vendorMap = vendorService.getVendorMap(
                convertSet(list, MesWmOutsourceReceiptDO::getVendorId));
        Map<Long, MesProWorkOrderDO> workOrderMap = workOrderService.getWorkOrderMap(
                convertSet(list, MesWmOutsourceReceiptDO::getWorkOrderId));
        // 2. 构建结果
        return BeanUtils.toBean(list, MesWmOutsourceReceiptRespVO.class, vo -> {
            MapUtils.findAndThen(vendorMap, vo.getVendorId(),
                    vendor -> vo.setVendorName(vendor.getName()));
            MapUtils.findAndThen(workOrderMap, vo.getWorkOrderId(),
                    workOrder -> vo.setWorkOrderCode(workOrder.getCode()));
        });
    }

}
