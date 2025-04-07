package cn.iocoder.yudao.module.wms.controller.admin.inventory.result.item;

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

import cn.iocoder.yudao.module.wms.controller.admin.inventory.result.item.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.result.item.WmsInventoryResultItemDO;
import cn.iocoder.yudao.module.wms.service.inventory.result.item.WmsInventoryResultItemService;

@Tag(name = "管理后台 - 库存盘点结果详情")
@RestController
@RequestMapping("/wms/inventory-result-item")
@Validated
public class WmsInventoryResultItemController {

    @Resource
    private WmsInventoryResultItemService inventoryResultItemService;

    @PostMapping("/create")
    @Operation(summary = "创建库存盘点结果详情")
    @PreAuthorize("@ss.hasPermission('wms:inventory-result-item:create')")
    public CommonResult<Long> createInventoryResultItem(@Valid @RequestBody WmsInventoryResultItemSaveReqVO createReqVO) {
        return success(inventoryResultItemService.createInventoryResultItem(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新库存盘点结果详情")
    @PreAuthorize("@ss.hasPermission('wms:inventory-result-item:update')")
    public CommonResult<Boolean> updateInventoryResultItem(@Valid @RequestBody WmsInventoryResultItemSaveReqVO updateReqVO) {
        inventoryResultItemService.updateInventoryResultItem(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除库存盘点结果详情")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:inventory-result-item:delete')")
    public CommonResult<Boolean> deleteInventoryResultItem(@RequestParam("id") Long id) {
        inventoryResultItemService.deleteInventoryResultItem(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得库存盘点结果详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:inventory-result-item:query')")
    public CommonResult<WmsInventoryResultItemRespVO> getInventoryResultItem(@RequestParam("id") Long id) {
        WmsInventoryResultItemDO inventoryResultItem = inventoryResultItemService.getInventoryResultItem(id);
        return success(BeanUtils.toBean(inventoryResultItem, WmsInventoryResultItemRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得库存盘点结果详情分页")
    @PreAuthorize("@ss.hasPermission('wms:inventory-result-item:query')")
    public CommonResult<PageResult<WmsInventoryResultItemRespVO>> getInventoryResultItemPage(@Valid WmsInventoryResultItemPageReqVO pageReqVO) {
        PageResult<WmsInventoryResultItemDO> pageResult = inventoryResultItemService.getInventoryResultItemPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WmsInventoryResultItemRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出库存盘点结果详情 Excel")
    @PreAuthorize("@ss.hasPermission('wms:inventory-result-item:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportInventoryResultItemExcel(@Valid WmsInventoryResultItemPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsInventoryResultItemDO> list = inventoryResultItemService.getInventoryResultItemPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "库存盘点结果详情.xls", "数据", WmsInventoryResultItemRespVO.class,
                        BeanUtils.toBean(list, WmsInventoryResultItemRespVO.class));
    }

}