package cn.iocoder.yudao.module.erp.controller.admin.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderBaseRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseOrderService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpSupplierService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockService;
import cn.iocoder.yudao.module.erp.service.stock.ErpWarehouseService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
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
import java.util.Optional;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP 采购订单")
@RestController
@RequestMapping("/erp/purchase-order")
@Validated
public class ErpPurchaseOrderController {

    @Resource
    private ErpPurchaseOrderService purchaseOrderService;
    @Resource
    private ErpStockService stockService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpSupplierService supplierService;
    @Resource
    private ErpWarehouseService erpWarehouseService;


    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;
//    warehouse


    @PostMapping("/create")
    @Operation(summary = "创建采购订单")
    @PreAuthorize("@ss.hasPermission('erp:purchase-order:create')")
    public CommonResult<Long> createPurchaseOrder(@Valid @RequestBody ErpPurchaseOrderSaveReqVO createReqVO) {
        return success(purchaseOrderService.createPurchaseOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新采购订单")
    @PreAuthorize("@ss.hasPermission('erp:purchase-order:update')")
    public CommonResult<Boolean> updatePurchaseOrder(@Valid @RequestBody ErpPurchaseOrderSaveReqVO updateReqVO) {
        purchaseOrderService.updatePurchaseOrder(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新采购订单的状态")
    @PreAuthorize("@ss.hasPermission('erp:purchase-order:update-status')")
    public CommonResult<Boolean> updatePurchaseOrderStatus(@RequestParam("id") Long id,
                                                           @RequestParam("status") Integer status) {
        purchaseOrderService.updatePurchaseOrderStatus(id, status);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除采购订单")
    @Parameter(name = "ids", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('erp:purchase-order:delete')")
    public CommonResult<Boolean> deletePurchaseOrder(@RequestParam("ids") List<Long> ids) {
        purchaseOrderService.deletePurchaseOrder(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得采购订单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:purchase-order:query')")
    public CommonResult<ErpPurchaseOrderBaseRespVO> getPurchaseOrder(@RequestParam("id") Long id) {
        ErpPurchaseOrderDO purchaseOrder = purchaseOrderService.getPurchaseOrder(id);
        if (purchaseOrder == null) {
            return success(null);
        }
        List<ErpPurchaseOrderItemDO> purchaseOrderItemList = purchaseOrderService.getPurchaseOrderItemListByOrderId(id);
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
            convertSet(purchaseOrderItemList, ErpPurchaseOrderItemDO::getProductId));
        return success(BeanUtils.toBean(purchaseOrder, ErpPurchaseOrderBaseRespVO.class, purchaseOrderVO ->
            purchaseOrderVO.setItems(BeanUtils.toBean(purchaseOrderItemList, ErpPurchaseOrderBaseRespVO.Item.class, item -> {
                BigDecimal purchaseCount = stockService.getStockCount(item.getProductId());
                item.setCount(purchaseCount != null ? purchaseCount : BigDecimal.ZERO);
                MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())
                    .setProductBarCode(product.getBarCode()).setProductUnitName(product.getUnitName()));
            }))));
    }

    @GetMapping("/page")
    @Operation(summary = "获得采购订单分页")
    @PreAuthorize("@ss.hasPermission('erp:purchase-order:query')")
    public CommonResult<PageResult<ErpPurchaseOrderBaseRespVO>> getPurchaseOrderPage(@Valid ErpPurchaseOrderPageReqVO pageReqVO) {
        PageResult<ErpPurchaseOrderDO> pageResult = purchaseOrderService.getPurchaseOrderPage(pageReqVO);
        return success(buildPurchaseOrderVOPageResult(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出采购订单 Excel")
    @PreAuthorize("@ss.hasPermission('erp:purchase-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPurchaseOrderExcel(@Valid ErpPurchaseOrderPageReqVO pageReqVO,
                                         HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpPurchaseOrderBaseRespVO> list = buildPurchaseOrderVOPageResult(purchaseOrderService.getPurchaseOrderPage(pageReqVO)).getList();
        // 导出 Excel
        ExcelUtils.write(response, "采购订单.xls", "数据", ErpPurchaseOrderBaseRespVO.class, list);
    }

    private PageResult<ErpPurchaseOrderBaseRespVO> buildPurchaseOrderVOPageResult(PageResult<ErpPurchaseOrderDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        // 1.1 订单项
        List<ErpPurchaseOrderItemDO> purchaseOrderItemList = purchaseOrderService.getPurchaseOrderItemListByOrderIds(
            convertSet(pageResult.getList(), ErpPurchaseOrderDO::getId));
        Map<Long, List<ErpPurchaseOrderItemDO>> purchaseOrderItemMap = convertMultiMap(purchaseOrderItemList, ErpPurchaseOrderItemDO::getOrderId);
        // 1.2 产品信息
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
            convertSet(purchaseOrderItemList, ErpPurchaseOrderItemDO::getProductId));
        // 1.3 供应商信息
        Map<Long, ErpSupplierDO> supplierMap = supplierService.getSupplierMap(
            convertSet(pageResult.getList(), ErpPurchaseOrderDO::getSupplierId));
        // 1.4 管理员信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
            convertSet(pageResult.getList(), purchaseOrder -> Long.parseLong(purchaseOrder.getCreator())));
        // 1.5 部门信息
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
            convertSet(pageResult.getList(), ErpPurchaseOrderDO::getDepartmentId));
        // 1.6 获取仓库信息
        Map<Long, ErpWarehouseDO> warehouseMap = erpWarehouseService.getWarehouseMap(
            convertSet(purchaseOrderItemList, ErpPurchaseOrderItemDO::getWarehouseId));


        // 2. 开始拼接
        return BeanUtils.toBean(pageResult, ErpPurchaseOrderBaseRespVO.class, purchaseOrder -> {
            purchaseOrder.setItems(BeanUtils.toBean(purchaseOrderItemMap.get(purchaseOrder.getId()), ErpPurchaseOrderBaseRespVO.Item.class,
                item -> {
                    //设置产品信息
                    MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())
                        .setProductBarCode(product.getBarCode())
                        .setProductUnitName(product.getUnitName()));
                    // 设置仓库信息
                    MapUtils.findAndThen(warehouseMap, item.getWarehouseId(), erpWarehouseDO -> item.setWarehouseName(erpWarehouseDO.getName()));
                }));
            purchaseOrder.setProductNames(CollUtil.join(purchaseOrder.getItems(), "，", ErpPurchaseOrderBaseRespVO.Item::getProductName));//设置订单下的产品信息
            purchaseOrder.setStatusDesc(ErpAuditStatus.getDescriptionByStatus(purchaseOrder.getStatus()));//设置采购状态
            MapUtils.findAndThen(supplierMap, purchaseOrder.getSupplierId(), supplier -> purchaseOrder.setSupplierName(supplier.getName()));
            //设置创建人的部门name
            MapUtils.findAndThen(deptMap, adminUserApi.getUser(Long.parseLong(purchaseOrder.getCreator())).getDeptId(), deptRespDTO -> purchaseOrder.setDepartmentName(deptRespDTO.getName()));
            MapUtils.findAndThen(userMap, Long.parseLong(purchaseOrder.getCreator()), user -> purchaseOrder.setCreator(user.getNickname()));
            //设置审核人的name
            Optional.ofNullable(purchaseOrder.getAuditor()).ifPresent(auditor->
                MapUtils.findAndThen(userMap,Long.parseLong(auditor),user->purchaseOrder.setAuditorName(user.getNickname()))
            );
            //设置币别name

        });
    }

}