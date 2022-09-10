package cn.iocoder.yudao.module.market.service.price;

import cn.iocoder.yudao.module.market.api.price.PriceApi;
import cn.iocoder.yudao.module.market.api.price.dto.PriceCalculateReqDTO;
import cn.iocoder.yudao.module.market.api.price.dto.PriceCalculateRespDTO;
import org.springframework.stereotype.Service;

/**
 * 价格 API 实现类
 *
 * TODO 完善注释
 *
 * @author TODO
 */
@Service
public class PriceApiImpl implements PriceApi {

    @Override
    public PriceCalculateRespDTO calculatePrice(PriceCalculateReqDTO calculateReqDTO) {
        // TODO fixme：实现逻辑
        return new PriceCalculateRespDTO();
    }

}
