package cn.iocoder.yudao.module.promotion.service.discount;

import cn.iocoder.yudao.module.promotion.dal.dataobject.discount.DiscountProductDO;

import java.util.Collection;
import java.util.Map;

/**
 * 限时折扣 Service 接口
 *
 * @author 芋道源码
 */
public interface DiscountService {

    /**
     * 基于指定 SKU 编号数组，获得匹配的限时折扣商品
     *
     * 注意，匹配的条件，仅仅是日期符合，并且处于开启状态
     *
     * @param skuIds SKU 编号数组
     * @return 匹配的限时折扣商品
     */
    Map<Long, DiscountProductDO> getMatchDiscountProducts(Collection<Long> skuIds);

}
