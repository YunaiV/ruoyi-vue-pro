package cn.iocoder.yudao.module.wms.controller.admin.inventory.product;

import cn.iocoder.yudao.module.wms.service.inventory.product.WmsInventoryProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "库存盘点产品")
@RestController
@RequestMapping("/wms/inventory-product")
@Validated
public class WmsInventoryProductController {

    @Resource
    private WmsInventoryProductService inventoryProductService;
    // /**
    // * @sign : 3A7070C1BA316C44
    // */
    // @PostMapping("/create")
    // @Operation(summary = "创建库存盘点产品")
    // @PreAuthorize("@ss.hasPermission('wms:inventory-product:create')")
    // public CommonResult<Long> createInventoryProduct(@Valid @RequestBody WmsInventoryProductSaveReqVO createReqVO) {
    // return success(inventoryProductService.createInventoryProduct(createReqVO).getId());
    // }
    // /**
    // * @sign : 3B01C0D4793FF214
    // */
    // @PutMapping("/update")
    // @Operation(summary = "更新库存盘点产品")
    // @PreAuthorize("@ss.hasPermission('wms:inventory-product:update')")
    // public CommonResult<Boolean> updateInventoryProduct(@Valid @RequestBody WmsInventoryProductSaveReqVO updateReqVO) {
    // inventoryProductService.updateInventoryProduct(updateReqVO);
    // return success(true);
    // }
    // @DeleteMapping("/delete")
    // @Operation(summary = "删除库存盘点产品")
    // @Parameter(name = "id", description = "编号", required = true)
    // @PreAuthorize("@ss.hasPermission('wms:inventory-product:delete')")
    // public CommonResult<Boolean> deleteInventoryProduct(@RequestParam("id") Long id) {
    // inventoryProductService.deleteInventoryProduct(id);
    // return success(true);
    // }
    // /**
    // * @sign : 6DD98F53540B2924
    // */
    // @GetMapping("/get")
    // @Operation(summary = "获得库存盘点产品")
    // @Parameter(name = "id", description = "编号", required = true, example = "1024")
    // @PreAuthorize("@ss.hasPermission('wms:inventory-product:query')")
    // public CommonResult<WmsInventoryProductRespVO> getInventoryProduct(@RequestParam("id") Long id) {
    // // 查询数据
    // WmsInventoryProductDO inventoryProduct = inventoryProductService.getInventoryProduct(id);
    // if (inventoryProduct == null) {
    // throw exception(INVENTORY_PRODUCT_NOT_EXISTS);
    // }
    // // 转换
    // WmsInventoryProductRespVO inventoryProductVO = BeanUtils.toBean(inventoryProduct, WmsInventoryProductRespVO.class);
    // // 返回
    // return success(inventoryProductVO);
    // }
    // /**
    // * @sign : EBB334F4564EC4BD
    // */
    // @GetMapping("/page")
    // @Operation(summary = "获得库存盘点产品分页")
    // @PreAuthorize("@ss.hasPermission('wms:inventory-product:query')")
    // public CommonResult<PageResult<WmsInventoryProductRespVO>> getInventoryProductPage(@Valid WmsInventoryProductPageReqVO pageReqVO) {
    // // 查询数据
    // PageResult<WmsInventoryProductDO> doPageResult = inventoryProductService.getInventoryProductPage(pageReqVO);
    // // 转换
    // PageResult<WmsInventoryProductRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsInventoryProductRespVO.class);
    // // 返回
    // return success(voPageResult);
    // }
    // 
    // @GetMapping("/export-excel")
    // @Operation(summary = "导出库存盘点产品 Excel")
    // @PreAuthorize("@ss.hasPermission('wms:inventory-product:export')")
    // @ApiAccessLog(operateType = EXPORT)
    // public void exportInventoryProductExcel(@Valid WmsInventoryProductPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
    // pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
    // List<WmsInventoryProductDO> list = inventoryProductService.getInventoryProductPage(pageReqVO).getList();
    // // 导出 Excel
    // ExcelUtils.write(response, "库存盘点产品.xls", "数据", WmsInventoryProductRespVO.class, BeanUtils.toBean(list, WmsInventoryProductRespVO.class));
    // }
}