package cn.iocoder.yudao.module.oms.api;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oms.api.dto.OmsOrderDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsOrderItemDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsOrderSaveReqDTO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsOrderDO;
import cn.iocoder.yudao.module.oms.service.OmsOrderService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class OmsOrderApiImpl implements OmsOrderApi {

    @Resource
    private OmsOrderService omsOrderService;

    @Override
    public void createOrUpdateOrderByPlatform(List<OmsOrderSaveReqDTO> saveReqDTOs) {
        omsOrderService.createOrUpdateOrderByPlatform(saveReqDTOs);
    }

    @Override
    public List<OmsOrderDTO> getByPlatformCode(String platformCode) {
        return BeanUtils.toBean(omsOrderService.getByPlatformCode(platformCode), OmsOrderDTO.class);
    }

    @Override
    public List<OmsOrderItemDTO> getOrderItemListByOrderIds(Collection<Long> orderIds) {
        return BeanUtils.toBean(omsOrderService.getOrderItemListByOrderIds(orderIds), OmsOrderItemDTO.class);
    }

    @Override
    public void updateOrders(List<OmsOrderDTO> updateOrders) {
        omsOrderService.updateOrders(BeanUtils.toBean(updateOrders, OmsOrderDO.class));
    }
}
