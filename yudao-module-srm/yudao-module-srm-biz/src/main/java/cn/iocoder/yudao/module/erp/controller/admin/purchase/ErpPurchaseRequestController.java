package cn.iocoder.yudao.module.erp.controller.admin.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.idempotent.core.annotation.Idempotent;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.req.*;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.resp.ErpPurchaseRequestItemRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.resp.ErpPurchaseRequestRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseRequestService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpSupplierService;
import cn.iocoder.yudao.module.erp.service.stock.ErpWarehouseService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * @author Administrator
 */
@Tag(name = "管理后台 - ERP采购申请单")
@RestController
@RequestMapping("/erp/purchase-request")
@Validated
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ErpPurchaseRequestController {

    private final ErpPurchaseRequestService erpPurchaseRequestService;
    private final ErpWarehouseService erpWarehouseService;
    private final ErpProductService productService;
    private final AdminUserApi adminUserApi;
    private final DeptApi deptApi;
    private final ErpSupplierService erpSupplierService;

    @PostMapping("/create")
    @Operation(summary = "创建ERP采购申请单")
    @PreAuthorize("@ss.hasPermission('erp:purchase-request:create')")
    @Idempotent
    public CommonResult<Long> createPurchaseRequest(@Validated(validation.OnCreate.class) @RequestBody ErpPurchaseRequestSaveReqVO createReqVO) {
        return success(erpPurchaseRequestService.createPurchaseRequest(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新ERP采购申请单")
    @PreAuthorize("@ss.hasPermission('erp:purchase-request:update')")
    public CommonResult<Boolean> updatePurchaseRequest(@Validated(validation.OnUpdate.class) @RequestBody ErpPurchaseRequestSaveReqVO updateReqVO) {
        erpPurchaseRequestService.updatePurchaseRequest(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除ERP采购申请单")
    @Parameter(name = "id", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('erp:purchase-request:delete')")
    public CommonResult<Boolean> deletePurchaseRequest(@NotNull @RequestParam("ids") List<Long> ids) {
        erpPurchaseRequestService.deletePurchaseRequest(ids);
        return success(true);
    }

    //提交审核 submitAudit
    @PutMapping("/submitAudit")
    @Operation(summary = "提交审核")
    @Parameter(name = "ids", description = "申请单编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('erp:purchase-request:submitAudit')")
    public CommonResult<Boolean> submitAudit(@NotNull @RequestBody Collection<Long> ids) {
        erpPurchaseRequestService.submitAudit(ids);
        return success(true);
    }

    @PostMapping("/auditStatus")
    @Operation(summary = "审核/反审核")
    @PreAuthorize("@ss.hasPermission('erp:purchase-request:review')")
    public CommonResult<Boolean> reviewPurchaseRequest(@Validated @RequestBody ErpPurchaseRequestAuditReqVO req) {
        erpPurchaseRequestService.reviewPurchaseOrder(req);
        return success(true);
    }

    @PostMapping("/merge")
    @Operation(summary = "合并采购")
    @PreAuthorize("@ss.hasPermission('erp:purchase-request:merge')")
    public CommonResult<Long> mergePurchaseOrder(@Validated @RequestBody ErpPurchaseRequestOrderReqVO reqVO) {
        Long orderId = erpPurchaseRequestService.merge(reqVO);
        return success(orderId);
    }

    @PutMapping("/enableStatus")
    @Operation(summary = "关闭/启用")
    @PreAuthorize("@ss.hasPermission('erp:purchase-request:enable')")
    public CommonResult<Boolean> switchPurchaseOrderStatus(@Validated @RequestBody ErpPurchaseRequestEnableReqVO reqVO) {
        erpPurchaseRequestService.switchPurchaseOrderStatus(null, reqVO.getItemIds(), reqVO.getEnable());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得ERP采购申请单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:purchase-request:query')")
    public CommonResult<ErpPurchaseRequestRespVO> getPurchaseRequest(@RequestParam("id") Long id) {
        ErpPurchaseRequestDO purchaseRequest = erpPurchaseRequestService.getPurchaseRequest(id);
        if (purchaseRequest == null) {
            return success(null);
        }
        List<ErpPurchaseRequestRespVO> vos = bindList(Collections.singletonList(purchaseRequest));
        return success(vos.get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得ERP采购申请单分页")
    @PreAuthorize("@ss.hasPermission('erp:purchase-request:query')")
    public CommonResult<PageResult<ErpPurchaseRequestRespVO>> getPurchaseRequestPage(@Valid ErpPurchaseRequestPageReqVO pageReqVO) {
        PageResult<ErpPurchaseRequestDO> pageResult = erpPurchaseRequestService.getPurchaseRequestPage(pageReqVO);
        return success(new PageResult<>(bindList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出ERP采购申请单 Excel")
    @PreAuthorize("@ss.hasPermission('erp:purchase-request:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPurchaseRequestExcel(@Valid ErpPurchaseRequestPageReqVO pageReqVO,
                                           HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpPurchaseRequestRespVO> list = bindList(erpPurchaseRequestService.getPurchaseRequestPage(pageReqVO).getList());
        // 导出 Excel
        ExcelUtils.write(response, "ERP采购申请单.xls", "数据", ErpPurchaseRequestRespVO.class, list);
    }

    private List<ErpPurchaseRequestRespVO> bindList(List<ErpPurchaseRequestDO> oldList) {
        if (CollUtil.isEmpty(oldList)) {
            return Collections.emptyList();
        }
        // 1.1 申请单-产品项
        List<ErpPurchaseRequestItemsDO> purchaseRequestItemList = erpPurchaseRequestService.getPurchaseRequestItemListByOrderIds(
            convertSet(oldList, ErpPurchaseRequestDO::getId));
        Map<Long, List<ErpPurchaseRequestItemsDO>> purchaseRequestItemMap = convertMultiMap(purchaseRequestItemList, ErpPurchaseRequestItemsDO::getRequestId);
        // 1.2 产品信息
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
            convertSet(purchaseRequestItemList, ErpPurchaseRequestItemsDO::getProductId));
        //1.3 获取用户信息
        Set<Long> userIds = Stream.concat(
                oldList.stream()
                    .flatMap(purchaseRequest -> Stream.of(
                        purchaseRequest.getApplicantId(),//申请人
                        purchaseRequest.getAuditorId(),//审核者
                        safeParseLong(purchaseRequest.getCreator()),
                        safeParseLong(purchaseRequest.getUpdater())
                    )),
                purchaseRequestItemList.stream()
                    .flatMap(purchaseRequestItem -> Stream.of(
                        safeParseLong(purchaseRequestItem.getCreator()),
                        safeParseLong(purchaseRequestItem.getUpdater())
                    ))
            )
            .distinct()
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        //1.3.1 获取所有用户
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        //1.4 仓库信息
        Map<Long, ErpWarehouseDO> warehouseMap = erpWarehouseService.getWarehouseMap(
            convertSet(purchaseRequestItemList, ErpPurchaseRequestItemsDO::getWarehouseId)
        );
        //1.4 部门信息
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
            convertSet(oldList, ErpPurchaseRequestDO::getApplicationDeptId)
        );
        //1.5 供应商信息
        Map<Long, ErpSupplierDO> supplierMap = erpSupplierService.getSupplierMap(
            convertSet(oldList, ErpPurchaseRequestDO::getSupplierId)
        );
        //2 开始拼接
        return BeanUtils.toBean(oldList, ErpPurchaseRequestRespVO.class, purchaseRequest -> {
            //2.1 申请单填充
            //设置人员信息
            MapUtils.findAndThen(userMap, purchaseRequest.getApplicantId(), user -> purchaseRequest.setApplicant(user.getNickname()));//申请人
            MapUtils.findAndThen(userMap, purchaseRequest.getAuditorId(), user -> purchaseRequest.setAuditor(user.getNickname()));//审核者
            //设置部门信息
            MapUtils.findAndThen(deptMap, purchaseRequest.getApplicationDeptId(), dept -> purchaseRequest.setApplicationDept(dept.getName()));
            //供应商信息
            MapUtils.findAndThen(supplierMap, purchaseRequest.getSupplierId(), supplier -> purchaseRequest.setSupplierName(supplier.getName()));
            purchaseRequest.setItems(
                BeanUtils.toBean(purchaseRequestItemMap.get(purchaseRequest.getId()), ErpPurchaseRequestItemRespVO.class,
                    item -> {
                        MapUtils.findAndThen(productMap, item.getProductId(), product -> item
                            .setProductName(product.getName())
                            .setProductBarCode(product.getBarCode())
                            .setProductUnitName(product.getUnitName()));
                        //产品仓库填充
                        MapUtils.findAndThen(warehouseMap, item.getWarehouseId(), erpWarehouseDO -> item.setWarehouseName(erpWarehouseDO.getName()));
                        //产品创建者、更新者填充
                        MapUtils.findAndThen(userMap, safeParseLong(item.getCreator()), user -> item.setCreator(user.getNickname()));
                        MapUtils.findAndThen(userMap, safeParseLong(item.getUpdater()), user -> item.setUpdater(user.getNickname()));
                        item.setOrderCount(ObjectUtils.defaultIfNull(item.getOrderCount(), 0));//已订购数量
                        item.setInCount(ObjectUtils.defaultIfNull(item.getInCount(), 0));//入库
                        if (item.getApproveCount() != null) {
                        item.setUnOrderCount(ObjectUtils.defaultIfNull(item.getApproveCount() - item.getOrderCount(), 0));//未订购
                        }
                    }
                )
            );
            //2.2 申请单-产品项
            //产品名称汇总拼接
            purchaseRequest.setProductNames(CollUtil.join(purchaseRequest.getItems(), "，", ErpPurchaseRequestItemRespVO::getProductName));
            //订单产品总数
            purchaseRequest.setTotalCount(CollUtil.isEmpty(purchaseRequest.getItems()) ? 0 : purchaseRequest.getItems().stream().mapToInt(ErpPurchaseRequestItemRespVO::getCount).sum());
            //创建者、更新者、审核人、申请人填充
            MapUtils.findAndThen(userMap, purchaseRequest.getApplicantId(), user -> purchaseRequest.setApplicant(user.getNickname()));
            MapUtils.findAndThen(userMap, purchaseRequest.getAuditorId(), user -> purchaseRequest.setAuditor(user.getNickname()));
            MapUtils.findAndThen(userMap, safeParseLong(purchaseRequest.getCreator()), user -> purchaseRequest.setCreator(user.getNickname()));
            MapUtils.findAndThen(userMap, safeParseLong(purchaseRequest.getUpdater()), user -> purchaseRequest.setUpdater(user.getNickname()));
            //未订购数量
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