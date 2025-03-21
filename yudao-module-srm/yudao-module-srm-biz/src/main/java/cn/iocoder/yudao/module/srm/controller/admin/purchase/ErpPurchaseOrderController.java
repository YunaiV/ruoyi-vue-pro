package cn.iocoder.yudao.module.srm.controller.admin.purchase;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.idempotent.core.annotation.Idempotent;
import cn.iocoder.yudao.module.srm.api.product.ErpProductApi;
import cn.iocoder.yudao.module.srm.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.srm.api.stock.WmsWarehouseApi;
import cn.iocoder.yudao.module.srm.api.stock.dto.ErpWarehouseDTO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.*;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpSupplierDO;
import cn.iocoder.yudao.module.srm.service.purchase.ErpPurchaseOrderService;
import cn.iocoder.yudao.module.srm.service.purchase.ErpSupplierService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.system.api.utils.Validation;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    WmsWarehouseApi wmsWarehouseApi;
    @Resource
    private ErpSupplierService supplierService;
    @Resource
    private ErpProductApi erpProductApi;


    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;
//    warehouse

    @PostMapping("/create")
    @Operation(summary = "创建采购订单")
    @Idempotent
    @PreAuthorize("@ss.hasPermission('erp:purchase-order:create')")
    public CommonResult<Long> createPurchaseOrder(@Validated(Validation.OnCreate.class) @RequestBody ErpPurchaseOrderSaveReqVO createReqVO) {
        return success(purchaseOrderService.createPurchaseOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新采购订单")
    @PreAuthorize("@ss.hasPermission('erp:purchase-order:update')")
    public CommonResult<Boolean> updatePurchaseOrder(@Validated(Validation.OnUpdate.class) @RequestBody ErpPurchaseOrderSaveReqVO updateReqVO) {
        purchaseOrderService.updatePurchaseOrder(updateReqVO);
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
        List<ErpPurchaseOrderBaseRespVO> vos = bindList(Collections.singletonList(purchaseOrder));
        return success(vos.get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得采购订单分页")
    @PreAuthorize("@ss.hasPermission('erp:purchase-order:query')")
    public CommonResult<PageResult<ErpPurchaseOrderBaseRespVO>> getPurchaseOrderPage(@Valid ErpPurchaseOrderPageReqVO pageReqVO) {
        PageResult<ErpPurchaseOrderDO> pageResult = purchaseOrderService.getPurchaseOrderPage(pageReqVO);
        List<ErpPurchaseOrderBaseRespVO> voList = bindList(pageResult.getList());
        return success(new PageResult<>(voList, pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出采购订单 Excel")
    @PreAuthorize("@ss.hasPermission('erp:purchase-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPurchaseOrderExcel(@Valid ErpPurchaseOrderPageReqVO pageReqVO,
                                         HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpPurchaseOrderBaseRespVO> voList = bindList(purchaseOrderService.getPurchaseOrderPage(pageReqVO).getList());
        // 导出 Excel
        ExcelUtils.write(response, "采购订单.xls", "数据", ErpPurchaseOrderBaseRespVO.class, voList);
    }

    @PostMapping("/submitAudit")
    @Operation(summary = "提交审核")
    @Parameter(name = "itemIds", description = "订单下的订单项Id集合", required = true)
    @PreAuthorize("@ss.hasPermission('erp:purchase-order:submitAudit')")
    public CommonResult<Boolean> submitAudit(@Validated @RequestBody ErpPurchaseOrderAuditReqVO reqVO) {
        purchaseOrderService.submitAudit(reqVO.getOrderIds());
        return success(true);
    }

    @PostMapping("/auditStatus")
    @Operation(summary = "审核/反审核")
    @Parameter(name = "requestId", description = "申请单编号", required = true)
    @Parameter(name = "reviewed", description = "审核状态", required = true)
    @PreAuthorize("@ss.hasPermission('erp:purchase-order:review')")
    public CommonResult<Boolean> reviewPurchaseRequest(@Validated(Validation.OnAudit.class) @RequestBody ErpPurchaseOrderAuditReqVO req) {
        purchaseOrderService.reviewPurchaseOrder(req);
        return success(true);
    }

    @PutMapping("/enableStatus")
    @Operation(summary = "关闭/启用")
    @PreAuthorize("@ss.hasPermission('erp:purchasereq-order:enable')")
    public CommonResult<Boolean> switchPurchaseOrderStatus(@Validated(Validation.OnSwitch.class) @RequestBody ErpPurchaseOrderAuditReqVO reqVO) {
        //获得reqVO的item的ids
        List<Long> itemIds = reqVO.getItems().stream().map(ErpPurchaseOrderAuditReqVO.requestItems::getId).collect(Collectors.toSet()).stream().toList();
        purchaseOrderService.switchPurchaseOrderStatus(itemIds, reqVO.getEnable());
        return success(true);
    }

    @PostMapping("/merge")
    @Operation(summary = "合并入库")
    @PreAuthorize("@ss.hasPermission('erp:purchasereq-order:merge')")
    public CommonResult<Boolean> mergePurchaseOrder(@Validated @RequestBody ErpPurchaseOrderMergeReqVO reqVO) {
        purchaseOrderService.merge(reqVO);
        return success(true);
    }

    private List<ErpPurchaseOrderBaseRespVO> bindList(List<ErpPurchaseOrderDO> list) {
        // 1.1 订单项
        List<ErpPurchaseOrderItemDO> purchaseOrderItemList = purchaseOrderService.getPurchaseOrderItemListByOrderIds(
            convertSet(list, ErpPurchaseOrderDO::getId));
        Map<Long, List<ErpPurchaseOrderItemDO>> purchaseOrderItemMap = convertMultiMap(purchaseOrderItemList, ErpPurchaseOrderItemDO::getOrderId);
        // 1.2 产品
        Map<Long, ErpProductDTO> productMap = erpProductApi.getProductMap(
            convertSet(purchaseOrderItemList, ErpPurchaseOrderItemDO::getProductId));
        // 1.3 供应商
        Map<Long, ErpSupplierDO> supplierMap = supplierService.getSupplierMap(
            convertSet(list, ErpPurchaseOrderDO::getSupplierId));
        // 1.4 人员
        Set<Long> userIds = Stream.concat(
                list.stream()
                    .flatMap(orderDO -> Stream.of(
                        orderDO.getAuditorId(),//审核者
                        safeParseLong(orderDO.getCreator()),
                        safeParseLong(orderDO.getUpdater())
                    )),
                purchaseOrderItemList.stream()
                    .flatMap(orderItemDO -> Stream.of(
                        safeParseLong(orderItemDO.getCreator()),
                        safeParseLong(orderItemDO.getUpdater()),
                        orderItemDO.getApplicantId()
                    ))
            )
            .distinct()
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        // 1.5 部门
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
            convertSet(purchaseOrderItemList, ErpPurchaseOrderItemDO::getApplicationDeptId));
        // 1.6 仓库
        Map<Long, ErpWarehouseDTO> warehouseMap = wmsWarehouseApi.getWarehouseMap(
            convertSet(purchaseOrderItemList, ErpPurchaseOrderItemDO::getWarehouseId));

        // 2. 开始拼接
        return BeanUtils.toBean(list, ErpPurchaseOrderBaseRespVO.class, respVO -> {
            respVO.setItems(BeanUtils.toBean(purchaseOrderItemMap.get(respVO.getId()), ErpPurchaseOrderBaseRespVO.Item.class,
                item -> {
                    //设置产品
                    MapUtils.findAndThen(productMap, item.getProductId(), item::setProduct);
                    // 设置仓库
                    MapUtils.findAndThen(warehouseMap, item.getWarehouseId(), erpWarehouseDO -> item.setWarehouseName(erpWarehouseDO.getName()));
                    //人员
                    MapUtils.findAndThen(userMap, Long.parseLong(item.getCreator()), user -> item.setCreator(user.getNickname()));
                    MapUtils.findAndThen(userMap, Long.parseLong(item.getUpdater()), user -> item.setUpdater(user.getNickname()));
                    //申请人name
                    MapUtils.findAndThen(userMap, item.getApplicantId(), user -> item.setApplicantName(user.getNickname()));
                    //部门name
                    MapUtils.findAndThen(deptMap, item.getApplicationDeptId(), dept -> item.setDepartmentName(dept.getName()));
                }));
            MapUtils.findAndThen(supplierMap, respVO.getSupplierId(), supplier -> respVO.setSupplierName(supplier.getName()));
            //人员
            MapUtils.findAndThen(userMap, Long.parseLong(respVO.getCreator()), user -> respVO.setCreator(user.getNickname()));
            MapUtils.findAndThen(userMap, Long.parseLong(respVO.getUpdater()), user -> respVO.setUpdater(user.getNickname()));
            Optional.ofNullable(respVO.getAuditor()).ifPresent(auditor ->
                MapUtils.findAndThen(userMap, Long.parseLong(auditor), user -> respVO.setAuditorName(user.getNickname()))
            );
        });
    }

    /**
     * 安全转换id为 Long
     *
     * @param value String类型的id
     * @return id
     */
    private Long safeParseLong(String value) {
        try {
            return Optional.ofNullable(value).map(Long::parseLong).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}