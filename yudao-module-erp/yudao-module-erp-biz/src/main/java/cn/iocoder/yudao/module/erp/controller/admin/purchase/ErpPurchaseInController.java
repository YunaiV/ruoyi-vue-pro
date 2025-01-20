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
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInBaseRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseInService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpSupplierService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockService;
import cn.iocoder.yudao.module.erp.service.stock.ErpWarehouseService;
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

@Tag(name = "管理后台 - ERP 采购入库")
@RestController
@RequestMapping("/erp/purchase-in")
@Validated
public class ErpPurchaseInController {

    @Resource
    private ErpPurchaseInService purchaseInService;
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

    @PostMapping("/create")
    @Operation(summary = "创建采购入库")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:create')")
    public CommonResult<Long> createPurchaseIn(@Valid @RequestBody ErpPurchaseInSaveReqVO createReqVO) {
        return success(purchaseInService.createPurchaseIn(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新采购入库")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:update')")
    public CommonResult<Boolean> updatePurchaseIn(@Valid @RequestBody ErpPurchaseInSaveReqVO updateReqVO) {
        purchaseInService.updatePurchaseIn(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新采购入库的状态")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:update-status')")
    public CommonResult<Boolean> updatePurchaseInStatus(@RequestParam("id") Long id,
                                                      @RequestParam("status") Integer status) {
        purchaseInService.updatePurchaseInStatus(id, status);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除采购入库")
    @Parameter(name = "ids", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:delete')")
    public CommonResult<Boolean> deletePurchaseIn(@RequestParam("ids") List<Long> ids) {
        purchaseInService.deletePurchaseIn(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得采购入库")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:query')")
    public CommonResult<ErpPurchaseInBaseRespVO> getPurchaseIn(@RequestParam("id") Long id) {
        ErpPurchaseInDO purchaseIn = purchaseInService.getPurchaseIn(id);
        if (purchaseIn == null) {
            return success(null);
        }
        List<ErpPurchaseInItemDO> purchaseInItemList = purchaseInService.getPurchaseInItemListByInId(id);
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(purchaseInItemList, ErpPurchaseInItemDO::getProductId));
        return success(BeanUtils.toBean(purchaseIn, ErpPurchaseInBaseRespVO.class, purchaseInVO ->
                purchaseInVO.setItems(BeanUtils.toBean(purchaseInItemList, ErpPurchaseInBaseRespVO.Item.class, item -> {
                    ErpStockDO stock = stockService.getStock(item.getProductId(), item.getWarehouseId());
                    item.setStockCount(stock != null ? stock.getCount() : BigDecimal.ZERO);
                    MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())
                            .setProductBarCode(product.getBarCode()).setProductUnitName(product.getUnitName()));
                }))));
    }

    @GetMapping("/page")
    @Operation(summary = "获得采购入库分页")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:query')")
    public CommonResult<PageResult<ErpPurchaseInBaseRespVO>> getPurchaseInPage(@Valid ErpPurchaseInPageReqVO pageReqVO) {
        PageResult<ErpPurchaseInDO> pageResult = purchaseInService.getPurchaseInPage(pageReqVO);
        return success(buildPurchaseInVOPageResult(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出采购入库 Excel")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPurchaseInExcel(@Valid ErpPurchaseInPageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpPurchaseInBaseRespVO> list = buildPurchaseInVOPageResult(purchaseInService.getPurchaseInPage(pageReqVO)).getList();
        // 导出 Excel
        ExcelUtils.write(response, "采购入库.xls", "数据", ErpPurchaseInBaseRespVO.class, list);
    }

    private PageResult<ErpPurchaseInBaseRespVO> buildPurchaseInVOPageResult(PageResult<ErpPurchaseInDO> pageResult) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        // 1.1 入库项
        List<ErpPurchaseInItemDO> purchaseInItemList = purchaseInService.getPurchaseInItemListByInIds(
                convertSet(pageResult.getList(), ErpPurchaseInDO::getId));
        Map<Long, List<ErpPurchaseInItemDO>> purchaseInItemMap = convertMultiMap(purchaseInItemList, ErpPurchaseInItemDO::getInId);
        // 1.2 产品信息
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
                convertSet(purchaseInItemList, ErpPurchaseInItemDO::getProductId));
        // 1.3 供应商信息
        Map<Long, ErpSupplierDO> supplierMap = supplierService.getSupplierMap(
                convertSet(pageResult.getList(), ErpPurchaseInDO::getSupplierId));
        // 1.4 管理员信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(pageResult.getList(), purchaseIn -> Long.parseLong(purchaseIn.getCreator())));
        // 1.6 获取仓库信息
        Map<Long, ErpWarehouseDO> warehouseMap = erpWarehouseService.getWarehouseMap(
            convertSet(purchaseInItemList, ErpPurchaseInItemDO::getWarehouseId));
        // 2. 开始拼接
        return BeanUtils.toBean(pageResult, ErpPurchaseInBaseRespVO.class, purchaseIn -> {
            purchaseIn.setItems(BeanUtils.toBean(purchaseInItemMap.get(purchaseIn.getId()), ErpPurchaseInBaseRespVO.Item.class,
                item -> {
                    //设置产品信息-带出相关字段
                    MapUtils.findAndThen(productMap, item.getProductId(), product -> item.setProductName(product.getName())
                        .setProductBarCode(product.getBarCode())
                        .setProductUnitName(product.getUnitName())//设置产品重量+体积+规格型号
                        .setTotalVolume(product.getLength()*product.getHeight()*product.getWeight() * Double.parseDouble(String.valueOf(item.getCount())))
                        .setTotalWeight(product.getWeight()*Double.parseDouble(String.valueOf(item.getCount())))
                        .setModel(product.getModel())
                        .setCustomsDeclaration(product.getBrand())//产品品牌
                    );
                    // 设置仓库信息
                    MapUtils.findAndThen(warehouseMap, item.getWarehouseId(), erpWarehouseDO -> item.setWarehouseName(erpWarehouseDO.getName()));
                }));
//            purchaseIn.setProductNames(CollUtil.join(purchaseIn.getItems(), "，", ErpPurchaseInBaseRespVO.Item::getProductName));
            //TODO 添加入库审核状态描述
//            String statusDescription = ErpAuditStatus.getDescriptionByStatus(purchaseIn.getAuditorStatus());
//            if (statusDescription != null) {
//                purchaseIn.setAuditorStatusDesc(statusDescription);
//            }
            //产品-带出相关字段

            MapUtils.findAndThen(supplierMap, purchaseIn.getSupplierId(), supplier -> purchaseIn.setSupplierName(supplier.getName()));
            MapUtils.findAndThen(userMap, Long.parseLong(purchaseIn.getCreator()), user -> purchaseIn.setCreator(user.getNickname()));
        });
    }

}