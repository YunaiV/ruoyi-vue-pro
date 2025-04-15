package cn.iocoder.yudao.module.wms.controller.admin.stock.bin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespBinVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.module.wms.service.stock.bin.WmsStockBinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "仓位库存")
@RestController
@RequestMapping("/wms/stock-bin")
@Validated
public class WmsStockBinController {

    @Resource
    private WmsStockBinService stockBinService;

    // /**
    // * @sign : 7472CDCA246B810A
    // */
    // @PostMapping("/create")
    // @Operation(summary = "创建仓位库存")
    // @PreAuthorize("@ss.hasPermission('wms:stock-bin:create')")
    // public CommonResult<Long> createStockBin(@Valid @RequestBody WmsStockBinSaveReqVO createReqVO) {
    // return success(stockBinService.createStockBin(createReqVO).getId());
    // }
    // /**
    // * @sign : AC84893CE186DA40
    // */
    // @PutMapping("/update")
    // @Operation(summary = "更新仓位库存")
    // @PreAuthorize("@ss.hasPermission('wms:stock-bin:update')")
    // public CommonResult<Boolean> updateStockBin(@Valid @RequestBody WmsStockBinSaveReqVO updateReqVO) {
    // stockBinService.updateStockBin(updateReqVO);
    // return success(true);
    // }
    // @DeleteMapping("/delete")
    // @Operation(summary = "删除仓位库存")
    // @Parameter(name = "id", description = "编号", required = true)
    // @PreAuthorize("@ss.hasPermission('wms:stock-bin:delete')")
    // public CommonResult<Boolean> deleteStockBin(@RequestParam("id") Long id) {
    // stockBinService.deleteStockBin(id);
    // return success(true);
    // }
    /**
     * @sign : D7B68E7D4D845527
     */
    @GetMapping("/stocks")
    @Operation(summary = "获得产品的仓位库存")
    @Parameter(name = "warehouseId", description = "仓库ID", required = true, example = "1024")
    @Parameter(name = "productId", description = "产品ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin:query')")
    public CommonResult<List<WmsStockBinRespVO>> getStockBin(@RequestParam("warehouseId") Long warehouseId,@RequestParam("binId") Long binId, @RequestParam("productId") Long productId) {
        // 查询数据
        List<WmsStockBinDO> stockBinList = stockBinService.selectStockBin(warehouseId,binId, productId);
        // 转换
        List<WmsStockBinRespVO> stockBinVO = BeanUtils.toBean(stockBinList, WmsStockBinRespVO.class);
        // 返回
        return success(stockBinVO);
    }

    /**
     * @sign : 33164D4484F99F37
     */
    @PostMapping("/page")
    @Operation(summary = "获得仓位库存分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin:query')")
    public CommonResult<PageResult<WmsStockBinRespVO>> getStockBinPage(@Valid @RequestBody WmsStockBinPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsStockBinDO> doPageResult = stockBinService.getStockBinPage(pageReqVO);

        // 转换
        PageResult<WmsStockBinRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsStockBinRespVO.class);
        stockBinService.assembleProducts(voPageResult.getList());
        stockBinService.assembleWarehouse(voPageResult.getList());
        stockBinService.assembleBin(voPageResult.getList());
        // 返回
        return success(voPageResult);
    }

    @PostMapping("/grouped-page")
    @Operation(summary = "获得按产品分组的仓位库存分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin:query')")
    public CommonResult<PageResult<WmsProductRespBinVO>> getGroupedStockBinPage(@Valid @RequestBody WmsStockBinPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsProductRespBinVO> voPageResult = stockBinService.getGroupedStockBinPage(pageReqVO);

        stockBinService.assembleDept(voPageResult.getList());
        stockBinService.assembleStockWarehouseList(pageReqVO.getWarehouseId(),voPageResult.getList());
        // 返回
        return success(voPageResult);
    }


    // @GetMapping("/export-excel")
    // @Operation(summary = "导出仓位库存 Excel")
    // @PreAuthorize("@ss.hasPermission('wms:stock-bin:export')")
    // @ApiAccessLog(operateType = EXPORT)
    // public void exportStockBinExcel(@Valid WmsStockBinPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
    // pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
    // List<WmsStockBinDO> list = stockBinService.getStockBinPage(pageReqVO).getList();
    // // 导出 Excel
    // ExcelUtils.write(response, "仓位库存.xls", "数据", WmsStockBinRespVO.class, BeanUtils.toBean(list, WmsStockBinRespVO.class));
    // }
}

