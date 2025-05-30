package cn.iocoder.yudao.module.oms.api;

import cn.iocoder.yudao.module.oms.api.dto.OmsOrderItemDTO;

import java.util.List;

public interface OmsOrderItemApi {

    /**
     * 批量更新订单项集合
     */
    public void updateOrderItems(List<OmsOrderItemDTO> updateOrderItems);
}
