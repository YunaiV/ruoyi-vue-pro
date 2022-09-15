package cn.iocoder.yudao.module.market.api.price;

import cn.iocoder.yudao.module.market.api.price.dto.PriceCalculateReqDTO;
import cn.iocoder.yudao.module.market.api.price.dto.PriceCalculateRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * @author LeeYan9
 * @since 2022-09-06
 */
@Service
@Validated
public class PriceApiImpl implements PriceApi {

    @Override
    public PriceCalculateRespDTO calculatePrice(PriceCalculateReqDTO calculateReqDTO) {
        return null;
    }
}
