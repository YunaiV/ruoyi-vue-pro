package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.member.api.config.MemberConfigApi;
import cn.iocoder.yudao.module.member.api.config.dto.MemberConfigRespDTO;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.promotion.enums.common.PromotionTypeEnum;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderTypeEnum;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

// TODO 芋艿：晚点 review
/**
 * {@link TradePointUsePriceCalculator } 的单元测试类
 *
 * @author owen
 */
@Disabled // TODO 芋艿：后续修复
public class TradePointUsePriceCalculatorTest extends BaseMockitoUnitTest {

    @InjectMocks
    private TradePointUsePriceCalculator tradePointUsePriceCalculator;

    @Mock
    private MemberConfigApi memberConfigApi;
    @Mock
    private MemberUserApi memberUserApi;

    @Test
    public void testCalculate_success() {
        // 准备参数
        TradePriceCalculateReqBO param = new TradePriceCalculateReqBO()
                .setUserId(233L).setPointStatus(true) // 是否使用积分
                .setItems(asList(
                        new TradePriceCalculateReqBO.Item().setSkuId(10L).setCount(2).setSelected(true), // 使用积分
                        new TradePriceCalculateReqBO.Item().setSkuId(20L).setCount(3).setSelected(true), // 使用积分
                        new TradePriceCalculateReqBO.Item().setSkuId(30L).setCount(5).setSelected(false) // 未选中，不使用积分
                ));
        TradePriceCalculateRespBO result = new TradePriceCalculateRespBO()
                .setType(TradeOrderTypeEnum.NORMAL.getType())
                .setPrice(new TradePriceCalculateRespBO.Price())
                .setPromotions(new ArrayList<>())
                .setItems(asList(
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(10L).setCount(2).setSelected(true)
                                .setPrice(100).setSpuId(1L),
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(20L).setCount(3).setSelected(true)
                                .setPrice(50).setSpuId(2L),
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(30L).setCount(5).setSelected(false)
                                .setPrice(30).setSpuId(3L)
                ));
        // 保证价格被初始化上
        TradePriceCalculatorHelper.recountPayPrice(result.getItems());
        TradePriceCalculatorHelper.recountAllPrice(result);

        // mock 方法（积分配置 信息）
        MemberConfigRespDTO memberConfig = randomPojo(MemberConfigRespDTO.class,
                o -> o.setPointTradeDeductEnable(true) // 启用积分折扣
                        .setPointTradeDeductUnitPrice(1) // 1 积分抵扣多少金额（单位分）
                        .setPointTradeDeductMaxPrice(100)); // 积分抵扣最大值
        when(memberConfigApi.getConfig()).thenReturn(memberConfig);
        // mock 方法（会员 信息）
        MemberUserRespDTO user = randomPojo(MemberUserRespDTO.class, o -> o.setId(param.getUserId()).setPoint(100));
        when(memberUserApi.getUser(user.getId())).thenReturn(user);

        // 调用
        tradePointUsePriceCalculator.calculate(param, result);
        // 断言：使用了多少积分
        assertEquals(result.getUsePoint(), 100);
        // 断言：Price 部分
        TradePriceCalculateRespBO.Price price = result.getPrice();
        assertEquals(price.getTotalPrice(), 350);
        assertEquals(price.getPayPrice(), 250);
        assertEquals(price.getPointPrice(), 100);
        // 断言：SKU 1
        TradePriceCalculateRespBO.OrderItem orderItem01 = result.getItems().get(0);
        assertEquals(orderItem01.getSkuId(), 10L);
        assertEquals(orderItem01.getCount(), 2);
        assertEquals(orderItem01.getPrice(), 100);
        assertEquals(orderItem01.getPointPrice(), 57);
        assertEquals(orderItem01.getPayPrice(), 143);
        // 断言：SKU 2
        TradePriceCalculateRespBO.OrderItem orderItem02 = result.getItems().get(1);
        assertEquals(orderItem02.getSkuId(), 20L);
        assertEquals(orderItem02.getCount(), 3);
        assertEquals(orderItem02.getPrice(), 50);
        assertEquals(orderItem02.getPointPrice(), 43);
        assertEquals(orderItem02.getPayPrice(), 107);
        // 断言：SKU 3
        TradePriceCalculateRespBO.OrderItem orderItem03 = result.getItems().get(2);
        assertEquals(orderItem03.getSkuId(), 30L);
        assertEquals(orderItem03.getCount(), 5);
        assertEquals(orderItem03.getPrice(), 30);
        assertEquals(orderItem03.getPointPrice(), 0);
        assertEquals(orderItem03.getPayPrice(), 150);
        // 断言：Promotion 部分
        assertEquals(result.getPromotions().size(), 1);
        TradePriceCalculateRespBO.Promotion promotion01 = result.getPromotions().get(0);
        assertEquals(promotion01.getId(), user.getId());
        assertEquals(promotion01.getName(), "积分抵扣");
        assertEquals(promotion01.getType(), PromotionTypeEnum.POINT.getType());
        assertEquals(promotion01.getTotalPrice(), 350);
        assertEquals(promotion01.getDiscountPrice(), 100);
        assertTrue(promotion01.getMatch());
        assertEquals(promotion01.getDescription(), "积分抵扣：省 1.00 元");
        assertEquals(promotion01.getItems().size(), 2);
        TradePriceCalculateRespBO.PromotionItem promotionItem011 = promotion01.getItems().get(0);
        assertEquals(promotionItem011.getSkuId(), 10L);
        assertEquals(promotionItem011.getTotalPrice(), 200);
        assertEquals(promotionItem011.getDiscountPrice(), 57);
        TradePriceCalculateRespBO.PromotionItem promotionItem012 = promotion01.getItems().get(1);
        assertEquals(promotionItem012.getSkuId(), 20L);
        assertEquals(promotionItem012.getTotalPrice(), 150);
        assertEquals(promotionItem012.getDiscountPrice(), 43);
    }

