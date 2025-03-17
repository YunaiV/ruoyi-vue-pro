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
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInAuditReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInBaseRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.tools.validation;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO;
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
import java.math.RoundingMode;
import java.util.Collections;
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
        //给vo里面的项的source设置字符串a
        createReqVO.getItems().forEach(item -> item.setSource("WEB录入"));
        return success(purchaseInService.createPurchaseIn(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新采购入库")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:update')")
    public CommonResult<Boolean> updatePurchaseIn(@Valid @RequestBody ErpPurchaseInSaveReqVO updateReqVO) {
        purchaseInService.updatePurchaseIn(updateReqVO);
        return success(true);
    }

//    @PutMapping("/update-status")
//    @Operation(summary = "更新采购入库的状态")
//    @PreAuthorize("@ss.hasPermission('erp:purchase-in:update-status')")
//    public CommonResult<Boolean> updatePurchaseInStatus(@RequestParam("id") Long id,
//                                                        @RequestParam("status") Integer status) {
//        purchaseInService.updatePurchaseInStatus(id, status);
//        return success(true);
//    }

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
        ErpPurchaseInBaseRespVO respVO = bindList(Collections.singletonList(purchaseIn)).get(0);
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得采购入库分页")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:query')")
    public CommonResult<PageResult<ErpPurchaseInBaseRespVO>> getPurchaseInPage(@Valid ErpPurchaseInPageReqVO pageReqVO) {
        PageResult<ErpPurchaseInDO> pageResult = purchaseInService.getPurchaseInPage(pageReqVO);
        List<ErpPurchaseInBaseRespVO> respVOS = bindList(pageResult.getList());
        return success(new PageResult<>(respVOS, pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出采购入库 Excel")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPurchaseInExcel(@Valid ErpPurchaseInPageReqVO pageReqVO,
                                      HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<ErpPurchaseInDO> page = purchaseInService.getPurchaseInPage(pageReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "采购入库.xls", "数据", ErpPurchaseInBaseRespVO.class, bindList(page.getList()));
    }

    @PostMapping("/submitAudit")
    @Operation(summary = "提交审核")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:submitAudit')")
    public CommonResult<Boolean> submitAudit(@Validated @RequestBody ErpPurchaseInAuditReqVO reqVO) {
        purchaseInService.submitAudit(reqVO.getInId());
        return success(true);
    }

    @PostMapping("/auditStatus")
    @Operation(summary = "审核/反审核")
    @PreAuthorize("@ss.hasPermission('erp:purchase-in:review')")
    public CommonResult<Boolean> reviewPurchaseRequest(@Validated(validation.OnAudit.class) @RequestBody ErpPurchaseInAuditReqVO req) {
        purchaseInService.review(req);
        return success(true);
    }

    private List<ErpPurchaseInBaseRespVO> bindList(List<ErpPurchaseInDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1.1 入库项
        List<ErpPurchaseInItemDO> purchaseInItemList = purchaseInService.getPurchaseInItemListByInIds(
            convertSet(list, ErpPurchaseInDO::getId));
        Map<Long, List<ErpPurchaseInItemDO>> purchaseInItemMap = convertMultiMap(purchaseInItemList, ErpPurchaseInItemDO::getInId);
        // 1.2 产品信息
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(
            convertSet(purchaseInItemList, ErpPurchaseInItemDO::getProductId));
        // 1.3 供应商信息
        Map<Long, ErpSupplierDO> supplierMap = supplierService.getSupplierMap(
            convertSet(list, ErpPurchaseInDO::getSupplierId));
        // 1.4 管理员信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
            convertSet(list, purchaseIn -> Long.parseLong(purchaseIn.getCreator())));
        // 1.6 获取仓库信息
        Map<Long, ErpWarehouseDO> warehouseMap = erpWarehouseService.getWarehouseMap(
            convertSet(purchaseInItemList, ErpPurchaseInItemDO::getWarehouseId));
        // 2. 开始拼接
        return BeanUtils.toBean(list, ErpPurchaseInBaseRespVO.class, purchaseIn -> {
            purchaseIn.setItems(BeanUtils.toBean(purchaseInItemMap.get(purchaseIn.getId()), ErpPurchaseInBaseRespVO.Item.class,
                item -> {
                    //设置产品信息-带出相关字段
                    MapUtils.findAndThen(productMap, item.getProductId(), product -> item
                        .setProduct(product)
                        .setTotalVolume(product.getLength() * product.getHeight() * product.getWidth() * Double.parseDouble(String.valueOf(item.getCount())))//总体积=数量*产品体积
                        .setTotalWeight(product.getWeight().setScale(2, RoundingMode.HALF_UP).longValue() * Double.parseDouble(String.valueOf(item.getCount())))//总重量=数量*产品重量
                    );
                    // 设置仓库信息
                    MapUtils.findAndThen(warehouseMap, item.getWarehouseId(), erpWarehouseDO -> item.setWarehouseName(erpWarehouseDO.getName()));
                }));
//            purchaseIn.setProductNames(CollUtil.join(purchaseIn.getItems(), "，", ErpPurchaseInBaseRespVO.Item::getProductName));
            //产品-带出相关字段

            MapUtils.findAndThen(supplierMap, purchaseIn.getSupplierId(), supplier -> purchaseIn.setSupplierName(supplier.getName()));
            MapUtils.findAndThen(userMap, Long.parseLong(purchaseIn.getCreator()), user -> purchaseIn.setCreator(user.getNickname()));
        });
    }

}