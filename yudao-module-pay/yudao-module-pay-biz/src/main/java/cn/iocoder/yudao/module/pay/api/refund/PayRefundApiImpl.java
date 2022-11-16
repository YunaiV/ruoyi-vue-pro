package cn.iocoder.yudao.module.pay.api.refund;

import cn.iocoder.yudao.module.pay.api.refund.dto.PayRefundCreateReqDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 退款单 API 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PayRefundApiImpl implements PayRefundApi {

    @Override
    public Long createPayRefund(PayRefundCreateReqDTO reqDTO) {
        // TODO 芋艿：暂未实现
        return null;
    }
}
