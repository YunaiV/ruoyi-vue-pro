package cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.item;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.item.vo.WmsStockOwnershipMoveItemPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.item.vo.WmsStockOwnershipMoveItemRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.move.item.WmsStockOwnershipMoveItemDO;
import cn.iocoder.yudao.module.wms.service.stock.ownership.move.item.WmsStockOwnershipMoveItemService;
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

@Tag(name = "所有者库存移动详情")
@RestController
@RequestMapping("/wms/stock-ownership-move-item")
@Validated
public class WmsStockOwnershipMoveItemController {

    @Resource
    private WmsStockOwnershipMoveItemService stockOwnershipMoveItemService;

//    /**
//     * @sign : 56C81C5C06921A3C
//     */
//    @PostMapping("/create")
//    @Operation(summary = "创建所有者库存移动详情")
//    @PreAuthorize("@ss.hasPermission('wms:stock-ownership-move-item:create')")
//    public CommonResult<Long> createStockOwnershipMoveItem(@Valid @RequestBody WmsStockOwnershipMoveItemSaveReqVO createReqVO) {
//        return success(stockOwnershipMoveItemService.createStockOwnershipMoveItem(createReqVO).getId());
//    }
//
//    /**
//     * @sign : EEEF0EC4B914FBE9
//     */
//    @PutMapping("/update")
//    @Operation(summary = "更新所有者库存移动详情")
//    @PreAuthorize("@ss.hasPermission('wms:stock-ownership-move-item:update')")
//    public CommonResult<Boolean> updateStockOwnershipMoveItem(@Valid @RequestBody WmsStockOwnershipMoveItemSaveReqVO updateReqVO) {
//        stockOwnershipMoveItemService.updateStockOwnershipMoveItem(updateReqVO);
//        return success(true);
//    }
//
//    @DeleteMapping("/delete")
//    @Operation(summary = "删除所有者库存移动详情")
//    @Parameter(name = "id", description = "编号", required = true)
//    @PreAuthorize("@ss.hasPermission('wms:stock-ownership-move-item:delete')")
//    public CommonResult<Boolean> deleteStockOwnershipMoveItem(@RequestParam("id") Long id) {
//        stockOwnershipMoveItemService.deleteStockOwnershipMoveItem(id);
//        return success(true);
//    }
//
//    /**
//     * @sign : 0893F459C772A04E
//     */
//    @GetMapping("/get")
//    @Operation(summary = "获得所有者库存移动详情")
//    @Parameter(name = "id", description = "编号", required = true, example = "1024")
//    @PreAuthorize("@ss.hasPermission('wms:stock-ownership-move-item:query')")
//    public CommonResult<WmsStockOwnershipMoveItemRespVO> getStockOwnershipMoveItem(@RequestParam("id") Long id) {
//        // 查询数据
//        WmsStockOwnershipMoveItemDO stockOwnershipMoveItem = stockOwnershipMoveItemService.getStockOwnershipMoveItem(id);
//        if (stockOwnershipMoveItem == null) {
//            throw exception(STOCK_OWNERSHIP_MOVE_ITEM_NOT_EXISTS);
//        }
//        // 转换
//        WmsStockOwnershipMoveItemRespVO stockOwnershipMoveItemVO = BeanUtils.toBean(stockOwnershipMoveItem, WmsStockOwnershipMoveItemRespVO.class);
//        // 返回
//        return success(stockOwnershipMoveItemVO);
//    }

    /**
     * @sign : CF2179E1049D4C17
     */
    @GetMapping("/page")
    @Operation(summary = "获得所有者库存移动详情分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-ownership-move-item:query')")
    public CommonResult<PageResult<WmsStockOwnershipMoveItemRespVO>> getStockOwnershipMoveItemPage(@Valid WmsStockOwnershipMoveItemPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsStockOwnershipMoveItemDO> doPageResult = stockOwnershipMoveItemService.getStockOwnershipMoveItemPage(pageReqVO);
        // 转换
        PageResult<WmsStockOwnershipMoveItemRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsStockOwnershipMoveItemRespVO.class);
        // 装配
        stockOwnershipMoveItemService.assembleProduct(voPageResult.getList());
        stockOwnershipMoveItemService.assembleCompanyAndDept(voPageResult.getList());
        // 返回
        return success(voPageResult);
    }

//    @GetMapping("/export-excel")
//    @Operation(summary = "导出所有者库存移动详情 Excel")
//    @PreAuthorize("@ss.hasPermission('wms:stock-ownership-move-item:export')")
//    @ApiAccessLog(operateType = EXPORT)
//    public void exportStockOwnershipMoveItemExcel(@Valid WmsStockOwnershipMoveItemPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
//        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
//        List<WmsStockOwnershipMoveItemDO> list = stockOwnershipMoveItemService.getStockOwnershipMoveItemPage(pageReqVO).getList();
//        // 导出 Excel
//        ExcelUtils.write(response, "所有者库存移动详情.xls", "数据", WmsStockOwnershipMoveItemRespVO.class, BeanUtils.toBean(list, WmsStockOwnershipMoveItemRespVO.class));
//    }
}
