package cn.iocoder.yudao.module.oms.api;

import cn.iocoder.yudao.module.oms.api.dto.OmsOrderDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsOrderItemDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsOrderSaveReqDTO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

public interface OmsOrderApi {
    /**
     * @Description: 按平台创建或更新订单信息
     * @return:
     */
    void createOrUpdateOrderByPlatform(@Valid List<OmsOrderSaveReqDTO> saveReqDTOs);


    /**
     * @param platformCode 平台代码
     * @Description: 根据平台获取订单列表
     * @return: @return {@link List }<{@link OmsOrderDTO }>
     */
    List<OmsOrderDTO> getByPlatformCode(String platformCode);

    /**
     * 获得销售订单项 List
     *
     * @param orderIds 销售订单编号数组
     * @return 销售订单项 List
     */
    List<OmsOrderItemDTO> getOrderItemListByOrderIds(Collection<Long> orderIds);

    /**
     * 批量更新订单集合
     */
    public void updateOrders(List<OmsOrderDTO> updateOrders);
}
