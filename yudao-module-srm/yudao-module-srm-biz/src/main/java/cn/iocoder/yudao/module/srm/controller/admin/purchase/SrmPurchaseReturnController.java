package cn.iocoder.yudao.module.srm.controller.admin.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.idempotent.core.annotation.Idempotent;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.returns.SrmPurchaseReturnAuditReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.returns.SrmPurchaseReturnBaseRespVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.returns.SrmPurchaseReturnPageReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.returns.SrmPurchaseReturnSaveReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnItemDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmSupplierDO;
import cn.iocoder.yudao.module.srm.service.purchase.SrmPurchaseReturnService;
import cn.iocoder.yudao.module.srm.service.purchase.SrmSupplierService;
import cn.iocoder.yudao.module.srm.service.purchase.refund.SrmPurchaseReturnBO;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.system.api.utils.Validation;
import cn.iocoder.yudao.module.wms.api.warehouse.WmsWarehouseApi;
import cn.iocoder.yudao.module.wms.api.warehouse.dto.WmsWarehouseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP 采购退货")
@RestController
@RequestMapping("/srm/purchase-return")
@Validated
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SrmPurchaseReturnController {

    private final SrmPurchaseReturnService purchaseReturnService;
    private final SrmSupplierService supplierService;
    private final AdminUserApi adminUserApi;
    private final DeptApi deptApi;
    private final WmsWarehouseApi wmsWarehouseApi;


    @PostMapping("/create")
    @Operation(summary = "创建采购退货")
    @Idempotent
    @PreAuthorize("@ss.hasPermission('srm:purchase-return:create')")
    public CommonResult<Long> createPurchaseReturn(@Validated(Validation.OnCreate.class) @RequestBody SrmPurchaseReturnSaveReqVO createReqVO) {
        return success(purchaseReturnService.createPurchaseReturn(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新采购退货")
    @PreAuthorize("@ss.hasPermission('srm:purchase-return:update')")
    public CommonResult<Boolean> updatePurchaseReturn(@Validated(Validation.OnUpdate.class) @RequestBody SrmPurchaseReturnSaveReqVO updateReqVO) {
        purchaseReturnService.updatePurchaseReturn(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除采购退货")
    @Parameter(name = "ids", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('srm:purchase-return:delete')")
    public CommonResult<Boolean> deletePurchaseReturn(@RequestParam("ids") List<Long> ids) {
        purchaseReturnService.deletePurchaseReturn(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得采购退货")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('srm:purchase-return:query')")
    public CommonResult<SrmPurchaseReturnBaseRespVO> getPurchaseReturn(@RequestParam("id") Long id) {
        SrmPurchaseReturnBO purchaseReturn = purchaseReturnService.getPurchaseBOReturn(id);
        if (purchaseReturn == null) {
            return success(null);
        }
        return success(bindResult(Collections.singletonList(purchaseReturn)).get(0));
    }

    @PostMapping("/page")
    @Operation(summary = "获得采购退货分页")
    @PreAuthorize("@ss.hasPermission('srm:purchase-return:query')")
    public CommonResult<PageResult<SrmPurchaseReturnBaseRespVO>> getPurchaseReturnPage(@Valid @RequestBody(required = false) SrmPurchaseReturnPageReqVO pageReqVO) {
        PageResult<SrmPurchaseReturnBO> pageResult = purchaseReturnService.getPurchaseReturnBOPage(pageReqVO);
        return success(new PageResult<>(bindResult(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出采购退货 Excel")
    @PreAuthorize("@ss.hasPermission('srm:purchase-return:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPurchaseReturnExcel(@Valid SrmPurchaseReturnPageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<SrmPurchaseReturnBO> page = purchaseReturnService.getPurchaseReturnBOPage(pageReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "采购退货.xls", "数据", SrmPurchaseReturnBaseRespVO.class, bindResult(page.getList()));
    }

    //提交审核
    @PutMapping("/submitAudit")
    @Operation(summary = "提交审核")
    @PreAuthorize("@ss.hasPermission('srm:purchase-return:submit')")
    public CommonResult<Boolean> submitPurchaseReturn(@Validated(Validation.OnSubmitAudit.class) @RequestBody SrmPurchaseReturnAuditReqVO reqVO) {
        purchaseReturnService.submitAudit(reqVO.getIds());
        return success(true);
    }

    //审核通过|审核撤销
    @PutMapping("/auditStatus")
    @Operation(summary = "审核/反审核")
    @PreAuthorize("@ss.hasPermission('srm:purchase-return:review')")
    public CommonResult<Boolean> auditPurchaseReturn(@Validated(Validation.OnAudit.class) @RequestBody SrmPurchaseReturnAuditReqVO reqVO) {
        purchaseReturnService.review(reqVO);
        return success(true);
    }

    //切换退款状态
    @PutMapping("/refundStatus")
    @Operation(summary = "切换退款状态")
    @PreAuthorize("@ss.hasPermission('srm:purchase-return:refund')")
    public CommonResult<Boolean> refundPurchaseReturn(@Validated(Validation.OnSwitch.class) @RequestBody SrmPurchaseReturnAuditReqVO reqVO) {
        purchaseReturnService.refund(reqVO);
        return success(true);
    }

    private static Long safeParseLong(String str) {
        if (str == null) {
            return null;
        }
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }


    private List<SrmPurchaseReturnBaseRespVO> bindResult(List<SrmPurchaseReturnBO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }

        // 1. 获取关联数据
        // 1.1 供应商信息
        Map<Long, SrmSupplierDO> supplierMap = supplierService.getSupplierMap(
            convertSet(list, SrmPurchaseReturnDO::getSupplierId));

        // 1.2 人员信息
        Set<Long> userIds = Stream.concat(
            list.stream().flatMap(returnDO -> Stream.of(
                returnDO.getAuditorId(), // 审核者
                safeParseLong(returnDO.getCreator()), // 创建者
                safeParseLong(returnDO.getUpdater())  // 更新者
            )),
            list.stream().flatMap(returnDO -> returnDO.getSrmPurchaseReturnItemDOs().stream())
                .flatMap(itemDO -> Stream.of(
                    safeParseLong(itemDO.getCreator()), // 创建者
                    safeParseLong(itemDO.getUpdater()), // 更新者
                    itemDO.getApplicantId() // 申请人
                ))
        ).distinct().filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);

        // 1.3 部门信息
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
            convertSet(list.stream().flatMap(returnDO -> returnDO.getSrmPurchaseReturnItemDOs().stream()).collect(Collectors.toList()),
                SrmPurchaseReturnItemDO::getApplicationDeptId));

        // 1.4 仓库信息
        Map<Long, WmsWarehouseDTO> warehouseMap = wmsWarehouseApi.getWarehouseMap(
            convertSet(list.stream().flatMap(returnDO -> returnDO.getSrmPurchaseReturnItemDOs().stream()).collect(Collectors.toList()),
                SrmPurchaseReturnItemDO::getWarehouseId));

        //MAP
        Map<Long, List<SrmPurchaseReturnItemDO>> returnItemMap = list.stream()
            .flatMap(returnDO -> returnDO.getSrmPurchaseReturnItemDOs().stream())
            .collect(Collectors.groupingBy(SrmPurchaseReturnItemDO::getReturnId));
        // 2. 开始拼接
        return BeanUtils.toBean(list, SrmPurchaseReturnBaseRespVO.class, purchaseReturn -> {
            // 2.1 设置退货项
            List<SrmPurchaseReturnBaseRespVO.Item> items = BeanUtils.toBean(
                returnItemMap.get(purchaseReturn.getId()),
                SrmPurchaseReturnBaseRespVO.Item.class,
                item -> {
                    // 2.1.1 设置仓库信息
                    MapUtils.findAndThen(warehouseMap, item.getWarehouseId(), warehouse -> {
                        item.setWarehouseName(warehouse.getName());
                    });
                    // 2.1.2 设置申请人信息
                    MapUtils.findAndThen(userMap, item.getApplicantId(), user -> {
                        item.setApplicantName(user.getNickname());
                    });
                    // 2.1.3 设置申请部门信息
                    MapUtils.findAndThen(deptMap, item.getApplicationDeptId(), dept -> {
                        item.setApplicationDeptName(dept.getName());
                    });
                    // 2.1.4 设置创建人信息
                    MapUtils.findAndThen(userMap, safeParseLong(item.getCreator()), user -> {
                        item.setCreator(user.getNickname());
                    });
                    // 2.1.5 设置更新人信息
                    MapUtils.findAndThen(userMap, safeParseLong(item.getUpdater()), user -> {
                        item.setUpdater(user.getNickname());
                    });
                }
            );
            purchaseReturn.setItems(items);

            // 2.2 设置供应商信息
            MapUtils.findAndThen(supplierMap, purchaseReturn.getSupplierId(), supplier -> {
                purchaseReturn.setSupplierName(supplier.getName());
            });

            // 2.3 设置审核人信息
            MapUtils.findAndThen(userMap, purchaseReturn.getAuditorId(), user -> {
                purchaseReturn.setAuditorName(user.getNickname());
            });

            // 2.4 设置创建人信息
            MapUtils.findAndThen(userMap, safeParseLong(purchaseReturn.getCreator()), user -> {
                purchaseReturn.setCreator(user.getNickname());
            });

            // 2.5 设置更新人信息
            MapUtils.findAndThen(userMap, safeParseLong(purchaseReturn.getUpdater()), user -> {
                purchaseReturn.setUpdater(user.getNickname());
            });
        });
    }
}