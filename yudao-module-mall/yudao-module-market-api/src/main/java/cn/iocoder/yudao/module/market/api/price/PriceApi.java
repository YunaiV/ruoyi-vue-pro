package cn.iocoder.yudao.module.market.api.price;

import cn.iocoder.yudao.module.market.api.price.dto.PriceCalculateReqDTO;
import cn.iocoder.yudao.module.market.api.price.dto.PriceCalculateRespDTO;

/**
 * 价格 API 接口
 *
 * @author 芋道源码
 */
public interface PriceApi {

    /**
     * 计算商品的价格
     *
     * @param calculateReqDTO 价格请求
     * @return 价格相应
     */
    PriceCalculateRespDTO calculatePrice(PriceCalculateReqDTO calculateReqDTO);

}
