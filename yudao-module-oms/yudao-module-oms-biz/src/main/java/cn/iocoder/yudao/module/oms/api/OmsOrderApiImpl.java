package cn.iocoder.yudao.module.oms.api;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oms.api.dto.OmsOrderDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsOrderSaveReqDTO;
import cn.iocoder.yudao.module.oms.service.OmsOrderService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

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
}
