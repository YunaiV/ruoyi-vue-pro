package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.promotion.api.bargain.BargainRecordApi;
import cn.iocoder.yudao.module.promotion.api.bargain.dto.BargainValidateJoinRespDTO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link TradeBargainActivityPriceCalculator} 的单元测试
 *
 * @author 芋道源码
 */
public class TradeBargainActivityPriceCalculatorTest extends BaseMockitoUnitTest {

    @InjectMocks
    private TradeBargainActivityPriceCalculator tradeBargainActivityPriceCalculator;

    @Mock
    private BargainRecordApi bargainRecordApi;

    @Test
    public void testCalculate_match() {
        // 准备参数：砍价订单，单 SKU、数量 1
        TradePriceCalculateReqBO param = new TradePriceCalculateReqBO()
                .setUserId(1L).setBargainRecordId(2L)
                .setItems(singletonList(
                        new TradePriceCalculateReqBO.Item().setSkuId(10L).setCount(1).setSelected(true)
                ));
        TradePriceCalculateRespBO result = new TradePriceCalculateRespBO()
                .setType(TradeOrderTypeEnum.BARGAIN.getType())
                .setPrice(new TradePriceCalculateRespBO.Price())
                .setPromotions(new ArrayList<>())
                .setItems(singletonList(
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(10L).setCount(1).setSelected(true)
                                .setPrice(100).setSpuId(1L).setGivePoint(0)
                ));
        TradePriceCalculatorHelper.recountPayPrice(result.getItems());
        TradePriceCalculatorHelper.recountAllPrice(result);

        // mock 方法：砍价后单价 30 分
        when(bargainRecordApi.validateJoinBargain(eq(1L), eq(2L), eq(10L))).thenReturn(
                new BargainValidateJoinRespDTO().setActivityId(1000L).setName("砍价 1000 号").setBargainPrice(30));

        // 调用：100 - 30 * 1 = 70 优惠
        tradeBargainActivityPriceCalculator.calculate(param, result);

        // 断言：订单价格
        TradePriceCalculateRespBO.Price price = result.getPrice();
        assertEquals(price.getTotalPrice(), 100);
        assertEquals(price.getDiscountPrice(), 70);
        assertEquals(price.getPayPrice(), 30);
        assertEquals(result.getBargainActivityId(), 1000L);

        // 断言：促销明细的 id 来自砍价活动而非秒杀活动
        assertEquals(result.getPromotions().size(), 1);
        TradePriceCalculateRespBO.Promotion promotion = result.getPromotions().get(0);
        assertEquals(promotion.getId(), 1000L);
        assertEquals(promotion.getName(), "砍价 1000 号");
        assertEquals(promotion.getType(), PromotionTypeEnum.BARGAIN_ACTIVITY.getType());
        assertEquals(promotion.getDiscountPrice(), 70);
        assertTrue(promotion.getMatch());
        assertEquals(promotion.getDescription(), "砍价活动：省 0.70 元");
    }

    @Test
    public void testCalculate_skipNonBargainOrder() {
        // 准备参数：普通订单
        TradePriceCalculateReqBO param = new TradePriceCalculateReqBO()
                .setItems(singletonList(
                        new TradePriceCalculateReqBO.Item().setSkuId(10L).setCount(1).setSelected(true)
                ));
        TradePriceCalculateRespBO result = new TradePriceCalculateRespBO()
                .setType(TradeOrderTypeEnum.NORMAL.getType())
                .setPrice(new TradePriceCalculateRespBO.Price())
                .setPromotions(new ArrayList<>())
                .setItems(singletonList(
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(10L).setCount(1).setSelected(true)
                                .setPrice(100).setSpuId(1L)
                ));
        TradePriceCalculatorHelper.recountPayPrice(result.getItems());
        TradePriceCalculatorHelper.recountAllPrice(result);

        // 调用：非砍价订单，直接跳过，不会调用 API
        tradeBargainActivityPriceCalculator.calculate(param, result);

        // 断言：未产生促销
        assertEquals(result.getPromotions().size(), 0);
    }

}
