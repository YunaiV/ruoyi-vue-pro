package cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.item;

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

import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.item.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.item.WmsStockBinMoveItemDO;
import cn.iocoder.yudao.module.wms.service.stock.bin.move.item.WmsStockBinMoveItemService;

@Tag(name = "管理后台 - 库位移动详情")
@RestController
@RequestMapping("/wms/stock-bin-move-item")
@Validated
public class WmsStockBinMoveItemController {

    @Resource
    private WmsStockBinMoveItemService stockBinMoveItemService;

    @PostMapping("/create")
    @Operation(summary = "创建库位移动详情")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin-move-item:create')")
    public CommonResult<Long> createStockBinMoveItem(@Valid @RequestBody WmsStockBinMoveItemSaveReqVO createReqVO) {
        return success(stockBinMoveItemService.createStockBinMoveItem(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新库位移动详情")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin-move-item:update')")
    public CommonResult<Boolean> updateStockBinMoveItem(@Valid @RequestBody WmsStockBinMoveItemSaveReqVO updateReqVO) {
        stockBinMoveItemService.updateStockBinMoveItem(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除库位移动详情")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:stock-bin-move-item:delete')")
    public CommonResult<Boolean> deleteStockBinMoveItem(@RequestParam("id") Long id) {
        stockBinMoveItemService.deleteStockBinMoveItem(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得库位移动详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin-move-item:query')")
    public CommonResult<WmsStockBinMoveItemRespVO> getStockBinMoveItem(@RequestParam("id") Long id) {
        WmsStockBinMoveItemDO stockBinMoveItem = stockBinMoveItemService.getStockBinMoveItem(id);
        return success(BeanUtils.toBean(stockBinMoveItem, WmsStockBinMoveItemRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得库位移动详情分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin-move-item:query')")
    public CommonResult<PageResult<WmsStockBinMoveItemRespVO>> getStockBinMoveItemPage(@Valid WmsStockBinMoveItemPageReqVO pageReqVO) {
        PageResult<WmsStockBinMoveItemDO> pageResult = stockBinMoveItemService.getStockBinMoveItemPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WmsStockBinMoveItemRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出库位移动详情 Excel")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin-move-item:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStockBinMoveItemExcel(@Valid WmsStockBinMoveItemPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsStockBinMoveItemDO> list = stockBinMoveItemService.getStockBinMoveItemPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "库位移动详情.xls", "数据", WmsStockBinMoveItemRespVO.class,
                        BeanUtils.toBean(list, WmsStockBinMoveItemRespVO.class));
    }

}