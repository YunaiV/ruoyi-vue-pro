package cn.iocoder.yudao.module.pay.api;

import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.PayOrderDataCreateReqDTO;
import org.springframework.stereotype.Service;

/**
 * TODO 注释
 */
@Service
public class PayOrderApiImpl implements PayOrderApi {

    @Override
    public Long createPayOrder(PayOrderDataCreateReqDTO reqDTO) {
        return null;
    }

}
