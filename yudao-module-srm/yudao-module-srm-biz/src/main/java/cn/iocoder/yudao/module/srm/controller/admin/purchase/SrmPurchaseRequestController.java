package cn.iocoder.yudao.module.srm.controller.admin.purchase;

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
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.ErpProductUnitApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductUnitDTO;
import cn.iocoder.yudao.module.erp.api.stock.WmsWarehouseApi;
import cn.iocoder.yudao.module.erp.api.stock.dto.ErpWarehouseDTO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.request.req.*;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.request.resp.SrmPurchaseRequestItemRespVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.request.resp.SrmPurchaseRequestRespVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmSupplierDO;
import cn.iocoder.yudao.module.srm.service.purchase.SrmPurchaseRequestService;
import cn.iocoder.yudao.module.srm.service.purchase.SrmSupplierService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.system.api.utils.Validation;
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
@RequestMapping("/srm/purchase-request")
@Validated
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SrmPurchaseRequestController {

    private final SrmPurchaseRequestService srmPurchaseRequestService;
    private final WmsWarehouseApi wmsWarehouseApi;
    private final ErpProductApi erpProductApi;
    private final AdminUserApi adminUserApi;
    private final DeptApi deptApi;
    private final SrmSupplierService srmSupplierService;
    private final ErpProductUnitApi erpProductUnitApi;

    @PostMapping("/create")
    @Operation(summary = "创建ERP采购申请单")
    @PreAuthorize("@ss.hasPermission('srm:purchase-request:create')")
    @Idempotent
    public CommonResult<Long> createPurchaseRequest(@Validated(Validation.OnCreate.class) @RequestBody SrmPurchaseRequestSaveReqVO createReqVO) {
        return success(srmPurchaseRequestService.createPurchaseRequest(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新ERP采购申请单")
    @PreAuthorize("@ss.hasPermission('srm:purchase-request:update')")
    public CommonResult<Boolean> updatePurchaseRequest(@Validated(Validation.OnUpdate.class) @RequestBody SrmPurchaseRequestSaveReqVO updateReqVO) {
        srmPurchaseRequestService.updatePurchaseRequest(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除ERP采购申请单")
    @Parameter(name = "id", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('srm:purchase-request:delete')")
    public CommonResult<Boolean> deletePurchaseRequest(@NotNull @RequestParam("ids") List<Long> ids) {
        srmPurchaseRequestService.deletePurchaseRequest(ids);
        return success(true);
    }

    //get获得最大流水号
    @GetMapping("/getMaxSerialNo")
    @Operation(summary = "获得ERP采购申请单NO流水号")
    @PreAuthorize("@ss.hasPermission('srm:purchase-request:query')")
    public CommonResult<String> getMaxSerialNo() {
        return success(srmPurchaseRequestService.getMaxSerialNumber());
    }

    //提交审核 submitAudit
    @PutMapping("/submitAudit")
    @Operation(summary = "提交审核")
    @Parameter(name = "ids", description = "申请单编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('srm:purchase-request:submitAudit')")
    public CommonResult<Boolean> submitAudit(@NotNull @RequestBody Collection<Long> ids) {
        srmPurchaseRequestService.submitAudit(ids);
        return success(true);
    }

    @PostMapping("/auditStatus")
    @Operation(summary = "审核/反审核")
    @PreAuthorize("@ss.hasPermission('srm:purchase-request:review')")
    public CommonResult<Boolean> reviewPurchaseRequest(@Validated @RequestBody SrmPurchaseRequestAuditReqVO req) {
        srmPurchaseRequestService.reviewPurchaseOrder(req);
        return success(true);
    }

    @PostMapping("/merge")
    @Operation(summary = "合并采购")
    @PreAuthorize("@ss.hasPermission('srm:purchase-request:merge')")
    public CommonResult<Long> mergePurchaseOrder(@Validated @RequestBody SrmPurchaseRequestMergeReqVO reqVO) {
        Long orderId = srmPurchaseRequestService.merge(reqVO);
        return success(orderId);
    }

    @PutMapping("/enableStatus")
    @Operation(summary = "关闭/启用")
    @PreAuthorize("@ss.hasPermission('srm:purchase-request:enable')")
    public CommonResult<Boolean> switchPurchaseOrderStatus(@Validated @RequestBody SrmPurchaseRequestEnableReqVO reqVO) {
        srmPurchaseRequestService.switchPurchaseOrderStatus(null, reqVO.getItemIds(), reqVO.getEnable());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得ERP采购申请单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('srm:purchase-request:query')")
    public CommonResult<SrmPurchaseRequestRespVO> getPurchaseRequest(@RequestParam("id") Long id) {
        SrmPurchaseRequestDO purchaseRequest = srmPurchaseRequestService.getPurchaseRequest(id);
        if (purchaseRequest == null) {
            return success(null);
        }
        List<SrmPurchaseRequestRespVO> vos = bindList(Collections.singletonList(purchaseRequest));
        return success(vos.get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得ERP采购申请单分页")
    @PreAuthorize("@ss.hasPermission('srm:purchase-request:query')")
    public CommonResult<PageResult<SrmPurchaseRequestRespVO>> getPurchaseRequestPage(@Valid SrmPurchaseRequestPageReqVO pageReqVO) {
        PageResult<SrmPurchaseRequestDO> pageResult = srmPurchaseRequestService.getPurchaseRequestPage(pageReqVO);
        return success(new PageResult<>(bindList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出ERP采购申请单 Excel")
    @PreAuthorize("@ss.hasPermission('srm:purchase-request:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPurchaseRequestExcel(@Valid SrmPurchaseRequestPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<SrmPurchaseRequestRespVO> list = bindList(srmPurchaseRequestService.getPurchaseRequestPage(pageReqVO).getList());
        // 导出 Excel
        ExcelUtils.write(response, "ERP采购申请单.xls", "数据", SrmPurchaseRequestRespVO.class, list);
    }

    private List<SrmPurchaseRequestRespVO> bindList(List<SrmPurchaseRequestDO> oldList) {
        if (CollUtil.isEmpty(oldList)) {
            return Collections.emptyList();
        }
        // 1.1 申请单-产品项
        List<SrmPurchaseRequestItemsDO> purchaseRequestItemList =
            srmPurchaseRequestService.getPurchaseRequestItemListByOrderIds(convertSet(oldList, SrmPurchaseRequestDO::getId));
        Map<Long, List<SrmPurchaseRequestItemsDO>> purchaseRequestItemMap = convertMultiMap(purchaseRequestItemList, SrmPurchaseRequestItemsDO::getRequestId);
        // 1.2 产品信息
        Map<Long, ErpProductDTO> productMap = erpProductApi.getProductMap(convertSet(purchaseRequestItemList, SrmPurchaseRequestItemsDO::getProductId));
        //1.3 获取用户信息
        Set<Long> userIds = Stream.concat(oldList.stream().flatMap(purchaseRequest -> Stream.of(purchaseRequest.getApplicantId(),//申请人
                purchaseRequest.getAuditorId(),//审核者
                safeParseLong(purchaseRequest.getCreator()), safeParseLong(purchaseRequest.getUpdater()))), purchaseRequestItemList.stream()
                .flatMap(purchaseRequestItem -> Stream.of(safeParseLong(purchaseRequestItem.getCreator()), safeParseLong(purchaseRequestItem.getUpdater()))))
            .distinct().filter(Objects::nonNull).collect(Collectors.toSet());
        //1.3.1 获取所有用户
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        //1.4 仓库信息
        Map<Long, ErpWarehouseDTO> warehouseMap =
            wmsWarehouseApi.getWarehouseMap(convertSet(purchaseRequestItemList, SrmPurchaseRequestItemsDO::getWarehouseId));
        //1.4 部门信息
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(oldList, SrmPurchaseRequestDO::getApplicationDeptId));
        //1.5 供应商信息
        Map<Long, SrmSupplierDO> supplierMap = srmSupplierService.getSupplierMap(convertSet(oldList, SrmPurchaseRequestDO::getSupplierId));
        //1.6 收集单位id map，从product里面
        Map<Long, ErpProductUnitDTO> unitMap =
            erpProductUnitApi.getProductUnitMap(productMap.values().stream().map(ErpProductDTO::getUnitId).collect(Collectors.toSet()));
        //2 开始拼接
        return BeanUtils.toBean(oldList, SrmPurchaseRequestRespVO.class, purchaseRequest -> {
            //2.1 申请单填充
            //设置人员信息
            MapUtils.findAndThen(userMap, purchaseRequest.getApplicantId(), user -> purchaseRequest.setApplicant(user.getNickname()));//申请人
            MapUtils.findAndThen(userMap, purchaseRequest.getAuditorId(), user -> purchaseRequest.setAuditor(user.getNickname()));//审核者
            //设置部门信息
            MapUtils.findAndThen(deptMap, purchaseRequest.getApplicationDeptId(), dept -> purchaseRequest.setApplicationDept(dept.getName()));

            //供应商信息
            MapUtils.findAndThen(supplierMap, purchaseRequest.getSupplierId(), supplier -> purchaseRequest.setSupplierName(supplier.getName()));
            purchaseRequest.setItems(BeanUtils.toBean(purchaseRequestItemMap.get(purchaseRequest.getId()), SrmPurchaseRequestItemRespVO.class, item -> {
                MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName()).setProductBarCode(product.getBarCode())
                    .setProductUnitName(unitMap.get(product.getUnitId()).getName()).setProductUnitId(unitMap.get(product.getUnitId()).getId())
                    .setNo(product.getBarCode()));
                //产品仓库填充
                MapUtils.findAndThen(warehouseMap, item.getWarehouseId(), erpWarehouseDO -> item.setWarehouseName(erpWarehouseDO.getName()));
                //产品创建者、更新者填充
                MapUtils.findAndThen(userMap, safeParseLong(item.getCreator()), user -> item.setCreator(user.getNickname()));
                MapUtils.findAndThen(userMap, safeParseLong(item.getUpdater()), user -> item.setUpdater(user.getNickname()));
                item.setOrderClosedQty(ObjectUtils.defaultIfNull(item.getOrderClosedQty(), 0));//已订购数量
                item.setInboundClosedQty(ObjectUtils.defaultIfNull(item.getInboundClosedQty(), 0));//入库
                if (item.getApprovedQty() != null && item.getOrderClosedQty() != null) {
                    item.setUnOrderCount(ObjectUtils.defaultIfNull(item.getApprovedQty() - item.getOrderClosedQty(), 0));//未订购
                }
            }));
            //2.2 申请单-产品项
            //产品名称汇总拼接
            purchaseRequest.setProductNames(CollUtil.join(purchaseRequest.getItems(), "，", SrmPurchaseRequestItemRespVO::getProductName));
            //订单产品总数
            purchaseRequest.setTotalCount(
                CollUtil.isEmpty(purchaseRequest.getItems()) ? 0 : purchaseRequest.getItems().stream().mapToInt(SrmPurchaseRequestItemRespVO::getQty).sum());
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