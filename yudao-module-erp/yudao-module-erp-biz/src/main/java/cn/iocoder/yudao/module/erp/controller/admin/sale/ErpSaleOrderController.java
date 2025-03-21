package cn.iocoder.yudao.module.erp.controller.admin.sale;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpCustomerDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderItemDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.sale.ErpCustomerService;
import cn.iocoder.yudao.module.erp.service.sale.ErpSaleOrderService;
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

@Tag(name = "管理后台 - ERP 销售订单")
@RestController
@RequestMapping("/erp/sale-order")
@Validated
public class ErpSaleOrderController {

    @Resource
    private ErpSaleOrderService saleOrderService;
    @Resource
    private ErpStockService stockService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpCustomerService customerService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建销售订单")
    @PreAuthorize("@ss.hasPermission('erp:sale-out:create')")
    public CommonResult<Long> createSaleOrder(@Valid @RequestBody ErpSaleOrderSaveReqVO createReqVO) {
        return success(saleOrderService.createSaleOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新销售订单")
    @PreAuthorize("@ss.hasPermission('erp:sale-out:update')")
    public CommonResult<Boolean> updateSaleOrder(@Valid @RequestBody ErpSaleOrderSaveReqVO updateReqVO) {
        saleOrderService.updateSaleOrder(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新销售订单的状态")
    @PreAuthorize("@ss.hasPermission('erp:sale-out:update-status')")
    public CommonResult<Boolean> updateSaleOrderStatus(@RequestParam("id") Long id,
                                                      @RequestParam("status") Integer status) {
        saleOrderService.updateSaleOrderStatus(id, status);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除销售订单")
    @Parameter(name = "ids", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('erp:sale-out:delete')")
    public CommonResult<Boolean> deleteSaleOrder(@RequestParam("ids") List<Long> ids) {
        saleOrderService.deleteSaleOrder(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得销售订单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:sale-out:query')")
    public CommonResult<ErpSaleOrderRespVO> getSaleOrder(@RequestParam("id") Long id) {
        ErpSaleOrderDO saleOrder = saleOrderService.getSaleOrder(id);
        if (saleOrder == null) {
            return success(null);
        }
        List<ErpSaleOrderItemDO> saleOrderItemList = saleOrderService.getSaleOrderItemListByOrderId(id);
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(saleOrderItemList, ErpSaleOrderItemDO::getProductId));
        return success(BeanUtils.toBean(saleOrder, ErpSaleOrderRespVO.class, saleOrderVO ->
                saleOrderVO.setItems(BeanUtils.toBean(saleOrderItemList, ErpSaleOrderRespVO.Item.class, item -> {
                    BigDecimal stockCount = stockService.getStockCount(item.getProductId());
                    item.setStockCount(stockCount != null ? stockCount : BigDecimal.ZERO);
                    MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())
                            .setProductBarCode(product.getBarCode()).setProductUnitName(product.getUnitName()));
                }))));
    }

    @GetMapping("/page")
    @Operation(summary = "获得销售订单分页")
    @PreAuthorize("@ss.hasPermission('erp:sale-out:query')")
    public CommonResult<PageResult<ErpSaleOrderRespVO>> getSaleOrderPage(@Valid ErpSaleOrderPageReqVO pageReqVO) {
        PageResult<ErpSaleOrderDO> pageResult = saleOrderService.getSaleOrderPage(pageReqVO);
        return success(buildSaleOrderVOPageResult(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出销售订单 Excel")
    @PreAuthorize("@ss.hasPermission('erp:sale-out:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSaleOrderExcel(@Valid ErpSaleOrderPageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpSaleOrderRespVO> list = buildSaleOrderVOPageResult(saleOrderService.getSaleOrderPage(pageReqVO)).getList();
        // 导出 Excel
        ExcelUtils.write(response, "销售订单.xls", "数据", ErpSaleOrderRespVO.class, list);
    }

    private PageResult<ErpSaleOrderRespVO> buildSaleOrderVOPageResult(PageResult<ErpSaleOrderDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        // 1.1 订单项
        List<ErpSaleOrderItemDO> saleOrderItemList = saleOrderService.getSaleOrderItemListByOrderIds(
                convertSet(pageResult.getList(), ErpSaleOrderDO::getId));
        Map<Long, List<ErpSaleOrderItemDO>> saleOrderItemMap = convertMultiMap(saleOrderItemList, ErpSaleOrderItemDO::getOrderId);
        // 1.2 产品信息
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(saleOrderItemList, ErpSaleOrderItemDO::getProductId));
        // 1.3 客户信息
        Map<Long, ErpCustomerDO> customerMap = customerService.getCustomerMap(
                convertSet(pageResult.getList(), ErpSaleOrderDO::getCustomerId));
        // 1.4 管理员信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(pageResult.getList(), saleOrder -> Long.parseLong(saleOrder.getCreator())));
        // 2. 开始拼接
        return BeanUtils.toBean(pageResult, ErpSaleOrderRespVO.class, saleOrder -> {
            saleOrder.setItems(BeanUtils.toBean(saleOrderItemMap.get(saleOrder.getId()), ErpSaleOrderRespVO.Item.class,
                    item -> MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())
                            .setProductBarCode(product.getBarCode()).setProductUnitName(product.getUnitName()))));
            saleOrder.setProductNames(CollUtil.join(saleOrder.getItems(), "，", ErpSaleOrderRespVO.Item::getProductName));
            MapUtils.findAndThen(customerMap, saleOrder.getCustomerId(), supplier -> saleOrder.setCustomerName(supplier.getName()));
            MapUtils.findAndThen(userMap, Long.parseLong(saleOrder.getCreator()), user -> saleOrder.setCreatorName(user.getNickname()));
        });
    }

}