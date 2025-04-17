package cn.iocoder.yudao.module.oms.api;

import cn.iocoder.yudao.module.oms.api.dto.OmsOrderDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsOrderSaveReqDTO;
import jakarta.validation.Valid;

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
}
