package cn.iocoder.yudao.module.wms.controller.admin.inventory.product;

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

import cn.iocoder.yudao.module.wms.controller.admin.inventory.product.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.product.WmsInventoryProductDO;
import cn.iocoder.yudao.module.wms.service.inventory.product.WmsInventoryProductService;

@Tag(name = "管理后台 - 库存盘点产品")
@RestController
@RequestMapping("/wms/inventory-product")
@Validated
public class WmsInventoryProductController {

    @Resource
    private WmsInventoryProductService inventoryProductService;

    @PostMapping("/create")
    @Operation(summary = "创建库存盘点产品")
    @PreAuthorize("@ss.hasPermission('wms:inventory-product:create')")
    public CommonResult<Long> createInventoryProduct(@Valid @RequestBody WmsInventoryProductSaveReqVO createReqVO) {
        return success(inventoryProductService.createInventoryProduct(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新库存盘点产品")
    @PreAuthorize("@ss.hasPermission('wms:inventory-product:update')")
    public CommonResult<Boolean> updateInventoryProduct(@Valid @RequestBody WmsInventoryProductSaveReqVO updateReqVO) {
        inventoryProductService.updateInventoryProduct(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除库存盘点产品")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:inventory-product:delete')")
    public CommonResult<Boolean> deleteInventoryProduct(@RequestParam("id") Long id) {
        inventoryProductService.deleteInventoryProduct(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得库存盘点产品")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:inventory-product:query')")
    public CommonResult<WmsInventoryProductRespVO> getInventoryProduct(@RequestParam("id") Long id) {
        WmsInventoryProductDO inventoryProduct = inventoryProductService.getInventoryProduct(id);
        return success(BeanUtils.toBean(inventoryProduct, WmsInventoryProductRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得库存盘点产品分页")
    @PreAuthorize("@ss.hasPermission('wms:inventory-product:query')")
    public CommonResult<PageResult<WmsInventoryProductRespVO>> getInventoryProductPage(@Valid WmsInventoryProductPageReqVO pageReqVO) {
        PageResult<WmsInventoryProductDO> pageResult = inventoryProductService.getInventoryProductPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WmsInventoryProductRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出库存盘点产品 Excel")
    @PreAuthorize("@ss.hasPermission('wms:inventory-product:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportInventoryProductExcel(@Valid WmsInventoryProductPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsInventoryProductDO> list = inventoryProductService.getInventoryProductPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "库存盘点产品.xls", "数据", WmsInventoryProductRespVO.class,
                        BeanUtils.toBean(list, WmsInventoryProductRespVO.class));
    }

}