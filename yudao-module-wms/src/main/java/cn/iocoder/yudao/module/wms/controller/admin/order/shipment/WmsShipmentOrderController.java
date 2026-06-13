package cn.iocoder.yudao.module.wms.controller.admin.order.shipment;

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
import cn.iocoder.yudao.module.wms.controller.admin.order.shipment.vo.detail.WmsShipmentOrderDetailRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.shipment.vo.order.WmsShipmentOrderPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.shipment.vo.order.WmsShipmentOrderRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.order.shipment.vo.order.WmsShipmentOrderSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemSkuDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.merchant.WmsMerchantDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.shipment.WmsShipmentOrderDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.shipment.WmsShipmentOrderDetailDO;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemService;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemSkuService;
import cn.iocoder.yudao.module.wms.service.md.merchant.WmsMerchantService;
import cn.iocoder.yudao.module.wms.service.md.warehouse.WmsWarehouseService;
import cn.iocoder.yudao.module.wms.service.order.shipment.WmsShipmentOrderDetailService;
import cn.iocoder.yudao.module.wms.service.order.shipment.WmsShipmentOrderService;
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

@Tag(name = "管理后台 - WMS 出库单")
@RestController
@RequestMapping("/wms/shipment-order")
@Validated
public class WmsShipmentOrderController {

    @Resource
    private WmsShipmentOrderService shipmentOrderService;
    @Resource
    private WmsShipmentOrderDetailService shipmentOrderDetailService;
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
    @Operation(summary = "创建出库单")
    @PreAuthorize("@ss.hasPermission('wms:shipment-order:create')")
    public CommonResult<Long> createShipmentOrder(@Valid @RequestBody WmsShipmentOrderSaveReqVO createReqVO) {
        return success(shipmentOrderService.createShipmentOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新出库单")
    @PreAuthorize("@ss.hasPermission('wms:shipment-order:update')")
    public CommonResult<Boolean> updateShipmentOrder(@Valid @RequestBody WmsShipmentOrderSaveReqVO updateReqVO) {
        shipmentOrderService.updateShipmentOrder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除出库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:shipment-order:delete')")
    public CommonResult<Boolean> deleteShipmentOrder(@RequestParam("id") Long id) {
        shipmentOrderService.deleteShipmentOrder(id);
        return success(true);
    }

    @PutMapping("/complete")
    @Operation(summary = "完成出库")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:shipment-order:complete')")
    public CommonResult<Boolean> completeShipmentOrder(@RequestParam("id") Long id) {
        shipmentOrderService.completeShipmentOrder(id);
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "作废出库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:shipment-order:cancel')")
    public CommonResult<Boolean> cancelShipmentOrder(@RequestParam("id") Long id) {
        shipmentOrderService.cancelShipmentOrder(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得出库单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:shipment-order:query')")
    public CommonResult<WmsShipmentOrderRespVO> getShipmentOrder(@RequestParam("id") Long id) {
        WmsShipmentOrderDO order = shipmentOrderService.getShipmentOrder(id);
        if (order == null) {
            return success(null);
        }
        // 获得出库单的明细列表
        List<WmsShipmentOrderDetailDO> detailList = shipmentOrderDetailService.getShipmentOrderDetailList(id);
        // 拼接结果返回
        WmsShipmentOrderRespVO respVO = buildShipmentOrderRespVO(order)
                .setDetails(buildShipmentOrderDetailRespVOList(detailList));
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得出库单分页")
    @PreAuthorize("@ss.hasPermission('wms:shipment-order:query')")
    public CommonResult<PageResult<WmsShipmentOrderRespVO>> getShipmentOrderPage(@Valid WmsShipmentOrderPageReqVO pageReqVO) {
        PageResult<WmsShipmentOrderDO> pageResult = shipmentOrderService.getShipmentOrderPage(pageReqVO);
        return success(new PageResult<>(buildShipmentOrderRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出出库单 Excel")
    @PreAuthorize("@ss.hasPermission('wms:shipment-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportShipmentOrderExcel(@Valid WmsShipmentOrderPageReqVO pageReqVO,
                                        HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsShipmentOrderDO> list = shipmentOrderService.getShipmentOrderPage(pageReqVO).getList();
        ExcelUtils.write(response, "出库单.xls", "数据", WmsShipmentOrderRespVO.class,
                buildShipmentOrderRespVOList(list));
    }

    // ==================== 拼接 VO ====================

    private WmsShipmentOrderRespVO buildShipmentOrderRespVO(WmsShipmentOrderDO order) {
        if (order == null) {
            return null;
        }
        List<WmsShipmentOrderRespVO> list = buildShipmentOrderRespVOList(Collections.singletonList(order));
        return CollUtil.getFirst(list);
    }

    private List<WmsShipmentOrderRespVO> buildShipmentOrderRespVOList(List<WmsShipmentOrderDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 获取相关的商户、仓库、用户等数据
        Map<Long, WmsMerchantDO> merchantMap = merchantService.getMerchantMap(convertSet(list, WmsShipmentOrderDO::getMerchantId));
        Map<Long, WmsWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(convertSet(list, WmsShipmentOrderDO::getWarehouseId));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSetByFlatMap(list,
                order -> Stream.of(parseUserId(order.getCreator()), parseUserId(order.getUpdater()))));
        // 拼接数据
        return BeanUtils.toBean(list, WmsShipmentOrderRespVO.class, vo -> {
            MapUtils.findAndThen(merchantMap, vo.getMerchantId(), merchant -> vo.setMerchantName(merchant.getName()));
            MapUtils.findAndThen(warehouseMap, vo.getWarehouseId(), warehouse -> vo.setWarehouseName(warehouse.getName()));
            MapUtils.findAndThen(userMap, parseUserId(vo.getCreator()), user -> vo.setCreatorName(user.getNickname()));
            MapUtils.findAndThen(userMap, parseUserId(vo.getUpdater()), user -> vo.setUpdaterName(user.getNickname()));
        });
    }

    private Long parseUserId(String userId) {
        return NumberUtil.parseLong(userId, null);
    }

    private List<WmsShipmentOrderDetailRespVO> buildShipmentOrderDetailRespVOList(List<WmsShipmentOrderDetailDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 获取相关的商品、SKU、仓库等数据
        Map<Long, WmsItemSkuDO> skuMap = itemSkuService.getItemSkuMap(convertSet(list, WmsShipmentOrderDetailDO::getSkuId));
        Map<Long, WmsItemDO> itemMap = itemService.getItemMap(convertSet(skuMap.values(), WmsItemSkuDO::getItemId));
        Map<Long, WmsWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(
                convertSet(list, WmsShipmentOrderDetailDO::getWarehouseId));
        // 拼接数据
        return BeanUtils.toBean(list, WmsShipmentOrderDetailRespVO.class, vo -> {
            MapUtils.findAndThen(skuMap, vo.getSkuId(), sku -> {
                vo.setSkuCode(sku.getCode()).setSkuName(sku.getName()).setItemId(sku.getItemId());
                MapUtils.findAndThen(itemMap, sku.getItemId(), item -> vo.setItemCode(item.getCode())
                        .setItemName(item.getName()).setUnit(item.getUnit()));
            });
            MapUtils.findAndThen(warehouseMap, vo.getWarehouseId(), warehouse -> vo.setWarehouseName(warehouse.getName()));
        });
    }

}
