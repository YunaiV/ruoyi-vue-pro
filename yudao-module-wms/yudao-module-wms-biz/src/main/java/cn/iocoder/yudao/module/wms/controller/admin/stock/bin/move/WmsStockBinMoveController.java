package cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move;

import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.constraints.*;
import jakarta.validation.*;
import jakarta.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.WmsStockBinMoveDO;
import cn.iocoder.yudao.module.wms.service.stock.bin.move.WmsStockBinMoveService;

@Tag(name = "管理后台 - 库位移动")
@RestController
@RequestMapping("/wms/stock-bin-move")
@Validated
public class WmsStockBinMoveController {

    @Resource
    private WmsStockBinMoveService stockBinMoveService;

    @PostMapping("/create")
    @Operation(summary = "创建库位移动")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin-move:create')")
    public CommonResult<Long> createStockBinMove(@Valid @RequestBody WmsStockBinMoveSaveReqVO createReqVO) {
        return success(stockBinMoveService.createStockBinMove(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新库位移动")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin-move:update')")
    public CommonResult<Boolean> updateStockBinMove(@Valid @RequestBody WmsStockBinMoveSaveReqVO updateReqVO) {
        stockBinMoveService.updateStockBinMove(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除库位移动")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:stock-bin-move:delete')")
    public CommonResult<Boolean> deleteStockBinMove(@RequestParam("id") Long id) {
        stockBinMoveService.deleteStockBinMove(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得库位移动")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin-move:query')")
    public CommonResult<WmsStockBinMoveRespVO> getStockBinMove(@RequestParam("id") Long id) {
        WmsStockBinMoveDO stockBinMove = stockBinMoveService.getStockBinMove(id);
        return success(BeanUtils.toBean(stockBinMove, WmsStockBinMoveRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得库位移动分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin-move:query')")
    public CommonResult<PageResult<WmsStockBinMoveRespVO>> getStockBinMovePage(@Valid WmsStockBinMovePageReqVO pageReqVO) {
        PageResult<WmsStockBinMoveDO> pageResult = stockBinMoveService.getStockBinMovePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WmsStockBinMoveRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出库位移动 Excel")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin-move:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStockBinMoveExcel(@Valid WmsStockBinMovePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsStockBinMoveDO> list = stockBinMoveService.getStockBinMovePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "库位移动.xls", "数据", WmsStockBinMoveRespVO.class,
                        BeanUtils.toBean(list, WmsStockBinMoveRespVO.class));
    }

}