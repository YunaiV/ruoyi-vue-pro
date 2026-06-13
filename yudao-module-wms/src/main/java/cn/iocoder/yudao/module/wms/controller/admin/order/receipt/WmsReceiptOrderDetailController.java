package cn.iocoder.yudao.module.wms.controller.admin.order.receipt;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.order.receipt.vo.detail.WmsReceiptOrderDetailRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemSkuDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.order.receipt.WmsReceiptOrderDetailDO;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemService;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemSkuService;
import cn.iocoder.yudao.module.wms.service.md.warehouse.WmsWarehouseService;
import cn.iocoder.yudao.module.wms.service.order.receipt.WmsReceiptOrderDetailService;
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

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - WMS 入库单明细")
@RestController
@RequestMapping("/wms/receipt-order-detail")
@Validated
public class WmsReceiptOrderDetailController {

    @Resource
    private WmsReceiptOrderDetailService receiptOrderDetailService;
    @Resource
    private WmsItemService itemService;
    @Resource
    private WmsItemSkuService itemSkuService;
    @Resource
    private WmsWarehouseService warehouseService;

    @GetMapping("/list-by-order-id")
    @Operation(summary = "获得入库单明细列表")
    @Parameter(name = "orderId", description = "入库单编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:receipt-order:query')")
    public CommonResult<List<WmsReceiptOrderDetailRespVO>> getReceiptOrderDetailListByOrderId(
            @RequestParam("orderId") Long orderId) {
        List<WmsReceiptOrderDetailDO> list = receiptOrderDetailService.getReceiptOrderDetailList(orderId);
        return success(buildReceiptOrderDetailRespVOList(list));
    }

    // ==================== 拼接 VO ====================

    private List<WmsReceiptOrderDetailRespVO> buildReceiptOrderDetailRespVOList(List<WmsReceiptOrderDetailDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 查询关联数据
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
