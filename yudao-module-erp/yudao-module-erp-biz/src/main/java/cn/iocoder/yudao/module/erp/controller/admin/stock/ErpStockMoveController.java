package cn.iocoder.yudao.module.erp.controller.admin.stock;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.move.ErpStockMovePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.move.ErpStockMoveRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.move.ErpStockMoveSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockMoveDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockMoveItemDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockMoveService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP 库存调拨单")
@RestController
@RequestMapping("/erp/stock-move")
@Validated
public class ErpStockMoveController {

    @Resource
    private ErpStockMoveService stockMoveService;
    @Resource
    private ErpStockService stockService;
    @Resource
    private ErpProductService productService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建库存调拨单")
    @PreAuthorize("@ss.hasPermission('erp:stock-move:create')")
    public CommonResult<Long> createStockMove(@Valid @RequestBody ErpStockMoveSaveReqVO createReqVO) {
        return success(stockMoveService.createStockMove(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新库存调拨单")
    @PreAuthorize("@ss.hasPermission('erp:stock-move:update')")
    public CommonResult<Boolean> updateStockMove(@Valid @RequestBody ErpStockMoveSaveReqVO updateReqVO) {
        stockMoveService.updateStockMove(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新库存调拨单的状态")
    @PreAuthorize("@ss.hasPermission('erp:stock-move:update-status')")
    public CommonResult<Boolean> updateStockMoveStatus(@RequestParam("id") Long id,
                                                     @RequestParam("status") Integer status) {
        stockMoveService.updateStockMoveStatus(id, status);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除库存调拨单")
    @Parameter(name = "ids", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('erp:stock-move:delete')")
    public CommonResult<Boolean> deleteStockMove(@RequestParam("ids") List<Long> ids) {
        stockMoveService.deleteStockMove(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得库存调拨单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:stock-move:query')")
    public CommonResult<ErpStockMoveRespVO> getStockMove(@RequestParam("id") Long id) {
        ErpStockMoveDO stockMove = stockMoveService.getStockMove(id);
        if (stockMove == null) {
            return success(null);
        }
        List<ErpStockMoveItemDO> stockMoveItemList = stockMoveService.getStockMoveItemListByMoveId(id);
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(stockMoveItemList, ErpStockMoveItemDO::getProductId));
        return success(BeanUtils.toBean(stockMove, ErpStockMoveRespVO.class, stockMoveVO ->
                stockMoveVO.setItems(BeanUtils.toBean(stockMoveItemList, ErpStockMoveRespVO.Item.class, item -> {
                    ErpStockDO stock = stockService.getStock(item.getProductId(), item.getFromWarehouseId());
                    item.setStockCount(stock != null ? stock.getCount() : BigDecimal.ZERO);
                    MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())
                            .setProductBarCode(product.getBarCode()).setProductUnitName(product.getUnitName()));
                }))));
    }

    @GetMapping("/page")
    @Operation(summary = "获得库存调拨单分页")
    @PreAuthorize("@ss.hasPermission('erp:stock-move:query')")
    public CommonResult<PageResult<ErpStockMoveRespVO>> getStockMovePage(@Valid ErpStockMovePageReqVO pageReqVO) {
        PageResult<ErpStockMoveDO> pageResult = stockMoveService.getStockMovePage(pageReqVO);
        return success(buildStockMoveVOPageResult(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出库存调拨单 Excel")
    @PreAuthorize("@ss.hasPermission('erp:stock-move:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStockMoveExcel(@Valid ErpStockMovePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpStockMoveRespVO> list = buildStockMoveVOPageResult(stockMoveService.getStockMovePage(pageReqVO)).getList();
        // 导出 Excel
        ExcelUtils.write(response, "库存调拨单.xls", "数据", ErpStockMoveRespVO.class, list);
    }

    private PageResult<ErpStockMoveRespVO> buildStockMoveVOPageResult(PageResult<ErpStockMoveDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        // 1.1 调拨项
        List<ErpStockMoveItemDO> stockMoveItemList = stockMoveService.getStockMoveItemListByMoveIds(
                convertSet(pageResult.getList(), ErpStockMoveDO::getId));
        Map<Long, List<ErpStockMoveItemDO>> stockMoveItemMap = convertMultiMap(stockMoveItemList, ErpStockMoveItemDO::getMoveId);
        // 1.2 产品信息
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(stockMoveItemList, ErpStockMoveItemDO::getProductId));
        // 1.3 TODO 芋艿：搞仓库信息
        // 1.4 管理员信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(pageResult.getList(), stockMove -> Long.parseLong(stockMove.getCreator())));
        // 2. 开始拼接
        return BeanUtils.toBean(pageResult, ErpStockMoveRespVO.class, stockMove -> {
            stockMove.setItems(BeanUtils.toBean(stockMoveItemMap.get(stockMove.getId()), ErpStockMoveRespVO.Item.class,
                    item -> MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())
                            .setProductBarCode(product.getBarCode()).setProductUnitName(product.getUnitName()))));
            stockMove.setProductNames(CollUtil.join(stockMove.getItems(), "，", ErpStockMoveRespVO.Item::getProductName));
            // TODO 芋艿：
//            MapUtils.findAndThen(customerMap, stockMove.getCustomerId(), supplier -> stockMove.setCustomerName(supplier.getName()));
            MapUtils.findAndThen(userMap, Long.parseLong(stockMove.getCreator()), user -> stockMove.setCreatorName(user.getNickname()));
        });
    }

}