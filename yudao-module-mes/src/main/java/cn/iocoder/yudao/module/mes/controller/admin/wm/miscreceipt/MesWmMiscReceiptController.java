package cn.iocoder.yudao.module.mes.controller.admin.wm.miscreceipt;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscreceipt.vo.MesWmMiscReceiptPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscreceipt.vo.MesWmMiscReceiptRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscreceipt.vo.MesWmMiscReceiptSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.miscreceipt.MesWmMiscReceiptDO;
import cn.iocoder.yudao.module.mes.service.wm.miscreceipt.MesWmMiscReceiptService;
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
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * MES 杂项入库单 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - MES 杂项入库单")
@RestController
@RequestMapping("/mes/wm/misc-receipt")
@Validated
public class MesWmMiscReceiptController {

    @Resource
    private MesWmMiscReceiptService miscReceiptService;

    @PostMapping("/create")
    @Operation(summary = "创建杂项入库单")
    @PreAuthorize("@ss.hasPermission('mes:wm:misc-receipt:create')")
    public CommonResult<Long> createMiscReceipt(@Valid @RequestBody MesWmMiscReceiptSaveReqVO createReqVO) {
        return success(miscReceiptService.createMiscReceipt(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改杂项入库单")
    @PreAuthorize("@ss.hasPermission('mes:wm:misc-receipt:update')")
    public CommonResult<Boolean> updateMiscReceipt(@Valid @RequestBody MesWmMiscReceiptSaveReqVO updateReqVO) {
        miscReceiptService.updateMiscReceipt(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除杂项入库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm:misc-receipt:delete')")
    public CommonResult<Boolean> deleteMiscReceipt(@RequestParam("id") Long id) {
        miscReceiptService.deleteMiscReceipt(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得杂项入库单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm:misc-receipt:query')")
    public CommonResult<MesWmMiscReceiptRespVO> getMiscReceipt(@RequestParam("id") Long id) {
        MesWmMiscReceiptDO receipt = miscReceiptService.getMiscReceipt(id);
        return success(BeanUtils.toBean(receipt, MesWmMiscReceiptRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得杂项入库单分页")
    @PreAuthorize("@ss.hasPermission('mes:wm:misc-receipt:query')")
    public CommonResult<PageResult<MesWmMiscReceiptRespVO>> getMiscReceiptPage(@Valid MesWmMiscReceiptPageReqVO pageReqVO) {
        PageResult<MesWmMiscReceiptDO> pageResult = miscReceiptService.getMiscReceiptPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, MesWmMiscReceiptRespVO.class));
    }

    @PutMapping("/submit")
    @Operation(summary = "提交审批")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm:misc-receipt:submit')")
    public CommonResult<Boolean> submitMiscReceipt(@RequestParam("id") Long id) {
        miscReceiptService.submitMiscReceipt(id);
        return success(true);
    }

    @PutMapping("/finish")
    @Operation(summary = "执行入库")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm:misc-receipt:finish')")
    public CommonResult<Boolean> finishMiscReceipt(@RequestParam("id") Long id) {
        miscReceiptService.finishMiscReceipt(id);
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消杂项入库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm:misc-receipt:cancel')")
    public CommonResult<Boolean> cancelMiscReceipt(@RequestParam("id") Long id) {
        miscReceiptService.cancelMiscReceipt(id);
        return success(true);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出杂项入库单 Excel")
    @PreAuthorize("@ss.hasPermission('mes:wm:misc-receipt:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportMiscReceiptExcel(@Valid MesWmMiscReceiptPageReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(Integer.MAX_VALUE);
        List<MesWmMiscReceiptDO> list = miscReceiptService.getMiscReceiptPage(pageReqVO).getList();
        ExcelUtils.write(response, "杂项入库单.xls", "数据", MesWmMiscReceiptRespVO.class,
                BeanUtils.toBean(list, MesWmMiscReceiptRespVO.class));
    }

}
