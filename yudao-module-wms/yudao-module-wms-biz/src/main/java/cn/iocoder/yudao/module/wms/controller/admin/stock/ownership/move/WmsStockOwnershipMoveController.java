package cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.item.vo.WmsStockOwnershipMoveItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.vo.WmsStockOwnershipMovePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.vo.WmsStockOwnershipMoveRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.vo.WmsStockOwnershipMoveSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.move.WmsStockOwnershipMoveDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.move.item.WmsStockOwnershipMoveItemDO;
import cn.iocoder.yudao.module.wms.service.stock.ownership.move.WmsStockOwnershipMoveService;
import cn.iocoder.yudao.module.wms.service.stock.ownership.move.item.WmsStockOwnershipMoveItemService;
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
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_OWNERSHIP_MOVE_NOT_EXISTS;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_OWNERSHIP_MOVE_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_OWNERSHIP_MOVE_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_OWNERSHIP_MOVE_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_OWNERSHIP_MOVE_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Tag(name = "所有者库存移动")
@RestController
@RequestMapping("/wms/stock-ownership-move")
@Validated
public class WmsStockOwnershipMoveController {

    @Resource()
    @Lazy()
    private WmsStockOwnershipMoveItemService stockOwnershipMoveItemService;

    @Resource
    private WmsStockOwnershipMoveService stockOwnershipMoveService;

    /**
     * @sign : E50BC63A85635F27
     */
    @PostMapping("/create")
    @Operation(summary = "创建所有者库存移动")
    @PreAuthorize("@ss.hasPermission('wms:stock-ownership-move:create')")
    public CommonResult<Long> createStockOwnershipMove(@Valid @RequestBody WmsStockOwnershipMoveSaveReqVO createReqVO) {
        return success(stockOwnershipMoveService.createStockOwnershipMove(createReqVO).getId());
    }

    // /**
    // * @sign : B17AAF1E8A33881D
    // */
    // @PutMapping("/update")
    // @Operation(summary = "更新所有者库存移动")
    // @PreAuthorize("@ss.hasPermission('wms:stock-ownership-move:update')")
    // public CommonResult<Boolean> updateStockOwnershipMove(@Valid @RequestBody WmsStockOwnershipMoveSaveReqVO updateReqVO) {
    // stockOwnershipMoveService.updateStockOwnershipMove(updateReqVO);
    // return success(true);
    // }
    // @DeleteMapping("/delete")
    // @Operation(summary = "删除所有者库存移动")
    // @Parameter(name = "id", description = "编号", required = true)
    // @PreAuthorize("@ss.hasPermission('wms:stock-ownership-move:delete')")
    // public CommonResult<Boolean> deleteStockOwnershipMove(@RequestParam("id") Long id) {
    // stockOwnershipMoveService.deleteStockOwnershipMove(id);
    // return success(true);
    // }
    /**
     * @sign : B7406A1F19B24A11
     */
    @GetMapping("/get")
    @Operation(summary = "获得所有者库存移动")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:stock-ownership-move:query')")
    public CommonResult<WmsStockOwnershipMoveRespVO> getStockOwnershipMove(@RequestParam("id") Long id) {
        // 查询数据
        WmsStockOwnershipMoveDO stockOwnershipMove = stockOwnershipMoveService.getStockOwnershipMove(id);
        if (stockOwnershipMove == null) {
            throw exception(STOCK_OWNERSHIP_MOVE_NOT_EXISTS);
        }
        // 转换
        WmsStockOwnershipMoveRespVO stockOwnershipMoveVO = BeanUtils.toBean(stockOwnershipMove, WmsStockOwnershipMoveRespVO.class);
        // 组装所有者库存移动详情
        List<WmsStockOwnershipMoveItemDO> stockOwnershipMoveItemList = stockOwnershipMoveItemService.selectByOwnershipMoveId(stockOwnershipMoveVO.getId());
        stockOwnershipMoveVO.setItemList(BeanUtils.toBean(stockOwnershipMoveItemList, WmsStockOwnershipMoveItemRespVO.class));
        // 返回
        return success(stockOwnershipMoveVO);
    }

    /**
     * @sign : 586BDA157BC07B30
     */
    @GetMapping("/page")
    @Operation(summary = "获得所有者库存移动分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-ownership-move:query')")
    public CommonResult<PageResult<WmsStockOwnershipMoveRespVO>> getStockOwnershipMovePage(@Valid WmsStockOwnershipMovePageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsStockOwnershipMoveDO> doPageResult = stockOwnershipMoveService.getStockOwnershipMovePage(pageReqVO);
        // 转换
        PageResult<WmsStockOwnershipMoveRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsStockOwnershipMoveRespVO.class);
        // 返回
        return success(voPageResult);
    }
    // @GetMapping("/export-excel")
    // @Operation(summary = "导出所有者库存移动 Excel")
    // @PreAuthorize("@ss.hasPermission('wms:stock-ownership-move:export')")
    // @ApiAccessLog(operateType = EXPORT)
    // public void exportStockOwnershipMoveExcel(@Valid WmsStockOwnershipMovePageReqVO pageReqVO, HttpServletResponse response) throws IOException {
    // pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
    // List<WmsStockOwnershipMoveDO> list = stockOwnershipMoveService.getStockOwnershipMovePage(pageReqVO).getList();
    // // 导出 Excel
    // ExcelUtils.write(response, "所有者库存移动.xls", "数据", WmsStockOwnershipMoveRespVO.class, BeanUtils.toBean(list, WmsStockOwnershipMoveRespVO.class));
    // }
}