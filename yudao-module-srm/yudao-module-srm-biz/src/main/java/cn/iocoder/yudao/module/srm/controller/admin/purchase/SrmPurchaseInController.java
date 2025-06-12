package cn.iocoder.yudao.module.srm.controller.admin.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.enums.TimeZoneEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.convert.DynamicTimeZoneLocalDateTimeConvert;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.SrmPurchaseInBaseRespVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.req.*;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInItemDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmSupplierDO;
import cn.iocoder.yudao.module.srm.enums.SrmPurchaseOrderSourceEnum;
import cn.iocoder.yudao.module.srm.service.purchase.SrmPurchaseInService;
import cn.iocoder.yudao.module.srm.service.purchase.SrmPurchaseOrderService;
import cn.iocoder.yudao.module.srm.service.purchase.SrmSupplierService;
import cn.iocoder.yudao.module.srm.service.purchase.bo.in.SrmPurchaseInBO;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.system.api.utils.Validation;
import cn.iocoder.yudao.module.wms.api.warehouse.WmsWarehouseApi;
import cn.iocoder.yudao.module.wms.api.warehouse.dto.WmsWarehouseDTO;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP 采购到货")
@RestController
@RequestMapping("/srm/purchase-in")
@Validated
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SrmPurchaseInController {

    private final SrmPurchaseInService purchaseInService;
    private final SrmSupplierService supplierService;
    private final WmsWarehouseApi wmsWarehouseApi;
    private final AdminUserApi adminUserApi;
    private final DeptApi deptApi;
    private final ErpProductApi erpProductApi;
    @Autowired
    @Lazy
    SrmPurchaseOrderService srmPurchaseOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建采购到货")
    @PreAuthorize("@ss.hasPermission('srm:purchase-in:create')")
    public CommonResult<Long> createPurchaseIn(@Validated(Validation.OnCreate.class) @RequestBody SrmPurchaseInSaveReqVO createReqVO) {
        createReqVO.getItems().forEach(item -> item.setSource(SrmPurchaseOrderSourceEnum.WEB_ENTRY.getDesc()));
        return success(purchaseInService.createPurchaseIn(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新采购到货")
    @PreAuthorize("@ss.hasPermission('srm:purchase-in:update')")
    public CommonResult<Boolean> updatePurchaseIn(@Validated(Validation.OnUpdate.class) @RequestBody SrmPurchaseInSaveReqVO updateReqVO) {
        purchaseInService.updatePurchaseIn(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除采购到货")
    @Parameter(name = "ids", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('srm:purchase-in:delete')")
    public CommonResult<Boolean> deletePurchaseIn(@RequestParam("ids") List<Long> ids) {
        purchaseInService.deletePurchaseIn(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得采购到货")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('srm:purchase-in:query')")
    public CommonResult<SrmPurchaseInBaseRespVO> getPurchaseIn(@RequestParam("id") Long id) {
        SrmPurchaseInBO purchaseInBO = purchaseInService.getPurchaseInBOById(id);
        return success(bindList(Collections.singletonList(purchaseInBO)).get(0));
    }

    @PostMapping("/page")
    @Operation(summary = "获得采购到货分页")
    @PreAuthorize("@ss.hasPermission('srm:purchase-in:query')")
    public CommonResult<PageResult<SrmPurchaseInBaseRespVO>> getPurchaseInPage(@Valid @RequestBody(required = false) SrmPurchaseInPageReqVO pageReqVO) {
        PageResult<SrmPurchaseInBO> pageResult = purchaseInService.getPurchaseInBOPage(pageReqVO);
        return success(new PageResult<>(bindList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出采购到货 Excel")
    @PreAuthorize("@ss.hasPermission('srm:purchase-in:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPurchaseInExcel(@Valid SrmPurchaseInPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<SrmPurchaseInBO> page = purchaseInService.getPurchaseInBOPage(pageReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "采购到货.xls", "数据", SrmPurchaseInBaseRespVO.class, bindList(page.getList()));
//        ExcelUtils.write(response, "采购到货.xls", "数据", SrmPurchaseInBaseRespVO.class, bindList(page.getList())
//                , Lists.newArrayList(DynamicTimeZoneLocalDateTimeConvert.build(TimeZoneEnum.UTC_ZONE_ID, TimeZoneEnum.UTC8_ZONE_ID)));
    }

    @PutMapping("/submitAudit")
    @Operation(summary = "提交审核")
    @PreAuthorize("@ss.hasPermission('srm:purchase-in:submit-audit')")
    public CommonResult<Boolean> submitAudit(@Valid @RequestBody SrmPurchaseInSubmitReqVO vo) {
        purchaseInService.submitAudit(vo.getArriveIds().stream().distinct().toList());
        return success(true);
    }

    @PostMapping("/auditStatus")
    @Operation(summary = "审核/反审核")
    @PreAuthorize("@ss.hasPermission('srm:purchase-in:review')")
    public CommonResult<Boolean> reviewPurchaseRequest(@Validated(Validation.OnAudit.class) @RequestBody SrmPurchaseInAuditReqVO req) {
        purchaseInService.review(req);
        return success(true);
    }

    //切换付款状态方法(暂时)
    @PostMapping("/changePayStatus")
    @Operation(summary = "切换付款状态")
    @PreAuthorize("@ss.hasPermission('srm:purchase-in:change-pay-status')")
    public CommonResult<Boolean> changePayStatus(@Valid @RequestBody SrmPurchaseInPayReqVO vo) {
        purchaseInService.switchPayStatus(vo);
        return success(true);
    }
    //TODO 合并出库

    private List<SrmPurchaseInBaseRespVO> bindList(List<SrmPurchaseInBO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }

        // 1. 获取关联数据
        // 1.1 到货项
        List<SrmPurchaseInItemDO> purchaseInItemList = list.stream()
                .flatMap(bo -> bo.getSrmPurchaseInItemDOS().stream())
                .collect(Collectors.toList());
        Map<Long, List<SrmPurchaseInItemDO>> purchaseInItemMap = convertMultiMap(purchaseInItemList, SrmPurchaseInItemDO::getArriveId);

        // 1.2 产品信息
        Map<Long, ErpProductDTO> productMap = erpProductApi.getProductMap(convertSet(purchaseInItemList, SrmPurchaseInItemDO::getProductId));
        // 1.3 供应商信息
        Map<Long, SrmSupplierDO> supplierMap = supplierService.getSupplierMap(convertSet(list, SrmPurchaseInBO::getSupplierId));
        // 1.4 人员信息
        Set<Long> userIds = Stream.concat(list.stream().flatMap(inBO -> Stream.of(inBO.getAuditorId(),//审核者
                        safeParseLong(inBO.getCreator()), safeParseLong(inBO.getUpdater()))), purchaseInItemList.stream()
                        .flatMap(itemDO -> Stream.of(safeParseLong(itemDO.getCreator()), safeParseLong(itemDO.getUpdater()), itemDO.getApplicantId())))
                .distinct().filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        // 1.5 部门
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(purchaseInItemList, SrmPurchaseInItemDO::getApplicationDeptId));
        // 1.6 获取仓库信息
        Map<Long, WmsWarehouseDTO> warehouseMap = wmsWarehouseApi.getWarehouseMap(convertSet(purchaseInItemList, SrmPurchaseInItemDO::getWarehouseId));

        //orderItemId 集合
        Set<Long> orderItemIds = purchaseInItemList.stream().map(SrmPurchaseInItemDO::getOrderItemId).collect(Collectors.toSet());
        List<SrmPurchaseOrderItemDO> orderItemList = srmPurchaseOrderService.getPurchaseOrderItemList(orderItemIds);
        //map
        Map<Long, SrmPurchaseOrderItemDO> orderItemMap = orderItemList.stream().collect(Collectors.toMap(SrmPurchaseOrderItemDO::getId, Function.identity()));

        // 2. 转换为 VO 列表
        return list.stream().map(inBO -> {
            // 2.1 转换为基础 VO
            SrmPurchaseInBaseRespVO respVO = BeanUtils.toBean(inBO, SrmPurchaseInBaseRespVO.class);
            // 2.2 设置供应商信息
            MapUtils.findAndThen(supplierMap, inBO.getSupplierId(), supplier -> respVO.setSupplierName(supplier.getName()));
            // 2.3 设置审核人信息
            MapUtils.findAndThen(userMap, inBO.getAuditorId(), user -> respVO.setAuditorName(user.getNickname()));
            // 2.4 设置创建人信息
            MapUtils.findAndThen(userMap, safeParseLong(inBO.getCreator()), user -> respVO.setCreatorName(user.getNickname()));
            // 2.5 设置更新人信息
            MapUtils.findAndThen(userMap, safeParseLong(inBO.getUpdater()), user -> respVO.setUpdaterName(user.getNickname()));
            // 2.6 设置入库项列表
            List<SrmPurchaseInItemDO> items = purchaseInItemMap.get(inBO.getId());
            if (CollUtil.isNotEmpty(items)) {
                respVO.setItems(items.stream().map(item -> {
                    SrmPurchaseInBaseRespVO.Item itemVO = BeanUtils.toBean(item, SrmPurchaseInBaseRespVO.Item.class);
                    // 设置产品信息
                    MapUtils.findAndThen(productMap, item.getProductId(), product -> {
                        itemVO.setProductName(product.getName());
                        itemVO.setModel(product.getModel());
                    });
                    // 设置仓库信息
                    MapUtils.findAndThen(warehouseMap, item.getWarehouseId(), warehouse -> itemVO.setWarehouseName(warehouse.getName()));
                    // 设置申请人信息
                    MapUtils.findAndThen(userMap, item.getApplicantId(), user -> itemVO.setApplicantName(user.getNickname()));
                    // 设置部门信息
                    MapUtils.findAndThen(deptMap, item.getApplicationDeptId(), dept -> itemVO.setApplicationDeptName(dept.getName()));
                    //关联订单行的采购数
                    MapUtils.findAndThen(orderItemMap, item.getOrderItemId(), orderItem -> itemVO.setOrderQty(orderItem.getQty()));
                    return itemVO;
                }).collect(Collectors.toList()));
            }
            return respVO;
        }).collect(Collectors.toList());
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