package cn.iocoder.yudao.module.wms.controller.admin.inventory;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.history.WmsInventoryHistoryPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.history.WmsInventoryHistoryRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryHistoryDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemSkuDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.service.inventory.WmsInventoryHistoryService;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemService;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemSkuService;
import cn.iocoder.yudao.module.wms.service.md.warehouse.WmsWarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - WMS 库存流水")
@RestController
@RequestMapping("/wms/inventory-history")
@Validated
public class WmsInventoryHistoryController {

    @Resource
    private WmsInventoryHistoryService inventoryHistoryService;
    @Resource
    private WmsItemService itemService;
    @Resource
    private WmsItemSkuService itemSkuService;
    @Resource
    private WmsWarehouseService warehouseService;

    @GetMapping("/page")
    @Operation(summary = "获得库存流水分页")
    @PreAuthorize("@ss.hasPermission('wms:inventory-history:query')")
    public CommonResult<PageResult<WmsInventoryHistoryRespVO>> getInventoryHistoryPage(
            @Valid WmsInventoryHistoryPageReqVO pageReqVO) {
        PageResult<WmsInventoryHistoryDO> pageResult = inventoryHistoryService.getInventoryHistoryPage(pageReqVO);
        return success(new PageResult<>(buildInventoryHistoryRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    // ==================== 拼接 VO ====================

    private List<WmsInventoryHistoryRespVO> buildInventoryHistoryRespVOList(List<WmsInventoryHistoryDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        Map<Long, WmsItemSkuDO> skuMap = itemSkuService.getItemSkuMap(convertSet(list, WmsInventoryHistoryDO::getSkuId));
        Map<Long, WmsItemDO> itemMap = itemService.getItemMap(convertSet(skuMap.values(), WmsItemSkuDO::getItemId));
        Map<Long, WmsWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(
                convertSet(list, WmsInventoryHistoryDO::getWarehouseId));
        return BeanUtils.toBean(list, WmsInventoryHistoryRespVO.class, vo -> {
            MapUtils.findAndThen(skuMap, vo.getSkuId(), sku -> {
                vo.setSkuCode(sku.getCode()).setSkuName(sku.getName()).setItemId(sku.getItemId());
                MapUtils.findAndThen(itemMap, sku.getItemId(), item -> vo.setItemCode(item.getCode())
                        .setItemName(item.getName()).setUnit(item.getUnit()));
            });
            MapUtils.findAndThen(warehouseMap, vo.getWarehouseId(), warehouse -> vo.setWarehouseName(warehouse.getName()));
        });
    }

}
