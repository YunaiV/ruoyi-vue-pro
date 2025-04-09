package cn.iocoder.yudao.module.wms.controller.admin.inventory;

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

import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryDO;
import cn.iocoder.yudao.module.wms.service.inventory.WmsInventoryService;

@Tag(name = "管理后台 - 盘点")
@RestController
@RequestMapping("/wms/inventory")
@Validated
public class WmsInventoryController {

    @Resource
    private WmsInventoryService inventoryService;

    @PostMapping("/create")
    @Operation(summary = "创建盘点")
    @PreAuthorize("@ss.hasPermission('wms:inventory:create')")
    public CommonResult<Long> createInventory(@Valid @RequestBody WmsInventorySaveReqVO createReqVO) {
        return success(inventoryService.createInventory(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新盘点")
    @PreAuthorize("@ss.hasPermission('wms:inventory:update')")
    public CommonResult<Boolean> updateInventory(@Valid @RequestBody WmsInventorySaveReqVO updateReqVO) {
        inventoryService.updateInventory(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除盘点")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:inventory:delete')")
    public CommonResult<Boolean> deleteInventory(@RequestParam("id") Long id) {
        inventoryService.deleteInventory(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得盘点")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:inventory:query')")
    public CommonResult<WmsInventoryRespVO> getInventory(@RequestParam("id") Long id) {
        WmsInventoryDO inventory = inventoryService.getInventory(id);
        return success(BeanUtils.toBean(inventory, WmsInventoryRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得盘点分页")
    @PreAuthorize("@ss.hasPermission('wms:inventory:query')")
    public CommonResult<PageResult<WmsInventoryRespVO>> getInventoryPage(@Valid WmsInventoryPageReqVO pageReqVO) {
        PageResult<WmsInventoryDO> pageResult = inventoryService.getInventoryPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WmsInventoryRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出盘点 Excel")
    @PreAuthorize("@ss.hasPermission('wms:inventory:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportInventoryExcel(@Valid WmsInventoryPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsInventoryDO> list = inventoryService.getInventoryPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "盘点.xls", "数据", WmsInventoryRespVO.class,
                        BeanUtils.toBean(list, WmsInventoryRespVO.class));
    }

}