    /**
     * 当用户积分充足时，抵扣的金额为：配置表的“积分抵扣最大值”
     */
    @Test
    public void testCalculate_TradeDeductMaxPrice() {
        // 准备参数
        TradePriceCalculateReqBO param = new TradePriceCalculateReqBO()
                .setUserId(233L).setPointStatus(true) // 是否使用积分
                .setItems(asList(
                        new TradePriceCalculateReqBO.Item().setSkuId(10L).setCount(2).setSelected(true), // 使用积分
                        new TradePriceCalculateReqBO.Item().setSkuId(20L).setCount(3).setSelected(true), // 使用积分
                        new TradePriceCalculateReqBO.Item().setSkuId(30L).setCount(5).setSelected(false) // 未选中，不使用积分
                ));
        TradePriceCalculateRespBO result = new TradePriceCalculateRespBO()
                .setType(TradeOrderTypeEnum.NORMAL.getType())
                .setPrice(new TradePriceCalculateRespBO.Price())
                .setPromotions(new ArrayList<>())
                .setItems(asList(
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(10L).setCount(2).setSelected(true)
                                .setPrice(100).setSpuId(1L),
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(20L).setCount(3).setSelected(true)
                                .setPrice(50).setSpuId(2L),
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(30L).setCount(5).setSelected(false)
                                .setPrice(30).setSpuId(3L)
                ));
        // 保证价格被初始化上
        TradePriceCalculatorHelper.recountPayPrice(result.getItems());
        TradePriceCalculatorHelper.recountAllPrice(result);

        // mock 方法（积分配置 信息）
        MemberConfigRespDTO memberConfig = randomPojo(MemberConfigRespDTO.class,
                o -> o.setPointTradeDeductEnable(true) // 启用积分折扣
                        .setPointTradeDeductUnitPrice(1) // 1 积分抵扣多少金额（单位分）
                        .setPointTradeDeductMaxPrice(50)); // 积分抵扣最大值
        when(memberConfigApi.getConfig()).thenReturn(memberConfig);
        // mock 方法（会员 信息）
        MemberUserRespDTO user = randomPojo(MemberUserRespDTO.class, o -> o.setId(param.getUserId()).setPoint(100));
        when(memberUserApi.getUser(user.getId())).thenReturn(user);

        // 调用
        tradePointUsePriceCalculator.calculate(param, result);
        // 断言：使用了多少积分
        assertEquals(result.getUsePoint(), 50);
        // 断言：Price 部分
        TradePriceCalculateRespBO.Price price = result.getPrice();
        assertEquals(price.getTotalPrice(), 350);
        assertEquals(price.getPayPrice(), 300);
        assertEquals(price.getPointPrice(), 50);
        // 断言：SKU 1
        TradePriceCalculateRespBO.OrderItem orderItem01 = result.getItems().get(0);
        assertEquals(orderItem01.getSkuId(), 10L);
        assertEquals(orderItem01.getCount(), 2);
        assertEquals(orderItem01.getPrice(), 100);
        assertEquals(orderItem01.getPointPrice(), 28);
        assertEquals(orderItem01.getPayPrice(), 172);
        // 断言：SKU 2
        TradePriceCalculateRespBO.OrderItem orderItem02 = result.getItems().get(1);
        assertEquals(orderItem02.getSkuId(), 20L);
        assertEquals(orderItem02.getCount(), 3);
        assertEquals(orderItem02.getPrice(), 50);
        assertEquals(orderItem02.getPointPrice(), 22);
        assertEquals(orderItem02.getPayPrice(), 128);
        // 断言：SKU 3
        TradePriceCalculateRespBO.OrderItem orderItem03 = result.getItems().get(2);
        assertEquals(orderItem03.getSkuId(), 30L);
        assertEquals(orderItem03.getCount(), 5);
        assertEquals(orderItem03.getPrice(), 30);
        assertEquals(orderItem03.getPointPrice(), 0);
        assertEquals(orderItem03.getPayPrice(), 150);
        // 断言：Promotion 部分
        assertEquals(result.getPromotions().size(), 1);
        TradePriceCalculateRespBO.Promotion promotion01 = result.getPromotions().get(0);
        assertEquals(promotion01.getId(), user.getId());
        assertEquals(promotion01.getName(), "积分抵扣");
        assertEquals(promotion01.getType(), PromotionTypeEnum.POINT.getType());
        assertEquals(promotion01.getTotalPrice(), 350);
        assertEquals(promotion01.getDiscountPrice(), 50);
        assertTrue(promotion01.getMatch());
        assertEquals(promotion01.getDescription(), "积分抵扣：省 0.50 元");
        assertEquals(promotion01.getItems().size(), 2);
        TradePriceCalculateRespBO.PromotionItem promotionItem011 = promotion01.getItems().get(0);
        assertEquals(promotionItem011.getSkuId(), 10L);
        assertEquals(promotionItem011.getTotalPrice(), 200);
        assertEquals(promotionItem011.getDiscountPrice(), 28);
        TradePriceCalculateRespBO.PromotionItem promotionItem012 = promotion01.getItems().get(1);
        assertEquals(promotionItem012.getSkuId(), 20L);
        assertEquals(promotionItem012.getTotalPrice(), 150);
        assertEquals(promotionItem012.getDiscountPrice(), 22);
    }

