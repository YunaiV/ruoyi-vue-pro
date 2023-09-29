package cn.iocoder.yudao.module.pay.api.transfer;

import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferCreateReqDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * @author jason
 */
@Service
@Validated
public class PayTransferApiImpl implements PayTransferApi {
    @Override
    public Long createTransfer(PayTransferCreateReqDTO reqDTO) {

        return null;
    }
}
