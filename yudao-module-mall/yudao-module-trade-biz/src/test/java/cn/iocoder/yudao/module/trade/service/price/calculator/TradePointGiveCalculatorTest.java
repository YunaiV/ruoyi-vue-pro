package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.member.api.config.MemberConfigApi;
import cn.iocoder.yudao.module.member.api.config.dto.MemberConfigRespDTO;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

// TODO 芋艿：晚点 review
/**
 * {@link TradePointGiveCalculator} 的单元测试类
 *
 * @author owen
 */
public class TradePointGiveCalculatorTest extends BaseMockitoUnitTest {

    @InjectMocks
    private TradePointGiveCalculator tradePointGiveCalculator;

    @Mock
    private MemberConfigApi memberConfigApi;

    @Test
    public void testCalculate() {

        // 准备参数
        TradePriceCalculateReqBO param = new TradePriceCalculateReqBO()
                .setUserId(233L)
                .setItems(asList(
                        new TradePriceCalculateReqBO.Item().setSkuId(10L).setCount(2).setSelected(true), // 全局积分
                        new TradePriceCalculateReqBO.Item().setSkuId(20L).setCount(3).setSelected(true), // 全局积分 + SKU 积分
                        new TradePriceCalculateReqBO.Item().setSkuId(30L).setCount(4).setSelected(false), // 全局积分，但是未选中
                        new TradePriceCalculateReqBO.Item().setSkuId(40L).setCount(5).setSelected(false) // 全局积分 + SKU 积分，但是未选中
                ));
        TradePriceCalculateRespBO result = new TradePriceCalculateRespBO()
                .setType(TradeOrderTypeEnum.NORMAL.getType())
                .setPrice(new TradePriceCalculateRespBO.Price())
                .setPromotions(new ArrayList<>())
                .setItems(asList(
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(10L).setCount(2).setSelected(true)
                                .setPrice(100).setSpuId(1L).setGivePoint(0),
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(20L).setCount(3).setSelected(true)
                                .setPrice(50).setSpuId(2L).setGivePoint(100),
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(30L).setCount(4).setSelected(false)
                                .setPrice(30).setSpuId(3L).setGivePoint(0),
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(40L).setCount(5).setSelected(false)
                                .setPrice(60).setSpuId(1L).setGivePoint(100)
                ));
        // 保证价格被初始化上
        TradePriceCalculatorHelper.recountPayPrice(result.getItems());
        TradePriceCalculatorHelper.recountAllPrice(result);

        // mock 方法（积分配置 信息）
        MemberConfigRespDTO memberConfig = randomPojo(MemberConfigRespDTO.class,
                o -> o.setPointTradeDeductEnable(true)  // 启用积分折扣
                        .setPointTradeGivePoint(100)); // 1 元赠送多少分
        when(memberConfigApi.getConfig()).thenReturn(memberConfig);

        // 调用
        tradePointGiveCalculator.calculate(param, result);
        // 断言：Price 部分
        assertEquals(result.getGivePoint(), 2 * 100 + 3 * 50 + 100);
        // 断言：SKU 1
        TradePriceCalculateRespBO.OrderItem orderItem01 = result.getItems().get(0);
        assertEquals(orderItem01.getSkuId(), 10L);
        assertEquals(orderItem01.getCount(), 2);
        assertEquals(orderItem01.getPrice(), 100);
        assertEquals(orderItem01.getGivePoint(), 2 * 100); // 全局积分
        // 断言：SKU 2
        TradePriceCalculateRespBO.OrderItem orderItem02 = result.getItems().get(1);
        assertEquals(orderItem02.getSkuId(), 20L);
        assertEquals(orderItem02.getCount(), 3);
        assertEquals(orderItem02.getPrice(), 50);
        assertEquals(orderItem02.getGivePoint(), 3 * 50 + 100); // 全局积分 + SKU 积分
        // 断言：SKU 3
        TradePriceCalculateRespBO.OrderItem orderItem03 = result.getItems().get(2);
        assertEquals(orderItem03.getSkuId(), 30L);
        assertEquals(orderItem03.getCount(), 4);
        assertEquals(orderItem03.getPrice(), 30);
        assertEquals(orderItem03.getGivePoint(), 0); // 全局积分，但是未选中
        // 断言：SKU 4
        TradePriceCalculateRespBO.OrderItem orderItem04 = result.getItems().get(3);
        assertEquals(orderItem04.getSkuId(), 40L);
        assertEquals(orderItem04.getCount(), 5);
        assertEquals(orderItem04.getPrice(), 60);
        assertEquals(orderItem04.getGivePoint(), 100); // 全局积分 + SKU 积分，但是未选中
    }
}
