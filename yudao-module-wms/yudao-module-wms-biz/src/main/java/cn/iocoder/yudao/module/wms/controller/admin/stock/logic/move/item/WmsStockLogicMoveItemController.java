package cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.item;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.item.vo.WmsStockLogicMoveItemPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.item.vo.WmsStockLogicMoveItemRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.move.item.WmsStockLogicMoveItemDO;
import cn.iocoder.yudao.module.wms.service.stock.logic.move.item.WmsStockLogicMoveItemService;
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

/**
 * @author jisencai
 */
@Tag(name = "逻辑库存移动详情")
@RestController
@RequestMapping("/wms/stock-logic-move-item")
@Validated
public class WmsStockLogicMoveItemController {

    @Resource
    private WmsStockLogicMoveItemService stockLogicMoveItemService;

    // /**
    // * @sign : 56C81C5C06921A3C
    // */
    // @PostMapping("/create")
    // @Operation(summary = "创建逻辑库存移动详情")
    // @PreAuthorize("@ss.hasPermission('wms:stock-logic-move-item:create')")
    // public CommonResult<Long> createStockLogicMoveItem(@Valid @RequestBody WmsStockLogicMoveItemSaveReqVO createReqVO) {
    // return success(stockLogicMoveItemService.createStockLogicMoveItem(createReqVO).getId());
    // }
    // 
    // /**
    // * @sign : EEEF0EC4B914FBE9
    // */
    // @PutMapping("/update")
    // @Operation(summary = "更新逻辑库存移动详情")
    // @PreAuthorize("@ss.hasPermission('wms:stock-logic-move-item:update')")
    // public CommonResult<Boolean> updateStockLogicMoveItem(@Valid @RequestBody WmsStockLogicMoveItemSaveReqVO updateReqVO) {
    // stockLogicMoveItemService.updateStockLogicMoveItem(updateReqVO);
    // return success(true);
    // }
    // 
    // @DeleteMapping("/delete")
    // @Operation(summary = "删除逻辑库存移动详情")
    // @Parameter(name = "id", description = "编号", required = true)
    // @PreAuthorize("@ss.hasPermission('wms:stock-logic-move-item:delete')")
    // public CommonResult<Boolean> deleteStockLogicMoveItem(@RequestParam("id") Long id) {
    // stockLogicMoveItemService.deleteStockLogicMoveItem(id);
    // return success(true);
    // }
    // 
    // /**
    // * @sign : 0893F459C772A04E
    // */
    // @GetMapping("/get")
    // @Operation(summary = "获得逻辑库存移动详情")
    // @Parameter(name = "id", description = "编号", required = true, example = "1024")
    // @PreAuthorize("@ss.hasPermission('wms:stock-logic-move-item:query')")
    // public CommonResult<WmsStockLogicMoveItemRespVO> getStockLogicMoveItem(@RequestParam("id") Long id) {
    // // 查询数据
    // WmsStockLogicMoveItemDO stockLogicMoveItem = stockLogicMoveItemService.getStockLogicMoveItem(id);
    // if (stockLogicMoveItem == null) {
    // throw exception(STOCK_LOGIC_MOVE_ITEM_NOT_EXISTS);
    // }
    // // 转换
    // WmsStockLogicMoveItemRespVO stockLogicMoveItemVO = BeanUtils.toBean(stockLogicMoveItem, WmsStockLogicMoveItemRespVO.class);
    // // 返回
    // return success(stockLogicMoveItemVO);
    // }

    /**
     * @sign : CF2179E1049D4C17
     */
    @GetMapping("/page")
    @Operation(summary = "获得逻辑库存移动详情分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-logic-move-item:query')")
    public CommonResult<PageResult<WmsStockLogicMoveItemRespVO>> getStockLogicMoveItemPage(@Valid WmsStockLogicMoveItemPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsStockLogicMoveItemDO> doPageResult = stockLogicMoveItemService.getStockLogicMoveItemPage(pageReqVO);
        // 转换
        PageResult<WmsStockLogicMoveItemRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsStockLogicMoveItemRespVO.class);
        // 装配
        stockLogicMoveItemService.assembleProduct(voPageResult.getList());
        stockLogicMoveItemService.assembleCompanyAndDept(voPageResult.getList());
        // 返回
        return success(voPageResult);
    }
    // @GetMapping("/export-excel")
    // @Operation(summary = "导出逻辑库存移动详情 Excel")
    // @PreAuthorize("@ss.hasPermission('wms:stock-logic-move-item:export')")
    // @ApiAccessLog(operateType = EXPORT)
    // public void exportStockLogicMoveItemExcel(@Valid WmsStockLogicMoveItemPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
    // pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
    // List<WmsStockLogicMoveItemDO> list = stockLogicMoveItemService.getStockLogicMoveItemPage(pageReqVO).getList();
    // // 导出 Excel
    // ExcelUtils.write(response, "逻辑库存移动详情.xls", "数据", WmsStockLogicMoveItemRespVO.class, BeanUtils.toBean(list, WmsStockLogicMoveItemRespVO.class));
    // }
}
