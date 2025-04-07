package cn.iocoder.yudao.module.wms.controller.admin.stock.ownershiop.move.item;

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

import cn.iocoder.yudao.module.wms.controller.admin.stock.ownershiop.move.item.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownershiop.move.item.WmsStockOwnershiopMoveItemDO;
import cn.iocoder.yudao.module.wms.service.stock.ownershiop.move.item.WmsStockOwnershiopMoveItemService;

@Tag(name = "管理后台 - 所有者库存移动详情")
@RestController
@RequestMapping("/wms/stock-ownershiop-move-item")
@Validated
public class WmsStockOwnershiopMoveItemController {

    @Resource
    private WmsStockOwnershiopMoveItemService stockOwnershiopMoveItemService;

    @PostMapping("/create")
    @Operation(summary = "创建所有者库存移动详情")
    @PreAuthorize("@ss.hasPermission('wms:stock-ownershiop-move-item:create')")
    public CommonResult<Long> createStockOwnershiopMoveItem(@Valid @RequestBody WmsStockOwnershiopMoveItemSaveReqVO createReqVO) {
        return success(stockOwnershiopMoveItemService.createStockOwnershiopMoveItem(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新所有者库存移动详情")
    @PreAuthorize("@ss.hasPermission('wms:stock-ownershiop-move-item:update')")
    public CommonResult<Boolean> updateStockOwnershiopMoveItem(@Valid @RequestBody WmsStockOwnershiopMoveItemSaveReqVO updateReqVO) {
        stockOwnershiopMoveItemService.updateStockOwnershiopMoveItem(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除所有者库存移动详情")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:stock-ownershiop-move-item:delete')")
    public CommonResult<Boolean> deleteStockOwnershiopMoveItem(@RequestParam("id") Long id) {
        stockOwnershiopMoveItemService.deleteStockOwnershiopMoveItem(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得所有者库存移动详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:stock-ownershiop-move-item:query')")
    public CommonResult<WmsStockOwnershiopMoveItemRespVO> getStockOwnershiopMoveItem(@RequestParam("id") Long id) {
        WmsStockOwnershiopMoveItemDO stockOwnershiopMoveItem = stockOwnershiopMoveItemService.getStockOwnershiopMoveItem(id);
        return success(BeanUtils.toBean(stockOwnershiopMoveItem, WmsStockOwnershiopMoveItemRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得所有者库存移动详情分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-ownershiop-move-item:query')")
    public CommonResult<PageResult<WmsStockOwnershiopMoveItemRespVO>> getStockOwnershiopMoveItemPage(@Valid WmsStockOwnershiopMoveItemPageReqVO pageReqVO) {
        PageResult<WmsStockOwnershiopMoveItemDO> pageResult = stockOwnershiopMoveItemService.getStockOwnershiopMoveItemPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WmsStockOwnershiopMoveItemRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出所有者库存移动详情 Excel")
    @PreAuthorize("@ss.hasPermission('wms:stock-ownershiop-move-item:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStockOwnershiopMoveItemExcel(@Valid WmsStockOwnershiopMoveItemPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsStockOwnershiopMoveItemDO> list = stockOwnershiopMoveItemService.getStockOwnershiopMoveItemPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "所有者库存移动详情.xls", "数据", WmsStockOwnershiopMoveItemRespVO.class,
                        BeanUtils.toBean(list, WmsStockOwnershiopMoveItemRespVO.class));
    }

}