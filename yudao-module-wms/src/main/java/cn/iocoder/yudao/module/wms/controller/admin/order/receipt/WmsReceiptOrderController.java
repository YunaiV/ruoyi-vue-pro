package cn.iocoder.yudao.module.wms.controller.admin.order.receipt;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.wms.controller.admin.order.receipt.vo.detail.WmsReceiptOrderDetailRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.receipt.vo.order.WmsReceiptOrderPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.receipt.vo.order.WmsReceiptOrderRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.receipt.vo.order.WmsReceiptOrderSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemSkuDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.merchant.WmsMerchantDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.receipt.WmsReceiptOrderDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.receipt.WmsReceiptOrderDetailDO;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemService;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemSkuService;
import cn.iocoder.yudao.module.wms.service.md.merchant.WmsMerchantService;
import cn.iocoder.yudao.module.wms.service.md.warehouse.WmsWarehouseService;
import cn.iocoder.yudao.module.wms.service.order.receipt.WmsReceiptOrderDetailService;
import cn.iocoder.yudao.module.wms.service.order.receipt.WmsReceiptOrderService;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSetByFlatMap;

@Tag(name = "管理后台 - WMS 入库单")
@RestController
@RequestMapping("/wms/receipt-order")
@Validated
public class WmsReceiptOrderController {

    @Resource
    private WmsReceiptOrderService receiptOrderService;
    @Resource
    private WmsReceiptOrderDetailService receiptOrderDetailService;
    @Resource
    private WmsMerchantService merchantService;
    @Resource
    private WmsWarehouseService warehouseService;
    @Resource
    private WmsItemService itemService;
    @Resource
    private WmsItemSkuService itemSkuService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建入库单")
    @PreAuthorize("@ss.hasPermission('wms:receipt-order:create')")
    public CommonResult<Long> createReceiptOrder(@Valid @RequestBody WmsReceiptOrderSaveReqVO createReqVO) {
        return success(receiptOrderService.createReceiptOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新入库单")
    @PreAuthorize("@ss.hasPermission('wms:receipt-order:update')")
    public CommonResult<Boolean> updateReceiptOrder(@Valid @RequestBody WmsReceiptOrderSaveReqVO updateReqVO) {
        receiptOrderService.updateReceiptOrder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除入库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:receipt-order:delete')")
    public CommonResult<Boolean> deleteReceiptOrder(@RequestParam("id") Long id) {
        receiptOrderService.deleteReceiptOrder(id);
        return success(true);
    }

    @PutMapping("/complete")
    @Operation(summary = "完成入库")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:receipt-order:complete')")
    public CommonResult<Boolean> completeReceiptOrder(@RequestParam("id") Long id) {
        receiptOrderService.completeReceiptOrder(id);
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "作废入库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:receipt-order:cancel')")
    public CommonResult<Boolean> cancelReceiptOrder(@RequestParam("id") Long id) {
        receiptOrderService.cancelReceiptOrder(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得入库单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:receipt-order:query')")
    public CommonResult<WmsReceiptOrderRespVO> getReceiptOrder(@RequestParam("id") Long id) {
        WmsReceiptOrderDO order = receiptOrderService.getReceiptOrder(id);
        if (order == null) {
            return success(null);
        }
        // 获得入库单的明细列表
        List<WmsReceiptOrderDetailDO> detailList = receiptOrderDetailService.getReceiptOrderDetailList(id);
        // 拼接结果返回
        WmsReceiptOrderRespVO respVO = buildReceiptOrderRespVO(order)
                .setDetails(buildReceiptOrderDetailRespVOList(detailList));
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得入库单分页")
    @PreAuthorize("@ss.hasPermission('wms:receipt-order:query')")
    public CommonResult<PageResult<WmsReceiptOrderRespVO>> getReceiptOrderPage(@Valid WmsReceiptOrderPageReqVO pageReqVO) {
        PageResult<WmsReceiptOrderDO> pageResult = receiptOrderService.getReceiptOrderPage(pageReqVO);
        return success(new PageResult<>(buildReceiptOrderRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出入库单 Excel")
    @PreAuthorize("@ss.hasPermission('wms:receipt-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportReceiptOrderExcel(@Valid WmsReceiptOrderPageReqVO pageReqVO,
                                        HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsReceiptOrderDO> list = receiptOrderService.getReceiptOrderPage(pageReqVO).getList();
        ExcelUtils.write(response, "入库单.xls", "数据", WmsReceiptOrderRespVO.class,
                buildReceiptOrderRespVOList(list));
    }

    // ==================== 拼接 VO ====================

    private WmsReceiptOrderRespVO buildReceiptOrderRespVO(WmsReceiptOrderDO order) {
        if (order == null) {
            return null;
        }
        List<WmsReceiptOrderRespVO> list = buildReceiptOrderRespVOList(Collections.singletonList(order));
        return CollUtil.getFirst(list);
    }

    private List<WmsReceiptOrderRespVO> buildReceiptOrderRespVOList(List<WmsReceiptOrderDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 获取相关的商户、仓库、用户等数据
        Map<Long, WmsMerchantDO> merchantMap = merchantService.getMerchantMap(convertSet(list, WmsReceiptOrderDO::getMerchantId));
        Map<Long, WmsWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(convertSet(list, WmsReceiptOrderDO::getWarehouseId));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSetByFlatMap(list,
                order -> Stream.of(parseUserId(order.getCreator()), parseUserId(order.getUpdater()))));
        // 拼接数据
        return BeanUtils.toBean(list, WmsReceiptOrderRespVO.class, vo -> {
            MapUtils.findAndThen(merchantMap, vo.getMerchantId(), merchant -> vo.setMerchantName(merchant.getName()));
            MapUtils.findAndThen(warehouseMap, vo.getWarehouseId(), warehouse -> vo.setWarehouseName(warehouse.getName()));
            MapUtils.findAndThen(userMap, parseUserId(vo.getCreator()), user -> vo.setCreatorName(user.getNickname()));
            MapUtils.findAndThen(userMap, parseUserId(vo.getUpdater()), user -> vo.setUpdaterName(user.getNickname()));
        });
    }

    private Long parseUserId(String userId) {
        return NumberUtil.parseLong(userId, null);
    }

    private List<WmsReceiptOrderDetailRespVO> buildReceiptOrderDetailRespVOList(List<WmsReceiptOrderDetailDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 获取相关的商品、SKU、仓库等数据
        Map<Long, WmsItemSkuDO> skuMap = itemSkuService.getItemSkuMap(convertSet(list, WmsReceiptOrderDetailDO::getSkuId));
        Map<Long, WmsItemDO> itemMap = itemService.getItemMap(convertSet(skuMap.values(), WmsItemSkuDO::getItemId));
        Map<Long, WmsWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(
                convertSet(list, WmsReceiptOrderDetailDO::getWarehouseId));
        // 拼接数据
        return BeanUtils.toBean(list, WmsReceiptOrderDetailRespVO.class, vo -> {
            MapUtils.findAndThen(skuMap, vo.getSkuId(), sku -> {
                vo.setSkuCode(sku.getCode()).setSkuName(sku.getName()).setItemId(sku.getItemId());
                MapUtils.findAndThen(itemMap, sku.getItemId(), item -> vo.setItemCode(item.getCode())
                        .setItemName(item.getName()).setUnit(item.getUnit()));
            });
            MapUtils.findAndThen(warehouseMap, vo.getWarehouseId(), warehouse -> vo.setWarehouseName(warehouse.getName()));
        });
    }

}
