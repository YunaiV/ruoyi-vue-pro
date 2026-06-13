package cn.iocoder.yudao.module.wms.controller.admin.inventory;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventoryListReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventoryPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventoryRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemSkuDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.service.inventory.WmsInventoryService;
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

@Tag(name = "管理后台 - WMS 库存统计")
@RestController
@RequestMapping("/wms/inventory")
@Validated
public class WmsInventoryController {

    @Resource
    private WmsInventoryService inventoryService;
    @Resource
    private WmsItemService itemService;
    @Resource
    private WmsItemSkuService itemSkuService;
    @Resource
    private WmsWarehouseService warehouseService;

    @GetMapping("/page")
    @Operation(summary = "获得库存统计分页")
    @PreAuthorize("@ss.hasPermission('wms:inventory:query')")
    public CommonResult<PageResult<WmsInventoryRespVO>> getInventoryPage(@Valid WmsInventoryPageReqVO pageReqVO) {
        PageResult<WmsInventoryDO> pageResult = inventoryService.getInventoryPage(pageReqVO);
        return success(new PageResult<>(buildInventoryRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/list")
    @Operation(summary = "获得库存统计列表")
    @PreAuthorize("@ss.hasPermission('wms:inventory:query')")
    public CommonResult<List<WmsInventoryRespVO>> getInventoryList(@Valid WmsInventoryListReqVO listReqVO) {
        List<WmsInventoryDO> list = inventoryService.getInventoryList(listReqVO);
        return success(buildInventoryRespVOList(list));
    }

    // ==================== 拼接 VO ====================

    private List<WmsInventoryRespVO> buildInventoryRespVOList(List<WmsInventoryDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        Map<Long, WmsItemSkuDO> skuMap = itemSkuService.getItemSkuMap(convertSet(list, WmsInventoryDO::getSkuId));
        Map<Long, WmsItemDO> itemMap = itemService.getItemMap(convertSet(skuMap.values(), WmsItemSkuDO::getItemId));
        Map<Long, WmsWarehouseDO> warehouseMap = warehouseService.getWarehouseMap(
                convertSet(list, WmsInventoryDO::getWarehouseId));
        return BeanUtils.toBean(list, WmsInventoryRespVO.class, vo -> {
            MapUtils.findAndThen(skuMap, vo.getSkuId(), sku -> {
                vo.setSkuCode(sku.getCode()).setSkuName(sku.getName()).setItemId(sku.getItemId());
                MapUtils.findAndThen(itemMap, sku.getItemId(), item -> vo.setItemCode(item.getCode())
                        .setItemName(item.getName()).setUnit(item.getUnit()));
            });
            MapUtils.findAndThen(warehouseMap, vo.getWarehouseId(), warehouse -> vo.setWarehouseName(warehouse.getName()));
        });
    }

}
