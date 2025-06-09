package cn.iocoder.yudao.module.srm.api.purchase;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.srm.api.purchase.dto.SrmPurchaseOrderDTO;
import cn.iocoder.yudao.module.srm.api.purchase.dto.SrmPurchaseOrderItemDTO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.service.purchase.SrmPurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 采购订单 API 实现类
 */
@Service
@Validated
public class SrmPurchaseOrderApiImpl implements SrmPurchaseOrderApi {

    @Autowired
    @Lazy
    private SrmPurchaseOrderService purchaseOrderService;

    @Override
    public List<SrmPurchaseOrderDTO> getPurchaseOrderList(List<Long> ids) {
        // 1. 获取采购订单列表
        Collection<SrmPurchaseOrderDO> orders = purchaseOrderService.getPurchaseOrderList(ids);
        if (orders.isEmpty()) {
            return List.of();
        }

        // 2. 获取订单明细列表
        Map<Long, List<SrmPurchaseOrderItemDO>> orderItemMap = purchaseOrderService.getPurchaseOrderItemListByOrderIds(
                        orders.stream().map(SrmPurchaseOrderDO::getId).collect(Collectors.toList()))
                .stream().collect(Collectors.groupingBy(SrmPurchaseOrderItemDO::getOrderId));

        // 3. 转换为 DTO 对象
        return orders.stream().map(order -> {
            SrmPurchaseOrderDTO dto = BeanUtils.toBean(order, SrmPurchaseOrderDTO.class);
            // 设置订单明细
            List<SrmPurchaseOrderItemDO> orderItems = orderItemMap.getOrDefault(order.getId(), List.of());
            dto.setItems(orderItems.stream().map(this::convertOrderItem).collect(Collectors.toList()));
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 转换订单明细
     *
     * @param item 订单明细 DO
     * @return 订单明细 DTO
     */
    private SrmPurchaseOrderItemDTO convertOrderItem(SrmPurchaseOrderItemDO item) {
        if (item == null) {
            return null;
        }
        SrmPurchaseOrderItemDTO dto = BeanUtils.toBean(item, SrmPurchaseOrderItemDTO.class);
        // 特殊字段映射
        dto.setMaterialId(item.getProductId());
//        dto.setMaterialCode(item.getProductCode());
        dto.setMaterialName(item.getProductName());
        dto.setUnit(item.getProductUnitName());
        // 将 LocalDateTime 转换为 yyyy-MM-dd 格式的字符串
        dto.setDeliveryDate(item.getDeliveryTime() != null ? item.getDeliveryTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null);
        return dto;
    }
} 