package cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.item;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.item.vo.WmsStockBinMoveItemPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.item.vo.WmsStockBinMoveItemRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.item.WmsStockBinMoveItemDO;
import cn.iocoder.yudao.module.wms.service.stock.bin.move.item.WmsStockBinMoveItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "库位移动详情")
@RestController
@RequestMapping("/wms/stock-bin-move-item")
@Validated
public class WmsStockBinMoveItemController {

    @Resource
    private WmsStockBinMoveItemService stockBinMoveItemService;

    // /**
    // * @sign : 2622F8D52EFBA3F8
    // */
    // @PostMapping("/create")
    // @Operation(summary = "创建库位移动详情")
    // @PreAuthorize("@ss.hasPermission('wms:stock-bin-move-item:create')")
    // public CommonResult<Long> createStockBinMoveItem(@Valid @RequestBody WmsStockBinMoveItemSaveReqVO createReqVO) {
    // return success(stockBinMoveItemService.createStockBinMoveItem(createReqVO).getId());
    // }
    // 
    // /**
    // * @sign : A722CBFCD75D85D5
    // */
    // @PutMapping("/update")
    // @Operation(summary = "更新库位移动详情")
    // @PreAuthorize("@ss.hasPermission('wms:stock-bin-move-item:update')")
    // public CommonResult<Boolean> updateStockBinMoveItem(@Valid @RequestBody WmsStockBinMoveItemSaveReqVO updateReqVO) {
    // stockBinMoveItemService.updateStockBinMoveItem(updateReqVO);
    // return success(true);
    // }
    // 
    // @DeleteMapping("/delete")
    // @Operation(summary = "删除库位移动详情")
    // @Parameter(name = "id", description = "编号", required = true)
    // @PreAuthorize("@ss.hasPermission('wms:stock-bin-move-item:delete')")
    // public CommonResult<Boolean> deleteStockBinMoveItem(@RequestParam("id") Long id) {
    // stockBinMoveItemService.deleteStockBinMoveItem(id);
    // return success(true);
    // }
    // 
    // /**
    // * @sign : DE7A72914F0F75C8
    // */
    // @GetMapping("/get")
    // @Operation(summary = "获得库位移动详情")
    // @Parameter(name = "id", description = "编号", required = true, example = "1024")
    // @PreAuthorize("@ss.hasPermission('wms:stock-bin-move-item:query')")
    // public CommonResult<WmsStockBinMoveItemRespVO> getStockBinMoveItem(@RequestParam("id") Long id) {
    // // 查询数据
    // WmsStockBinMoveItemDO stockBinMoveItem = stockBinMoveItemService.getStockBinMoveItem(id);
    // if (stockBinMoveItem == null) {
    // throw exception(STOCK_BIN_MOVE_ITEM_NOT_EXISTS);
    // }
    // // 转换
    // WmsStockBinMoveItemRespVO stockBinMoveItemVO = BeanUtils.toBean(stockBinMoveItem, WmsStockBinMoveItemRespVO.class);
    // // 返回
    // return success(stockBinMoveItemVO);
    // }
    /**
     * @sign : DECD508565F769F8
     */
    @GetMapping("/page")
    @Operation(summary = "获得库位移动详情分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin-move-item:query')")
    public CommonResult<PageResult<WmsStockBinMoveItemRespVO>> getStockBinMoveItemPage(@Valid WmsStockBinMoveItemPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsStockBinMoveItemDO> doPageResult = stockBinMoveItemService.getStockBinMoveItemPage(pageReqVO);
        // 转换
        PageResult<WmsStockBinMoveItemRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsStockBinMoveItemRespVO.class);
        // 关联
        stockBinMoveItemService.assembleBin(voPageResult.getList());
        stockBinMoveItemService.assembleProduct(voPageResult.getList());
        stockBinMoveItemService.assembleBinMove(voPageResult.getList());
        // 返回
        return success(voPageResult);
    }
    // @GetMapping("/export-excel")
    // @Operation(summary = "导出库位移动详情 Excel")
    // @PreAuthorize("@ss.hasPermission('wms:stock-bin-move-item:export')")
    // @ApiAccessLog(operateType = EXPORT)
    // public void exportStockBinMoveItemExcel(@Valid WmsStockBinMoveItemPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
    // pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
    // List<WmsStockBinMoveItemDO> list = stockBinMoveItemService.getStockBinMoveItemPage(pageReqVO).getList();
    // // 导出 Excel
    // ExcelUtils.write(response, "库位移动详情.xls", "数据", WmsStockBinMoveItemRespVO.class, BeanUtils.toBean(list, WmsStockBinMoveItemRespVO.class));
    // }
}
