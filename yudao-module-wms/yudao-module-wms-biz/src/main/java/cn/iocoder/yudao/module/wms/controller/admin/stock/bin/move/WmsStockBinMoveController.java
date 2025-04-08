package cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.item.vo.WmsStockBinMoveItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo.WmsStockBinMovePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo.WmsStockBinMoveRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo.WmsStockBinMoveSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.WmsStockBinMoveDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.item.WmsStockBinMoveItemDO;
import cn.iocoder.yudao.module.wms.service.stock.bin.move.WmsStockBinMoveService;
import cn.iocoder.yudao.module.wms.service.stock.bin.move.item.WmsStockBinMoveItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_BIN_MOVE_NOT_EXISTS;

@Tag(name = "库位移动")
@RestController
@RequestMapping("/wms/stock-bin-move")
@Validated
public class WmsStockBinMoveController {

    @Resource()
    @Lazy()
    private WmsStockBinMoveItemService stockBinMoveItemService;

    @Resource
    private WmsStockBinMoveService stockBinMoveService;

    /**
     * @sign : FDBDD7D2FB7319B5
     */
    @PostMapping("/create")
    @Operation(summary = "创建库位移动")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin-move:create')")
    public CommonResult<Long> createStockBinMove(@Valid @RequestBody WmsStockBinMoveSaveReqVO createReqVO) {
        return success(stockBinMoveService.createStockBinMove(createReqVO).getId());
    }

    // /**
    // * @sign : 5785B61F8D831D77
    // */
    // @PutMapping("/update")
    // @Operation(summary = "更新库位移动")
    // @PreAuthorize("@ss.hasPermission('wms:stock-bin-move:update')")
    // public CommonResult<Boolean> updateStockBinMove(@Valid @RequestBody WmsStockBinMoveSaveReqVO updateReqVO) {
    // stockBinMoveService.updateStockBinMove(updateReqVO);
    // return success(true);
    // }
    // @DeleteMapping("/delete")
    // @Operation(summary = "删除库位移动")
    // @Parameter(name = "id", description = "编号", required = true)
    // @PreAuthorize("@ss.hasPermission('wms:stock-bin-move:delete')")
    // public CommonResult<Boolean> deleteStockBinMove(@RequestParam("id") Long id) {
    // stockBinMoveService.deleteStockBinMove(id);
    // return success(true);
    // }
    /**
     * @sign : 51C7A99B4BB4E361
     */
    @GetMapping("/get")
    @Operation(summary = "获得库位移动")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin-move:query')")
    public CommonResult<WmsStockBinMoveRespVO> getStockBinMove(@RequestParam("id") Long id) {
        // 查询数据
        WmsStockBinMoveDO stockBinMove = stockBinMoveService.getStockBinMove(id);
        if (stockBinMove == null) {
            throw exception(STOCK_BIN_MOVE_NOT_EXISTS);
        }
        // 转换
        WmsStockBinMoveRespVO stockBinMoveVO = BeanUtils.toBean(stockBinMove, WmsStockBinMoveRespVO.class);
        // 组装库位移动详情
        List<WmsStockBinMoveItemDO> stockBinMoveItemList = stockBinMoveItemService.selectByBinMoveId(stockBinMoveVO.getId());
        stockBinMoveVO.setItemList(BeanUtils.toBean(stockBinMoveItemList, WmsStockBinMoveItemRespVO.class));
        // 返回
        return success(stockBinMoveVO);
    }

    /**
     * @sign : 4A344623E27B783A
     */
    @GetMapping("/page")
    @Operation(summary = "获得库位移动分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-bin-move:query')")
    public CommonResult<PageResult<WmsStockBinMoveRespVO>> getStockBinMovePage(@Valid WmsStockBinMovePageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsStockBinMoveDO> doPageResult = stockBinMoveService.getStockBinMovePage(pageReqVO);
        // 转换
        PageResult<WmsStockBinMoveRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsStockBinMoveRespVO.class);
        // 返回
        return success(voPageResult);
    }
    // @GetMapping("/export-excel")
    // @Operation(summary = "导出库位移动 Excel")
    // @PreAuthorize("@ss.hasPermission('wms:stock-bin-move:export')")
    // @ApiAccessLog(operateType = EXPORT)
    // public void exportStockBinMoveExcel(@Valid WmsStockBinMovePageReqVO pageReqVO, HttpServletResponse response) throws IOException {
    // pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
    // List<WmsStockBinMoveDO> list = stockBinMoveService.getStockBinMovePage(pageReqVO).getList();
    // // 导出 Excel
    // ExcelUtils.write(response, "库位移动.xls", "数据", WmsStockBinMoveRespVO.class, BeanUtils.toBean(list, WmsStockBinMoveRespVO.class));
    // }
}