    /**
     * 订单不使用积分，不会产生优惠
     */
    @Test
    public void testCalculate_PointStatusFalse() {
        // 准备参数
        TradePriceCalculateReqBO param = new TradePriceCalculateReqBO()
                .setUserId(233L).setPointStatus(false) // 是否使用积分
                .setItems(asList(
                        new TradePriceCalculateReqBO.Item().setSkuId(10L).setCount(2).setSelected(true), // 使用积分
                        new TradePriceCalculateReqBO.Item().setSkuId(20L).setCount(3).setSelected(true), // 使用积分
                        new TradePriceCalculateReqBO.Item().setSkuId(30L).setCount(5).setSelected(false) // 未选中，不使用积分
                ));
        TradePriceCalculateRespBO result = new TradePriceCalculateRespBO()
                .setType(TradeOrderTypeEnum.NORMAL.getType())
                .setPrice(new TradePriceCalculateRespBO.Price())
                .setPromotions(new ArrayList<>())
                .setItems(asList(
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(10L).setCount(2).setSelected(true)
                                .setPrice(100).setSpuId(1L),
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(20L).setCount(3).setSelected(true)
                                .setPrice(50).setSpuId(2L),
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(30L).setCount(5).setSelected(false)
                                .setPrice(30).setSpuId(3L)
                ));
        // 保证价格被初始化上
        TradePriceCalculatorHelper.recountPayPrice(result.getItems());
        TradePriceCalculatorHelper.recountAllPrice(result);

        // 调用
        tradePointUsePriceCalculator.calculate(param, result);
        // 断言：没有使用积分
        assertNotUsePoint(result);
    }

