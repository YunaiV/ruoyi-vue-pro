package cn.iocoder.yudao.module.oms.api;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oms.api.dto.OmsOrderItemDTO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsOrderItemDO;
import cn.iocoder.yudao.module.oms.service.OmsOrderItemService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OmsOrderItemApiImpl implements OmsOrderItemApi {

    @Resource
    private OmsOrderItemService omsOrderItemService;

    @Override
    public void updateOrderItems(List<OmsOrderItemDTO> updateOrderItems) {
        omsOrderItemService.updateOrderItems(BeanUtils.toBean(
            updateOrderItems, OmsOrderItemDO.class
        ));
    }
}
