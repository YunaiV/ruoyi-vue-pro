package cn.iocoder.yudao.module.wms.controller.admin.inventory.bin;

import cn.iocoder.yudao.module.wms.service.inventory.bin.WmsInventoryBinService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "库位盘点")
@RestController
@RequestMapping("/wms/inventory-bin")
@Validated
public class WmsInventoryBinController {

    @Resource
    private WmsInventoryBinService inventoryBinService;

//    /**
//     * @sign : ACAABD7462DAD982
//     */
//    @PostMapping("/create")
//    @Operation(summary = "创建库位盘点")
//    @PreAuthorize("@ss.hasPermission('wms:inventory-bin:create')")
//    public CommonResult<Long> createInventoryBin(@Valid @RequestBody WmsInventoryBinSaveReqVO createReqVO) {
//        return success(inventoryBinService.createInventoryBin(createReqVO).getId());
//    }
//
//    /**
//     * @sign : 75B7A098990ADFC4
//     */
//    @PutMapping("/update")
//    @Operation(summary = "更新库位盘点")
//    @PreAuthorize("@ss.hasPermission('wms:inventory-bin:update')")
//    public CommonResult<Boolean> updateInventoryBin(@Valid @RequestBody WmsInventoryBinSaveReqVO updateReqVO) {
//        inventoryBinService.updateInventoryBin(updateReqVO);
//        return success(true);
//    }
//
//    @DeleteMapping("/delete")
//    @Operation(summary = "删除库位盘点")
//    @Parameter(name = "id", description = "编号", required = true)
//    @PreAuthorize("@ss.hasPermission('wms:inventory-bin:delete')")
//    public CommonResult<Boolean> deleteInventoryBin(@RequestParam("id") Long id) {
//        inventoryBinService.deleteInventoryBin(id);
//        return success(true);
//    }
//
//    /**
//     * @sign : 48F08E170F254296
//     */
//    @GetMapping("/get")
//    @Operation(summary = "获得库位盘点")
//    @Parameter(name = "id", description = "编号", required = true, example = "1024")
//    @PreAuthorize("@ss.hasPermission('wms:inventory-bin:query')")
//    public CommonResult<WmsInventoryBinRespVO> getInventoryBin(@RequestParam("id") Long id) {
//        // 查询数据
//        WmsInventoryBinDO inventoryBin = inventoryBinService.getInventoryBin(id);
//        if (inventoryBin == null) {
//            throw exception(INVENTORY_BIN_NOT_EXISTS);
//        }
//        // 转换
//        WmsInventoryBinRespVO inventoryBinVO = BeanUtils.toBean(inventoryBin, WmsInventoryBinRespVO.class);
//        // 返回
//        return success(inventoryBinVO);
//    }
//
//    /**
//     * @sign : E02F374AE9A85D01
//     */
//    @GetMapping("/page")
//    @Operation(summary = "获得库位盘点分页")
//    @PreAuthorize("@ss.hasPermission('wms:inventory-bin:query')")
//    public CommonResult<PageResult<WmsInventoryBinRespVO>> getInventoryBinPage(@Valid WmsInventoryBinPageReqVO pageReqVO) {
//        // 查询数据
//        PageResult<WmsInventoryBinDO> doPageResult = inventoryBinService.getInventoryBinPage(pageReqVO);
//        // 转换
//        PageResult<WmsInventoryBinRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsInventoryBinRespVO.class);
//        // 返回
//        return success(voPageResult);
//    }
//
//    @GetMapping("/export-excel")
//    @Operation(summary = "导出库位盘点 Excel")
//    @PreAuthorize("@ss.hasPermission('wms:inventory-bin:export')")
//    @ApiAccessLog(operateType = EXPORT)
//    public void exportInventoryBinExcel(@Valid WmsInventoryBinPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
//        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
//        List<WmsInventoryBinDO> list = inventoryBinService.getInventoryBinPage(pageReqVO).getList();
//        // 导出 Excel
//        ExcelUtils.write(response, "库位盘点.xls", "数据", WmsInventoryBinRespVO.class, BeanUtils.toBean(list, WmsInventoryBinRespVO.class));
//    }
}
