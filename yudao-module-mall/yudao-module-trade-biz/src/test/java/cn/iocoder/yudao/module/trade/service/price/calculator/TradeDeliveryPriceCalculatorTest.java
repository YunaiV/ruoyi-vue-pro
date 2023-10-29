package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.member.api.address.MemberAddressApi;
import cn.iocoder.yudao.module.member.api.address.dto.MemberAddressRespDTO;
import cn.iocoder.yudao.module.trade.dal.dataobject.config.TradeConfigDO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryExpressChargeModeEnum;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.service.config.TradeConfigService;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryExpressTemplateService;
import cn.iocoder.yudao.module.trade.service.delivery.bo.DeliveryExpressTemplateRespBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;

import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link TradeDeliveryPriceCalculator} 的单元测试
 *
 * @author jason
 */
public class TradeDeliveryPriceCalculatorTest  extends BaseMockitoUnitTest {

    @InjectMocks
    private TradeDeliveryPriceCalculator calculator;

    @Mock
    private MemberAddressApi addressApi;

    @Mock
    private DeliveryExpressTemplateService deliveryExpressTemplateService;
    @Mock
    private TradeConfigService tradeConfigService;

    private TradePriceCalculateReqBO reqBO;
    private TradePriceCalculateRespBO resultBO;

    private DeliveryExpressTemplateRespBO templateRespBO;
    private DeliveryExpressTemplateRespBO.Charge chargeBO;
    private DeliveryExpressTemplateRespBO.Free freeBO;

    @BeforeEach
    public void init(){
        // 准备参数
        reqBO = new TradePriceCalculateReqBO()
                .setDeliveryType(DeliveryTypeEnum.EXPRESS.getType())
                .setAddressId(10L)
                .setUserId(1L)
                .setItems(asList(
                        new TradePriceCalculateReqBO.Item().setSkuId(10L).setCount(2).setSelected(true),
                        new TradePriceCalculateReqBO.Item().setSkuId(20L).setCount(10).setSelected(true),
                        new TradePriceCalculateReqBO.Item().setSkuId(30L).setCount(4).setSelected(false) // 未选中
                ));
        resultBO = new TradePriceCalculateRespBO()
                .setPrice(new TradePriceCalculateRespBO.Price())
                .setPromotions(new ArrayList<>())
                .setItems(asList(
                        new TradePriceCalculateRespBO.OrderItem().setDeliveryTemplateId(1L).setSkuId(10L).setCount(2).setSelected(true)
                                .setWeight(10d).setVolume(10d).setPrice(100),
                        new TradePriceCalculateRespBO.OrderItem().setDeliveryTemplateId(1L).setSkuId(20L).setCount(10).setSelected(true)
                                .setWeight(10d).setVolume(10d).setPrice(200),
                        new TradePriceCalculateRespBO.OrderItem().setDeliveryTemplateId(1L).setSkuId(30L).setCount(1).setSelected(false)
                                .setWeight(10d).setVolume(10d).setPrice(300)
                ));
        // 保证价格被初始化上
        TradePriceCalculatorHelper.recountPayPrice(resultBO.getItems());
        TradePriceCalculatorHelper.recountAllPrice(resultBO);

        // 准备收件地址数据
        MemberAddressRespDTO addressResp = randomPojo(MemberAddressRespDTO.class, item -> item.setAreaId(10));
        when(addressApi.getAddress(eq(10L), eq(1L))).thenReturn(addressResp);

        // 准备运费模板费用配置数据
        chargeBO = randomPojo(DeliveryExpressTemplateRespBO.Charge.class,
                item -> item.setStartCount(10D).setStartPrice(1000).setExtraCount(10D).setExtraPrice(2000));
        // 准备运费模板包邮配置数据：订单总件数 < 包邮件数时 12 < 20
        freeBO = randomPojo(DeliveryExpressTemplateRespBO.Free.class,
                item -> item.setFreeCount(20).setFreePrice(100));
        // 准备 SP 运费模板数据
        templateRespBO = randomPojo(DeliveryExpressTemplateRespBO.class,
                item -> item.setChargeMode(DeliveryExpressChargeModeEnum.COUNT.getType())
                        .setCharge(chargeBO).setFree(freeBO));
    }

