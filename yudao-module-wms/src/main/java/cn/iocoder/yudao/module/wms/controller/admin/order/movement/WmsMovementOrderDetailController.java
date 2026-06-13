package cn.iocoder.yudao.module.wms.controller.admin.order.movement;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.order.movement.vo.detail.WmsMovementOrderDetailRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemSkuDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.movement.WmsMovementOrderDetailDO;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemService;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemSkuService;
import cn.iocoder.yudao.module.wms.service.md.warehouse.WmsWarehouseService;
import cn.iocoder.yudao.module.wms.service.order.movement.WmsMovementOrderDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSetByFlatMap;

@Tag(name = "管理后台 - WMS 移库单明细")
@RestController
@RequestMapping("/wms/movement-order-detail")
@Validated
public class WmsMovementOrderDetailController {

    @Resource
    private WmsMovementOrderDetailService movementOrderDetailService;
    @Resource
    private WmsItemService itemService;
    @Resource
    private WmsItemSkuService itemSkuService;
    @Resource
    private WmsWarehouseService warehouseService;

    @GetMapping("/list-by-order-id")
    @Operation(summary = "获得移库单明细列表")
    @Parameter(name = "orderId", description = "移库单编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:movement-order:query')")
    public CommonResult<List<WmsMovementOrderDetailRespVO>> getMovementOrderDetailListByOrderId(
            @RequestParam("orderId") Long orderId) {
        List<WmsMovementOrderDetailDO> list = movementOrderDetailService.getMovementOrderDetailList(orderId);
        return success(buildMovementOrderDetailRespVOList(list));
    }

    // ==================== 拼接 VO ====================

    private List<WmsMovementOrderDetailRespVO> buildMovementOrderDetailRespVOList(List<WmsMovementOrderDetailDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 查询关联数据
        Map<Long, WmsItemSkuDO> skuMap = itemSkuService.getItemSkuMap(convertSet(list, WmsMovementOrderDetailDO::getSkuId));
        Map<Long, WmsItemDO> itemMap = itemService.getItemMap(convertSet(skuMap.values(), WmsItemSkuDO::getItemId));
        Map<Long, WmsWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(convertSetByFlatMap(list,
                detail -> Stream.of(detail.getSourceWarehouseId(), detail.getTargetWarehouseId())));
        // 拼接数据
        return BeanUtils.toBean(list, WmsMovementOrderDetailRespVO.class, vo -> {
            MapUtils.findAndThen(skuMap, vo.getSkuId(), sku -> {
                vo.setSkuCode(sku.getCode()).setSkuName(sku.getName()).setItemId(sku.getItemId());
                MapUtils.findAndThen(itemMap, sku.getItemId(), item -> vo.setItemCode(item.getCode())
                        .setItemName(item.getName()).setUnit(item.getUnit()));
            });
            MapUtils.findAndThen(warehouseMap, vo.getSourceWarehouseId(),
                    warehouse -> vo.setSourceWarehouseName(warehouse.getName()));
            MapUtils.findAndThen(warehouseMap, vo.getTargetWarehouseId(),
                    warehouse -> vo.setTargetWarehouseName(warehouse.getName()));
        });
    }

}
