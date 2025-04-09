package cn.iocoder.yudao.module.wms.controller.admin.inventory.bin;

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

import cn.iocoder.yudao.module.wms.controller.admin.inventory.bin.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.bin.WmsInventoryBinDO;
import cn.iocoder.yudao.module.wms.service.inventory.bin.WmsInventoryBinService;

@Tag(name = "管理后台 - 库位盘点")
@RestController
@RequestMapping("/wms/inventory-bin")
@Validated
public class WmsInventoryBinController {

    @Resource
    private WmsInventoryBinService inventoryBinService;

    @PostMapping("/create")
    @Operation(summary = "创建库位盘点")
    @PreAuthorize("@ss.hasPermission('wms:inventory-bin:create')")
    public CommonResult<Long> createInventoryBin(@Valid @RequestBody WmsInventoryBinSaveReqVO createReqVO) {
        return success(inventoryBinService.createInventoryBin(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新库位盘点")
    @PreAuthorize("@ss.hasPermission('wms:inventory-bin:update')")
    public CommonResult<Boolean> updateInventoryBin(@Valid @RequestBody WmsInventoryBinSaveReqVO updateReqVO) {
        inventoryBinService.updateInventoryBin(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除库位盘点")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:inventory-bin:delete')")
    public CommonResult<Boolean> deleteInventoryBin(@RequestParam("id") Long id) {
        inventoryBinService.deleteInventoryBin(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得库位盘点")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:inventory-bin:query')")
    public CommonResult<WmsInventoryBinRespVO> getInventoryBin(@RequestParam("id") Long id) {
        WmsInventoryBinDO inventoryBin = inventoryBinService.getInventoryBin(id);
        return success(BeanUtils.toBean(inventoryBin, WmsInventoryBinRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得库位盘点分页")
    @PreAuthorize("@ss.hasPermission('wms:inventory-bin:query')")
    public CommonResult<PageResult<WmsInventoryBinRespVO>> getInventoryBinPage(@Valid WmsInventoryBinPageReqVO pageReqVO) {
        PageResult<WmsInventoryBinDO> pageResult = inventoryBinService.getInventoryBinPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WmsInventoryBinRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出库位盘点 Excel")
    @PreAuthorize("@ss.hasPermission('wms:inventory-bin:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportInventoryBinExcel(@Valid WmsInventoryBinPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsInventoryBinDO> list = inventoryBinService.getInventoryBinPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "库位盘点.xls", "数据", WmsInventoryBinRespVO.class,
                        BeanUtils.toBean(list, WmsInventoryBinRespVO.class));
    }

}