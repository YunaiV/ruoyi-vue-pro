package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.promotion.api.seckill.SeckillActivityApi;
import cn.iocoder.yudao.module.promotion.api.seckill.dto.SeckillActivityProductRespDTO;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * 秒杀活动的 {@link TradePriceCalculator} 实现类
 *
 * @author HUIHUI
 */
@Component
@Order(TradePriceCalculator.ORDER_DISCOUNT_ACTIVITY)
public class TradeSeckillActivityPriceCalculator implements TradePriceCalculator {

    @Resource
    private SeckillActivityApi activityApi;

    @Override
    public void calculate(TradePriceCalculateReqBO param, TradePriceCalculateRespBO result) {
        // 1、判断订单类型和是否具有秒杀活动编号
        if (ObjectUtil.notEqual(param.getType(), TradeOrderTypeEnum.SECKILL.getType()) && param.getSeckillActivityId() == null) {
            return;
        }
        // 2、获取秒杀活动商品信息
        List<SeckillActivityProductRespDTO> productList = activityApi.getSeckillActivityProductList(param.getSeckillActivityId(), convertSet(param.getItems(),
                TradePriceCalculateReqBO.Item::getSkuId));
        Map<Long, SeckillActivityProductRespDTO> productMap = convertMap(productList, SeckillActivityProductRespDTO::getSkuId);
        result.getItems().forEach(item -> {
            SeckillActivityProductRespDTO product = productMap.get(item.getSkuId());
            item.setActivityPrice(product.getSeckillPrice()); // 设置活动金额
        });
    }

}