    @Test
    @DisplayName("全场包邮")
    public void testCalculate_expressGlobalFree() {
        // mock 方法（全场包邮）
        when(tradeConfigService.getTradeConfig()).thenReturn(new TradeConfigDO().setDeliveryExpressFreeEnabled(true)
                .setDeliveryExpressFreePrice(2200));

        // 调用
        calculator.calculate(reqBO, resultBO);
        TradePriceCalculateRespBO.Price price = resultBO.getPrice();
        assertThat(price)
                .extracting("totalPrice","discountPrice","couponPrice","pointPrice","deliveryPrice","payPrice")
                .containsExactly(2200, 0, 0, 0, 0,  2200);
        assertThat(resultBO.getItems()).hasSize(3);
        // 断言：SKU1
        assertThat(resultBO.getItems().get(0))
                .extracting("price", "count","discountPrice" ,"couponPrice", "pointPrice","deliveryPrice","payPrice")
                .containsExactly(100, 2, 0, 0, 0, 0, 200);
        // 断言：SKU2
        assertThat(resultBO.getItems().get(1))
                .extracting("price", "count","discountPrice" ,"couponPrice", "pointPrice","deliveryPrice","payPrice")
                .containsExactly(200, 10, 0, 0, 0, 0, 2000);
        // 断言：SKU3 未选中
        assertThat(resultBO.getItems().get(2))
                .extracting("price", "count","discountPrice" ,"couponPrice", "pointPrice","deliveryPrice","payPrice")
                .containsExactly(300, 1, 0, 0, 0, 0, 300);
    }

    @Test
    @DisplayName("按件计算运费不包邮的情况")
    public void testCalculate_expressTemplateCharge() {
        // SKU 1 : 100 * 2  = 200
        // SKU 2 ：200 * 10 = 2000
        // 运费  首件 1000 +  续件 2000 = 3000
        // mock 方法
        when(deliveryExpressTemplateService.getExpressTemplateMapByIdsAndArea(eq(asSet(1L)), eq(10)))
                .thenReturn(MapUtil.of(1L, templateRespBO));

        // 调用
        calculator.calculate(reqBO, resultBO);
        // 断言
        TradePriceCalculateRespBO.Price price = resultBO.getPrice();
        assertThat(price)
                .extracting("totalPrice","discountPrice","couponPrice","pointPrice","deliveryPrice","payPrice")
                .containsExactly(2200, 0, 0, 0, 3000,  5200);
        assertThat(resultBO.getItems()).hasSize(3);
        // 断言：SKU1
        assertThat(resultBO.getItems().get(0))
                .extracting("price", "count","discountPrice" ,"couponPrice", "pointPrice","deliveryPrice","payPrice")
                .containsExactly(100, 2, 0, 0, 0, 500, 700);
        // 断言：SKU2
        assertThat(resultBO.getItems().get(1))
                .extracting("price", "count","discountPrice" ,"couponPrice", "pointPrice","deliveryPrice","payPrice")
                .containsExactly(200, 10, 0, 0, 0, 2500, 4500);
        // 断言：SKU3 未选中
        assertThat(resultBO.getItems().get(2))
                .extracting("price", "count","discountPrice" ,"couponPrice", "pointPrice","deliveryPrice","payPrice")
                .containsExactly(300, 1, 0, 0, 0, 0, 300);
    }

    @Test
    @DisplayName("按件计算运费包邮的情况")
    public void testCalculate_expressTemplateFree() {
        // SKU 1 : 100 * 2  = 200
        // SKU 2 ：200 * 10 = 2000
        // 运费  0
        // mock 方法
        // 准备运费模板包邮配置数据 包邮 订单总件数 > 包邮件数时 12 > 10
        templateRespBO.setFree(randomPojo(DeliveryExpressTemplateRespBO.Free.class,
                item -> item.setFreeCount(10).setFreePrice(1000)));
        when(deliveryExpressTemplateService.getExpressTemplateMapByIdsAndArea(eq(asSet(1L)), eq(10)))
                .thenReturn(MapUtil.of(1L, templateRespBO));

        // 调用
        calculator.calculate(reqBO, resultBO);
        // 断言
        TradePriceCalculateRespBO.Price price = resultBO.getPrice();
        assertThat(price)
                .extracting("totalPrice","discountPrice","couponPrice","pointPrice","deliveryPrice","payPrice")
                .containsExactly(2200, 0, 0, 0, 0,  2200);
        assertThat(resultBO.getItems()).hasSize(3);
        // 断言：SKU1
        assertThat(resultBO.getItems().get(0))
                .extracting("price", "count","discountPrice" ,"couponPrice", "pointPrice","deliveryPrice","payPrice")
                .containsExactly(100, 2, 0, 0, 0, 0, 200);
        // 断言：SKU2
        assertThat(resultBO.getItems().get(1))
                .extracting("price", "count","discountPrice" ,"couponPrice", "pointPrice","deliveryPrice","payPrice")
                .containsExactly(200, 10, 0, 0, 0, 0, 2000);
        // 断言：SKU3 未选中
        assertThat(resultBO.getItems().get(2))
                .extracting("price", "count","discountPrice" ,"couponPrice", "pointPrice","deliveryPrice","payPrice")
                .containsExactly(300, 1, 0, 0, 0, 0, 300);
    }

}