    /**
     * 会员积分不足，不会产生优惠
     */
    @Test
    public void testCalculate_UserPointNotEnough() {
        // 准备参数
        TradePriceCalculateReqBO param = new TradePriceCalculateReqBO()
                .setUserId(233L).setPointStatus(true) // 是否使用积分
                .setItems(asList(
                        new TradePriceCalculateReqBO.Item().setSkuId(10L).setCount(2).setSelected(true), // 使用积分
                        new TradePriceCalculateReqBO.Item().setSkuId(20L).setCount(3).setSelected(true), // 使用积分
                        new TradePriceCalculateReqBO.Item().setSkuId(30L).setCount(5).setSelected(false) // 未选中，不使用积分
                ));
        TradePriceCalculateRespBO result = new TradePriceCalculateRespBO()
                .setType(TradeOrderTypeEnum.NORMAL.getType())
                .setPrice(new TradePriceCalculateRespBO.Price())
                .setPromotions(new ArrayList<>())
                .setItems(asList(
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(10L).setCount(2).setSelected(true)
                                .setPrice(100).setSpuId(1L),
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(20L).setCount(3).setSelected(true)
                                .setPrice(50).setSpuId(2L),
                        new TradePriceCalculateRespBO.OrderItem().setSkuId(30L).setCount(5).setSelected(false)
                                .setPrice(30).setSpuId(3L)
                ));
        // 保证价格被初始化上
        TradePriceCalculatorHelper.recountPayPrice(result.getItems());
        TradePriceCalculatorHelper.recountAllPrice(result);

        // mock 方法（积分配置 信息）
        MemberConfigRespDTO memberConfig = randomPojo(MemberConfigRespDTO.class,
                o -> o.setPointTradeDeductEnable(true) // 启用积分折扣
                        .setPointTradeDeductUnitPrice(1) // 1 积分抵扣多少金额（单位分）
                        .setPointTradeDeductMaxPrice(100)); // 积分抵扣最大值
        when(memberConfigApi.getConfig()).thenReturn(memberConfig);
        // mock 方法（会员 信息）
        MemberUserRespDTO user = randomPojo(MemberUserRespDTO.class, o -> o.setId(param.getUserId()).setPoint(0));
        when(memberUserApi.getUser(user.getId())).thenReturn(user);

        // 调用
        tradePointUsePriceCalculator.calculate(param, result);

        // 断言：没有使用积分
        assertNotUsePoint(result);
    }

    /**
     * 断言：没有使用积分
     */
    private static void assertNotUsePoint(TradePriceCalculateRespBO result) {
        // 断言：使用了多少积分
        assertEquals(result.getUsePoint(), 0);
        // 断言：Price 部分
        TradePriceCalculateRespBO.Price price = result.getPrice();
        assertEquals(price.getTotalPrice(), 350);
        assertEquals(price.getPayPrice(), 350);
        assertEquals(price.getPointPrice(), 0);
        // 断言：SKU 1
        TradePriceCalculateRespBO.OrderItem orderItem01 = result.getItems().get(0);
        assertEquals(orderItem01.getSkuId(), 10L);
        assertEquals(orderItem01.getCount(), 2);
        assertEquals(orderItem01.getPrice(), 100);
        assertEquals(orderItem01.getPointPrice(), 0);
        assertEquals(orderItem01.getPayPrice(), 200);
        // 断言：SKU 2
        TradePriceCalculateRespBO.OrderItem orderItem02 = result.getItems().get(1);
        assertEquals(orderItem02.getSkuId(), 20L);
        assertEquals(orderItem02.getCount(), 3);
        assertEquals(orderItem02.getPrice(), 50);
        assertEquals(orderItem02.getPointPrice(), 0);
        assertEquals(orderItem02.getPayPrice(), 150);
        // 断言：SKU 3
        TradePriceCalculateRespBO.OrderItem orderItem03 = result.getItems().get(2);
        assertEquals(orderItem03.getSkuId(), 30L);
        assertEquals(orderItem03.getCount(), 5);
        assertEquals(orderItem03.getPrice(), 30);
        assertEquals(orderItem03.getPointPrice(), 0);
        assertEquals(orderItem03.getPayPrice(), 150);
        // 断言：Promotion 部分
        assertEquals(result.getPromotions().size(), 0);
    }
}
