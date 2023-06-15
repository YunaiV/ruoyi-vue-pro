package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.member.api.address.AddressApi;
import cn.iocoder.yudao.module.member.api.address.dto.AddressRespDTO;
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
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.trade.enums.delivery.DeliveryExpressChargeModeEnum.PIECE;
import static cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum.EXPRESS;
import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author jason
 */
public class TradeDeliveryPriceCalculatorTest  extends BaseMockitoUnitTest {

    @InjectMocks
    private TradeDeliveryPriceCalculator calculator;
    @Mock
    private AddressApi addressApi;
    @Mock
    private DeliveryExpressTemplateService deliveryExpressTemplateService;

    private TradePriceCalculateReqBO reqBO;
    private TradePriceCalculateRespBO resultBO;
    private AddressRespDTO addressResp;
    private DeliveryExpressTemplateRespBO.DeliveryExpressTemplateChargeBO chargeBO;
    private DeliveryExpressTemplateRespBO.DeliveryExpressTemplateFreeBO freeBO;
    private DeliveryExpressTemplateRespBO templateRespBO;

    @BeforeEach
    public void init(){
        // 准备参数
        reqBO = new TradePriceCalculateReqBO()
                .setDeliveryType(EXPRESS.getMode())
                .setAddressId(10L)
                .setUserId(1L)
                .setItems(asList(
                        new TradePriceCalculateReqBO.Item().setSkuId(10L).setCount(2).setSelected(true),
                        new TradePriceCalculateReqBO.Item().setSkuId(20L).setCount(10).setSelected(true),
                        new TradePriceCalculateReqBO.Item().setSkuId(30L).setCount(4).setSelected(false)
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
        addressResp = randomPojo(AddressRespDTO.class, item -> item.setAreaId(10));
        // 准备运费模板费用配置数据
        chargeBO = randomPojo(DeliveryExpressTemplateRespBO.DeliveryExpressTemplateChargeBO.class,
                item -> item.setStartCount(10D).setStartPrice(1000).setExtraCount(10D).setExtraPrice(2000));
        // 准备运费模板包邮配置数据 订单总件数 < 包邮件数时 12 < 20
        freeBO = randomPojo(DeliveryExpressTemplateRespBO.DeliveryExpressTemplateFreeBO.class,
                item -> item.setFreeCount(20).setFreePrice(100));
        // 准备 SP 运费模板 数据
        templateRespBO = randomPojo(DeliveryExpressTemplateRespBO.class,
                item -> item.setChargeMode(PIECE.getType())
                        .setTemplateCharge(chargeBO).setTemplateFree(freeBO));
    }

    @Test
    @DisplayName("按件计算运费不包邮的情况")
    public void testCalculateByExpressTemplateCharge() {
        // SKU 1 : 100 * 2  = 200
        // SKU 2 ：200 * 10 = 2000
        // 运费  首件 1000 +  续件 2000 = 3000
        Map<Long, DeliveryExpressTemplateRespBO> respMap = new HashMap<>();
        respMap.put(1L, templateRespBO);

        // mock 方法
        when(addressApi.getAddress(eq(10L), eq(1L))).thenReturn(addressResp);
        when(deliveryExpressTemplateService.getExpressTemplateMapByIdsAndArea(eq(asSet(1L)), eq(10)))
                .thenReturn(respMap);

        calculator.calculate(reqBO, resultBO);

        TradePriceCalculateRespBO.Price price = resultBO.getPrice();

        assertThat(price)
                .extracting("totalPrice","discountPrice","couponPrice","pointPrice","deliveryPrice","payPrice")
                .containsExactly(2200, 0, 0, 0, 3000,  5200);
        // 断言：SKU
        assertThat(resultBO.getItems()).hasSize(3);
        // SKU1
        assertThat(resultBO.getItems().get(0))
                .extracting("price", "count","discountPrice" ,"couponPrice", "pointPrice","deliveryPrice","payPrice")
                .containsExactly(100, 2, 0, 0, 0, 1500, 1700);
        // SKU2
        assertThat(resultBO.getItems().get(1))
                .extracting("price", "count","discountPrice" ,"couponPrice", "pointPrice","deliveryPrice","payPrice")
                .containsExactly(200, 10, 0, 0, 0, 1500, 3500);
        // SKU3 未选中
        assertThat(resultBO.getItems().get(2))
                .extracting("price", "count","discountPrice" ,"couponPrice", "pointPrice","deliveryPrice","payPrice")
                .containsExactly(300, 1, 0, 0, 0, 0, 300);
    }

    @Test
    @DisplayName("按件计算运费包邮的情况")
    public void testCalculateByExpressTemplateFree() {
        // SKU 1 : 100 * 2  = 200
        // SKU 2 ：200 * 10 = 2000
        // 运费  0
        Map<Long, DeliveryExpressTemplateRespBO> respMap = new HashMap<>();
        respMap.put(1L, templateRespBO);
        // 准备运费模板包邮配置数据 包邮 订单总件数 > 包邮件数时 12 > 10
        freeBO = randomPojo(DeliveryExpressTemplateRespBO.DeliveryExpressTemplateFreeBO.class,
                item -> item.setFreeCount(10).setFreePrice(1000));
        templateRespBO.setTemplateFree(freeBO);
        // mock 方法
        when(addressApi.getAddress(eq(10L), eq(1L))).thenReturn(addressResp);
        when(deliveryExpressTemplateService.getExpressTemplateMapByIdsAndArea(eq(asSet(1L)), eq(10)))
                .thenReturn(respMap);

        calculator.calculate(reqBO, resultBO);

        TradePriceCalculateRespBO.Price price = resultBO.getPrice();

        // 断言price
        assertThat(price)
                .extracting("totalPrice","discountPrice","couponPrice","pointPrice","deliveryPrice","payPrice")
                .containsExactly(2200, 0, 0, 0, 0,  2200);
        // 断言：SKU
        assertThat(resultBO.getItems()).hasSize(3);
        // SKU1
        assertThat(resultBO.getItems().get(0))
                .extracting("price", "count","discountPrice" ,"couponPrice", "pointPrice","deliveryPrice","payPrice")
                .containsExactly(100, 2, 0, 0, 0, 0, 200);
        // SKU2
        assertThat(resultBO.getItems().get(1))
                .extracting("price", "count","discountPrice" ,"couponPrice", "pointPrice","deliveryPrice","payPrice")
                .containsExactly(200, 10, 0, 0, 0, 0, 2000);
        // SKU3 未选中
        assertThat(resultBO.getItems().get(2))
                .extracting("price", "count","discountPrice" ,"couponPrice", "pointPrice","deliveryPrice","payPrice")
                .containsExactly(300, 1, 0, 0, 0, 0, 300);
    }
}