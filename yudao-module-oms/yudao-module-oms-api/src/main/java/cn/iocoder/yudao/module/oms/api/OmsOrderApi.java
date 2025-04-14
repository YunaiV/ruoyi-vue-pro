package cn.iocoder.yudao.module.oms.api;

import cn.iocoder.yudao.module.oms.api.dto.OmsOrderSaveReqDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface OmsOrderApi {
    /**
     * @Description: 按平台创建或更新订单信息
     * @return:
     */
    void createOrUpdateOrderByPlatform(@Valid List<OmsOrderSaveReqDTO> saveReqDTOs);
}
