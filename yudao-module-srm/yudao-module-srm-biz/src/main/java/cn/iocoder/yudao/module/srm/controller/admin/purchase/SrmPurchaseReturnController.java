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
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.returns.SrmPurchaseReturnAuditReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.returns.SrmPurchaseReturnBaseRespVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.returns.SrmPurchaseReturnPageReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.returns.SrmPurchaseReturnSaveReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnItemDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmSupplierDO;
import cn.iocoder.yudao.module.srm.service.purchase.SrmPurchaseReturnService;
import cn.iocoder.yudao.module.srm.service.purchase.SrmSupplierService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP 采购退货")
@RestController
@RequestMapping("/erp/purchase-return")
@Validated
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SrmPurchaseReturnController {

    private final SrmPurchaseReturnService purchaseReturnService;
    private final SrmSupplierService supplierService;
    private final ErpProductApi erpProductApi;
    private final AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建采购退货")
    @Idempotent
    @PreAuthorize("@ss.hasPermission('srm:purchase-return:create')")
    public CommonResult<Long> createPurchaseReturn(@Valid @RequestBody SrmPurchaseReturnSaveReqVO createReqVO) {
        return success(purchaseReturnService.createPurchaseReturn(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新采购退货")
    @PreAuthorize("@ss.hasPermission('srm:purchase-return:update')")
    public CommonResult<Boolean> updatePurchaseReturn(@Valid @RequestBody SrmPurchaseReturnSaveReqVO updateReqVO) {
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
        SrmPurchaseReturnDO purchaseReturn = purchaseReturnService.getPurchaseReturn(id);
        if (purchaseReturn == null) {
            return success(null);
        }
        return success(bindResult(Collections.singletonList(purchaseReturn)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得采购退货分页")
    @PreAuthorize("@ss.hasPermission('srm:purchase-return:query')")
    public CommonResult<PageResult<SrmPurchaseReturnBaseRespVO>> getPurchaseReturnPage(@Valid SrmPurchaseReturnPageReqVO pageReqVO) {
        PageResult<SrmPurchaseReturnDO> pageResult = purchaseReturnService.getPurchaseReturnPage(pageReqVO);
        return success(new PageResult<>(bindResult(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出采购退货 Excel")
    @PreAuthorize("@ss.hasPermission('srm:purchase-return:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPurchaseReturnExcel(@Valid SrmPurchaseReturnPageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<SrmPurchaseReturnDO> page = purchaseReturnService.getPurchaseReturnPage(pageReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "采购退货.xls", "数据", SrmPurchaseReturnBaseRespVO.class, bindResult(page.getList()));
    }

    //提交审核
    @PutMapping("/submitAudit")
    @Operation(summary = "提交审核")
    @PreAuthorize("@ss.hasPermission('srm:purchase-return:submit')")
    public CommonResult<Boolean> submitPurchaseReturn(@Valid SrmPurchaseReturnAuditReqVO reqVO) {
        purchaseReturnService.submitAudit(reqVO.getIds());
        return success(true);
    }

    //审核通过|审核撤销
    @PutMapping("/auditStatus")
    @Operation(summary = "审核/反审核")
    @PreAuthorize("@ss.hasPermission('srm:purchase-return:review')")
    public CommonResult<Boolean> auditPurchaseReturn(@Valid SrmPurchaseReturnAuditReqVO reqVO) {
        purchaseReturnService.review(reqVO);
        return success(true);
    }

    //切换退款状态
    @PutMapping("/refundStatus")
    @Operation(summary = "切换退款状态")
    @PreAuthorize("@ss.hasPermission('srm:purchase-return:refund')")
    public CommonResult<Boolean> refundPurchaseReturn(@Valid SrmPurchaseReturnAuditReqVO reqVO) {
        purchaseReturnService.refund(reqVO);
        return success(true);
    }

    private List<SrmPurchaseReturnBaseRespVO> bindResult(List<SrmPurchaseReturnDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1.1 退货项
        List<SrmPurchaseReturnItemDO> purchaseReturnItemList = purchaseReturnService.getPurchaseReturnItemListByReturnIds(
            convertSet(list, SrmPurchaseReturnDO::getId));
        Map<Long, List<SrmPurchaseReturnItemDO>> purchaseReturnItemMap = convertMultiMap(purchaseReturnItemList, SrmPurchaseReturnItemDO::getReturnId);
        // 1.2 产品信息
        Map<Long, ErpProductDTO> productMap = erpProductApi.getProductMap(
            convertSet(purchaseReturnItemList, SrmPurchaseReturnItemDO::getProductId));
        // 1.3 供应商信息
        Map<Long, SrmSupplierDO> supplierMap = supplierService.getSupplierMap(
            convertSet(list, SrmPurchaseReturnDO::getSupplierId));
        // 1.4 管理员信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
            convertSet(list, purchaseReturn -> Long.parseLong(purchaseReturn.getCreator())));
        // 2. 开始拼接
        return BeanUtils.toBean(list, SrmPurchaseReturnBaseRespVO.class, purchaseReturn -> {
            purchaseReturn.setItems(BeanUtils.toBean(purchaseReturnItemMap.get(purchaseReturn.getId()), SrmPurchaseReturnBaseRespVO.Item.class
                )
            );
            purchaseReturn.setProductNames(CollUtil.join(purchaseReturn.getItems(), "，", SrmPurchaseReturnBaseRespVO.Item::getProductName));
            MapUtils.findAndThen(supplierMap, purchaseReturn.getSupplierId(), supplier -> purchaseReturn.setSupplierName(supplier.getName()));
            MapUtils.findAndThen(userMap, Long.parseLong(purchaseReturn.getCreator()), user -> purchaseReturn.setCreator(user.getNickname()));
        });
    }
}