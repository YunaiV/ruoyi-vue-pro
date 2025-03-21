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
import cn.iocoder.yudao.module.srm.api.product.ErpProductApi;
import cn.iocoder.yudao.module.srm.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.returns.ErpPurchaseReturnAuditReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.returns.ErpPurchaseReturnBaseRespVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.returns.ErpPurchaseReturnPageReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.returns.ErpPurchaseReturnSaveReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseReturnItemDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpSupplierDO;
import cn.iocoder.yudao.module.srm.service.purchase.ErpPurchaseReturnService;
import cn.iocoder.yudao.module.srm.service.purchase.ErpSupplierService;
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
public class ErpPurchaseReturnController {

    private final ErpPurchaseReturnService purchaseReturnService;
    private final ErpSupplierService supplierService;
    private final ErpProductApi erpProductApi;
    private final AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建采购退货")
    @Idempotent
    @PreAuthorize("@ss.hasPermission('erp:purchase-return:create')")
    public CommonResult<Long> createPurchaseReturn(@Valid @RequestBody ErpPurchaseReturnSaveReqVO createReqVO) {
        return success(purchaseReturnService.createPurchaseReturn(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新采购退货")
    @PreAuthorize("@ss.hasPermission('erp:purchase-return:update')")
    public CommonResult<Boolean> updatePurchaseReturn(@Valid @RequestBody ErpPurchaseReturnSaveReqVO updateReqVO) {
        purchaseReturnService.updatePurchaseReturn(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除采购退货")
    @Parameter(name = "ids", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('erp:purchase-return:delete')")
    public CommonResult<Boolean> deletePurchaseReturn(@RequestParam("ids") List<Long> ids) {
        purchaseReturnService.deletePurchaseReturn(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得采购退货")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:purchase-return:query')")
    public CommonResult<ErpPurchaseReturnBaseRespVO> getPurchaseReturn(@RequestParam("id") Long id) {
        ErpPurchaseReturnDO purchaseReturn = purchaseReturnService.getPurchaseReturn(id);
        if (purchaseReturn == null) {
            return success(null);
        }
        return success(bindResult(Collections.singletonList(purchaseReturn)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得采购退货分页")
    @PreAuthorize("@ss.hasPermission('erp:purchase-return:query')")
    public CommonResult<PageResult<ErpPurchaseReturnBaseRespVO>> getPurchaseReturnPage(@Valid ErpPurchaseReturnPageReqVO pageReqVO) {
        PageResult<ErpPurchaseReturnDO> pageResult = purchaseReturnService.getPurchaseReturnPage(pageReqVO);
        return success(new PageResult<>(bindResult(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出采购退货 Excel")
    @PreAuthorize("@ss.hasPermission('erp:purchase-return:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPurchaseReturnExcel(@Valid ErpPurchaseReturnPageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<ErpPurchaseReturnDO> page = purchaseReturnService.getPurchaseReturnPage(pageReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "采购退货.xls", "数据", ErpPurchaseReturnBaseRespVO.class, bindResult(page.getList()));
    }

    //提交审核
    @PutMapping("/submitAudit")
    @Operation(summary = "提交审核")
    @PreAuthorize("@ss.hasPermission('erp:purchase-return:submit')")
    public CommonResult<Boolean> submitPurchaseReturn(@NotNull @RequestBody ErpPurchaseReturnAuditReqVO reqVO) {
        purchaseReturnService.submitAudit(reqVO.getIds());
        return success(true);
    }

    //审核通过|审核撤销
    @PutMapping("/auditStatus")
    @Operation(summary = "审核/反审核")
    @PreAuthorize("@ss.hasPermission('erp:purchase-return:review')")
    public CommonResult<Boolean> auditPurchaseReturn(@NotNull @RequestBody ErpPurchaseReturnAuditReqVO reqVO) {
        purchaseReturnService.review(reqVO);
        return success(true);
    }

    //切换退款状态
    @PutMapping("/refundStatus")
    @Operation(summary = "切换退款状态")
    @PreAuthorize("@ss.hasPermission('erp:purchase-return:refund')")
    public CommonResult<Boolean> refundPurchaseReturn(@NotNull @RequestBody ErpPurchaseReturnAuditReqVO reqVO) {
        purchaseReturnService.refund(reqVO);
        return success(true);
    }

    private List<ErpPurchaseReturnBaseRespVO> bindResult(List<ErpPurchaseReturnDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1.1 退货项
        List<ErpPurchaseReturnItemDO> purchaseReturnItemList = purchaseReturnService.getPurchaseReturnItemListByReturnIds(
            convertSet(list, ErpPurchaseReturnDO::getId));
        Map<Long, List<ErpPurchaseReturnItemDO>> purchaseReturnItemMap = convertMultiMap(purchaseReturnItemList, ErpPurchaseReturnItemDO::getReturnId);
        // 1.2 产品信息
        Map<Long, ErpProductDTO> productMap = erpProductApi.getProductMap(
                convertSet(purchaseReturnItemList, ErpPurchaseReturnItemDO::getProductId));
        // 1.3 供应商信息
        Map<Long, ErpSupplierDO> supplierMap = supplierService.getSupplierMap(
            convertSet(list, ErpPurchaseReturnDO::getSupplierId));
        // 1.4 管理员信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
            convertSet(list, purchaseReturn -> Long.parseLong(purchaseReturn.getCreator())));
        // 2. 开始拼接
        return BeanUtils.toBean(list, ErpPurchaseReturnBaseRespVO.class, purchaseReturn -> {
            purchaseReturn.setItems(BeanUtils.toBean(purchaseReturnItemMap.get(purchaseReturn.getId()), ErpPurchaseReturnBaseRespVO.Item.class,
                    item -> {
                        MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())
                                .setProductBarCode(product.getBarCode())
//                            .setProductUnitName(product.getUnitName())
                            .setProduct(product)
                        );
                    }
                )
            );
            purchaseReturn.setProductNames(CollUtil.join(purchaseReturn.getItems(), "，", ErpPurchaseReturnBaseRespVO.Item::getProductName));
            MapUtils.findAndThen(supplierMap, purchaseReturn.getSupplierId(), supplier -> purchaseReturn.setSupplierName(supplier.getName()));
            MapUtils.findAndThen(userMap, Long.parseLong(purchaseReturn.getCreator()), user -> purchaseReturn.setCreator(user.getNickname()));
        });
    }
}