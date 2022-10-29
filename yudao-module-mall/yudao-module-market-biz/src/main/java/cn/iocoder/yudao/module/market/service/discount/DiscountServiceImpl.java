package cn.iocoder.yudao.module.market.service.discount;

import cn.iocoder.yudao.module.market.dal.dataobject.discount.DiscountProductDO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 限时折扣 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class DiscountServiceImpl implements DiscountService {

    // TODO 芋艿：待实现
    @Override
    public Map<Long, DiscountProductDO> getMatchDiscountProducts(Collection<Long> skuIds) {
        Map<Long, DiscountProductDO> products = new HashMap<>();
        products.put(1L, new DiscountProductDO().setPromotionPrice(100));
        products.put(2L, new DiscountProductDO().setPromotionPrice(50));
        return products;
    }

}
