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
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseReturnItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseReturnService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpSupplierService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
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
public class ErpPurchaseReturnController {

    @Resource
    private ErpPurchaseReturnService purchaseReturnService;
    @Resource
    private ErpStockService stockService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpSupplierService supplierService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建采购退货")
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

    @PutMapping("/update-status")
    @Operation(summary = "更新采购退货的状态")
    @PreAuthorize("@ss.hasPermission('erp:purchase-return:update-status')")
    public CommonResult<Boolean> updatePurchaseReturnStatus(@RequestParam("id") Long id,
                                                      @RequestParam("status") Integer status) {
        purchaseReturnService.updatePurchaseReturnStatus(id, status);
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
    public CommonResult<ErpPurchaseReturnRespVO> getPurchaseReturn(@RequestParam("id") Long id) {
        ErpPurchaseReturnDO purchaseReturn = purchaseReturnService.getPurchaseReturn(id);
        if (purchaseReturn == null) {
            return success(null);
        }
        List<ErpPurchaseReturnItemDO> purchaseReturnItemList = purchaseReturnService.getPurchaseReturnItemListByReturnId(id);
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(purchaseReturnItemList, ErpPurchaseReturnItemDO::getProductId));
        return success(BeanUtils.toBean(purchaseReturn, ErpPurchaseReturnRespVO.class, purchaseReturnVO ->
                purchaseReturnVO.setItems(BeanUtils.toBean(purchaseReturnItemList, ErpPurchaseReturnRespVO.Item.class, item -> {
                    ErpStockDO stock = stockService.getStock(item.getProductId(), item.getWarehouseId());
                    item.setStockCount(stock != null ? stock.getCount() : BigDecimal.ZERO);
                    MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())
                            .setProductBarCode(product.getBarCode()).setProductUnitName(product.getUnitName()));
                }))));
    }

    @GetMapping("/page")
    @Operation(summary = "获得采购退货分页")
    @PreAuthorize("@ss.hasPermission('erp:purchase-return:query')")
    public CommonResult<PageResult<ErpPurchaseReturnRespVO>> getPurchaseReturnPage(@Valid ErpPurchaseReturnPageReqVO pageReqVO) {
        PageResult<ErpPurchaseReturnDO> pageResult = purchaseReturnService.getPurchaseReturnPage(pageReqVO);
        return success(buildPurchaseReturnVOPageResult(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出采购退货 Excel")
    @PreAuthorize("@ss.hasPermission('erp:purchase-return:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPurchaseReturnExcel(@Valid ErpPurchaseReturnPageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpPurchaseReturnRespVO> list = buildPurchaseReturnVOPageResult(purchaseReturnService.getPurchaseReturnPage(pageReqVO)).getList();
        // 导出 Excel
        ExcelUtils.write(response, "采购退货.xls", "数据", ErpPurchaseReturnRespVO.class, list);
    }

    private PageResult<ErpPurchaseReturnRespVO> buildPurchaseReturnVOPageResult(PageResult<ErpPurchaseReturnDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        // 1.1 退货项
        List<ErpPurchaseReturnItemDO> purchaseReturnItemList = purchaseReturnService.getPurchaseReturnItemListByReturnIds(
                convertSet(pageResult.getList(), ErpPurchaseReturnDO::getId));
        Map<Long, List<ErpPurchaseReturnItemDO>> purchaseReturnItemMap = convertMultiMap(purchaseReturnItemList, ErpPurchaseReturnItemDO::getReturnId);
        // 1.2 产品信息
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(purchaseReturnItemList, ErpPurchaseReturnItemDO::getProductId));
        // 1.3 供应商信息
        Map<Long, ErpSupplierDO> supplierMap = supplierService.getSupplierMap(
                convertSet(pageResult.getList(), ErpPurchaseReturnDO::getSupplierId));
        // 1.4 管理员信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(pageResult.getList(), purchaseReturn -> Long.parseLong(purchaseReturn.getCreator())));
        // 2. 开始拼接
        return BeanUtils.toBean(pageResult, ErpPurchaseReturnRespVO.class, purchaseReturn -> {
            purchaseReturn.setItems(BeanUtils.toBean(purchaseReturnItemMap.get(purchaseReturn.getId()), ErpPurchaseReturnRespVO.Item.class,
                    item -> MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())
                            .setProductBarCode(product.getBarCode()).setProductUnitName(product.getUnitName()))));
            purchaseReturn.setProductNames(CollUtil.join(purchaseReturn.getItems(), "，", ErpPurchaseReturnRespVO.Item::getProductName));
            MapUtils.findAndThen(supplierMap, purchaseReturn.getSupplierId(), supplier -> purchaseReturn.setSupplierName(supplier.getName()));
            MapUtils.findAndThen(userMap, Long.parseLong(purchaseReturn.getCreator()), user -> purchaseReturn.setCreatorName(user.getNickname()));
        });
    }

}