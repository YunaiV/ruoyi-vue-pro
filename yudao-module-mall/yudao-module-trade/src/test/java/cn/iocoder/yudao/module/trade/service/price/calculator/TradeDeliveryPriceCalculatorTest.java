package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.member.api.address.MemberAddressApi;
import cn.iocoder.yudao.module.member.api.address.dto.MemberAddressRespDTO;
import cn.iocoder.yudao.module.trade.dal.dataobject.config.TradeConfigDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryPickUpStoreDO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryExpressChargeModeEnum;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.service.config.TradeConfigService;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryExpressTemplateService;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryPickUpStoreService;
import cn.iocoder.yudao.module.trade.service.delivery.bo.DeliveryExpressTemplateRespBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Collections;

import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PICK_UP_STORE_NOT_EXISTS;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PRICE_CALCULATE_DELIVERY_PRICE_TEMPLATE_NOT_FOUND;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PRICE_CALCULATE_DELIVERY_PRICE_TYPE_ILLEGAL;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link TradeDeliveryPriceCalculator} 的单元测试
 *
 * @author jason
 */
public class TradeDeliveryPriceCalculatorTest extends BaseMockitoUnitTest {

    @InjectMocks
    private TradeDeliveryPriceCalculator calculator;

    @Mock
    private MemberAddressApi addressApi;
    @Mock
    private DeliveryPickUpStoreService deliveryPickUpStoreService;
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
    public void init() {
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
                                .setWeight(10d).setVolume(10d).setPrice(100)
                                .setDeliveryTypes(asList(DeliveryTypeEnum.EXPRESS.getType(), DeliveryTypeEnum.PICK_UP.getType())),
                        new TradePriceCalculateRespBO.OrderItem().setDeliveryTemplateId(1L).setSkuId(20L).setCount(10).setSelected(true)
                                .setWeight(10d).setVolume(10d).setPrice(200)
                                .setDeliveryTypes(asList(DeliveryTypeEnum.EXPRESS.getType(), DeliveryTypeEnum.PICK_UP.getType())),
                        new TradePriceCalculateRespBO.OrderItem().setDeliveryTemplateId(1L).setSkuId(30L).setCount(1).setSelected(false)
                                .setWeight(10d).setVolume(10d).setPrice(300)
                                .setDeliveryTypes(asList(DeliveryTypeEnum.EXPRESS.getType(), DeliveryTypeEnum.PICK_UP.getType()))
                ));
        // 保证价格被初始化上
        TradePriceCalculatorHelper.recountPayPrice(resultBO.getItems());
        TradePriceCalculatorHelper.recountAllPrice(resultBO);

        // 准备运费模板费用配置数据：起步 10 件 1000 分，续 10 件 2000 分
        chargeBO = randomPojo(DeliveryExpressTemplateRespBO.Charge.class,
                item -> item.setStartCount(10D).setStartPrice(1000).setExtraCount(10D).setExtraPrice(2000));
        // 准备运费模板包邮配置数据：默认不包邮（订单总件数 12 不大于 包邮件数 20）
        freeBO = randomPojo(DeliveryExpressTemplateRespBO.Free.class,
                item -> item.setFreeCount(20).setFreePrice(100));
        // 准备 SP 运费模板数据
        templateRespBO = randomPojo(DeliveryExpressTemplateRespBO.class,
                item -> item.setChargeMode(DeliveryExpressChargeModeEnum.COUNT.getType())
                        .setCharge(chargeBO).setFree(freeBO));
    }

    // ========== 配送方式校验 ==========

    @Test
    @DisplayName("配送方式为空：直接返回，不计算运费")
    public void testCalculate_deliveryTypeNull() {
        // 准备参数
        reqBO.setDeliveryType(null);

        // 调用
        calculator.calculate(reqBO, resultBO);

        // 断言：未计算运费
        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(0);
    }

    @Test
    @DisplayName("配送方式与商品不匹配：抛出异常")
    public void testCalculate_deliveryTypeMismatch() {
        // 准备参数：商品 deliveryTypes 不包含 EXPRESS
        resultBO.getItems().forEach(item -> item.setDeliveryTypes(singletonList(DeliveryTypeEnum.PICK_UP.getType())));

        // 调用并断言异常
        assertServiceException(() -> calculator.calculate(reqBO, resultBO),
                PRICE_CALCULATE_DELIVERY_PRICE_TYPE_ILLEGAL);
    }

    // ========== 自提模式 ==========

    @Test
    @DisplayName("自提：未选择门店时直接返回")
    public void testCalculate_pickUp_storeIdNull() {
        // 准备参数
        reqBO.setDeliveryType(DeliveryTypeEnum.PICK_UP.getType()).setPickUpStoreId(null);

        // 调用
        calculator.calculate(reqBO, resultBO);

        // 断言：未计算运费
        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(0);
    }

    @Test
    @DisplayName("自提：门店不存在时抛出异常")
    public void testCalculate_pickUp_storeNotFound() {
        // 准备参数
        reqBO.setDeliveryType(DeliveryTypeEnum.PICK_UP.getType()).setPickUpStoreId(99L);
        // mock：门店不存在
        when(deliveryPickUpStoreService.getDeliveryPickUpStore(eq(99L))).thenReturn(null);

        // 调用并断言异常
        assertServiceException(() -> calculator.calculate(reqBO, resultBO),
                PICK_UP_STORE_NOT_EXISTS);
    }

    @Test
    @DisplayName("自提：门店被禁用时抛出异常")
    public void testCalculate_pickUp_storeDisabled() {
        // 准备参数
        reqBO.setDeliveryType(DeliveryTypeEnum.PICK_UP.getType()).setPickUpStoreId(99L);
        // mock：门店被禁用
        when(deliveryPickUpStoreService.getDeliveryPickUpStore(eq(99L))).thenReturn(
                new DeliveryPickUpStoreDO().setStatus(CommonStatusEnum.DISABLE.getStatus()));

        // 调用并断言异常
        assertServiceException(() -> calculator.calculate(reqBO, resultBO),
                PICK_UP_STORE_NOT_EXISTS);
    }

    @Test
    @DisplayName("自提：门店正常时不计算运费")
    public void testCalculate_pickUp_ok() {
        // 准备参数
        reqBO.setDeliveryType(DeliveryTypeEnum.PICK_UP.getType()).setPickUpStoreId(99L);
        // mock：门店正常
        when(deliveryPickUpStoreService.getDeliveryPickUpStore(eq(99L))).thenReturn(
                new DeliveryPickUpStoreDO().setStatus(CommonStatusEnum.ENABLE.getStatus()));

        // 调用
        calculator.calculate(reqBO, resultBO);

        // 断言：未计算运费
        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(0);
    }

    // ========== 快递模式 ==========

    @Test
    @DisplayName("快递：收件地址为空时直接返回")
    public void testCalculate_express_addressIdNull() {
        // 准备参数
        reqBO.setAddressId(null);

        // 调用
        calculator.calculate(reqBO, resultBO);

        // 断言：未计算运费
        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(0);
    }

    @Test
    @DisplayName("快递：全场包邮（开关开启 && 总金额满足全场包邮金额）")
    public void testCalculate_globalFree() {
        // mock：收件地址 + 全场包邮配置
        mockAddress();
        when(tradeConfigService.getTradeConfig()).thenReturn(new TradeConfigDO()
                .setDeliveryExpressFreeEnabled(true).setDeliveryExpressFreePrice(2200));

        // 调用
        calculator.calculate(reqBO, resultBO);

        // 断言
        TradePriceCalculateRespBO.Price price = resultBO.getPrice();
        assertThat(price)
                .extracting("totalPrice", "discountPrice", "couponPrice", "pointPrice", "deliveryPrice", "payPrice")
                .containsExactly(2200, 0, 0, 0, 0, 2200);
        assertThat(resultBO.getItems()).hasSize(3);
        assertThat(resultBO.getItems().get(0))
                .extracting("price", "count", "discountPrice", "couponPrice", "pointPrice", "deliveryPrice", "payPrice")
                .containsExactly(100, 2, 0, 0, 0, 0, 200);
        assertThat(resultBO.getItems().get(1))
                .extracting("price", "count", "discountPrice", "couponPrice", "pointPrice", "deliveryPrice", "payPrice")
                .containsExactly(200, 10, 0, 0, 0, 0, 2000);
        assertThat(resultBO.getItems().get(2))
                .extracting("price", "count", "discountPrice", "couponPrice", "pointPrice", "deliveryPrice", "payPrice")
                .containsExactly(300, 1, 0, 0, 0, 0, 300);
    }

    @Test
    @DisplayName("快递：活动包邮（freeDelivery=true）时不计算运费")
    public void testCalculate_activityFree() {
        // 准备参数
        resultBO.setFreeDelivery(true);
        // mock：收件地址
        mockAddress();

        // 调用
        calculator.calculate(reqBO, resultBO);

        // 断言：未计算运费
        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(0);
    }

    @Test
    @DisplayName("快递：找不到运费模板时抛出异常")
    public void testCalculate_express_templateNotFound() {
        // mock：收件地址 + 空模板
        mockAddress();
        when(deliveryExpressTemplateService.getExpressTemplateMapByIdsAndArea(eq(asSet(1L)), eq(10)))
                .thenReturn(Collections.emptyMap());

        // 调用并断言异常
        assertServiceException(() -> calculator.calculate(reqBO, resultBO),
                PRICE_CALCULATE_DELIVERY_PRICE_TEMPLATE_NOT_FOUND);
    }

    @Test
    @DisplayName("按件计算运费：不包邮的情况")
    public void testCalculate_expressTemplateCharge() {
        // SKU 1：100 * 2 = 200
        // SKU 2：200 * 10 = 2000
        // 运费：首件 1000 + 续件 ceil((12-10)/10)=1 倍 * 2000 = 3000
        // mock 方法：默认 freeCount=20，订单 12 件 不大于 20，不包邮
        mockAddress();
        mockTemplate(templateRespBO);

        // 调用
        calculator.calculate(reqBO, resultBO);

        // 断言
        TradePriceCalculateRespBO.Price price = resultBO.getPrice();
        assertThat(price)
                .extracting("totalPrice", "discountPrice", "couponPrice", "pointPrice", "deliveryPrice", "payPrice")
                .containsExactly(2200, 0, 0, 0, 3000, 5200);
        assertThat(resultBO.getItems()).hasSize(3);
        // 断言：SKU1 分摊运费 (int)(3000 * 2/12) = 500
        assertThat(resultBO.getItems().get(0))
                .extracting("price", "count", "discountPrice", "couponPrice", "pointPrice", "deliveryPrice", "payPrice")
                .containsExactly(100, 2, 0, 0, 0, 500, 700);
        // 断言：SKU2 分摊运费 3000 - 500 = 2500
        assertThat(resultBO.getItems().get(1))
                .extracting("price", "count", "discountPrice", "couponPrice", "pointPrice", "deliveryPrice", "payPrice")
                .containsExactly(200, 10, 0, 0, 0, 2500, 4500);
        // 断言：SKU3 未选中，不计运费
        assertThat(resultBO.getItems().get(2))
                .extracting("price", "count", "discountPrice", "couponPrice", "pointPrice", "deliveryPrice", "payPrice")
                .containsExactly(300, 1, 0, 0, 0, 0, 300);
    }

    @Test
    @DisplayName("按件计算运费：包邮（订单总件数 >= 包邮件数 && 总金额 >= 包邮金额）")
    public void testCalculate_expressTemplateFree() {
        // SKU 1：100 * 2 = 200
        // SKU 2：200 * 10 = 2000
        // 总件数 12 >= 包邮件数 10 && 总金额 2200 >= 包邮金额 1000，包邮
        templateRespBO.setFree(randomPojo(DeliveryExpressTemplateRespBO.Free.class,
                item -> item.setFreeCount(10).setFreePrice(1000)));
        mockAddress();
        mockTemplate(templateRespBO);

        // 调用
        calculator.calculate(reqBO, resultBO);

        // 断言
        TradePriceCalculateRespBO.Price price = resultBO.getPrice();
        assertThat(price)
                .extracting("totalPrice", "discountPrice", "couponPrice", "pointPrice", "deliveryPrice", "payPrice")
                .containsExactly(2200, 0, 0, 0, 0, 2200);
        assertThat(resultBO.getItems()).hasSize(3);
        assertThat(resultBO.getItems().get(0))
                .extracting("price", "count", "discountPrice", "couponPrice", "pointPrice", "deliveryPrice", "payPrice")
                .containsExactly(100, 2, 0, 0, 0, 0, 200);
        assertThat(resultBO.getItems().get(1))
                .extracting("price", "count", "discountPrice", "couponPrice", "pointPrice", "deliveryPrice", "payPrice")
                .containsExactly(200, 10, 0, 0, 0, 0, 2000);
        assertThat(resultBO.getItems().get(2))
                .extracting("price", "count", "discountPrice", "couponPrice", "pointPrice", "deliveryPrice", "payPrice")
                .containsExactly(300, 1, 0, 0, 0, 0, 300);
    }

    @Test
    @DisplayName("按件计算运费：边界场景（总件数 = 包邮件数 → 包邮）")
    public void testCalculate_expressTemplateFree_countBoundary() {
        // 总件数 12 等于 包邮件数 12 && 总金额 2200 >= 包邮金额 1000，包邮
        templateRespBO.setFree(randomPojo(DeliveryExpressTemplateRespBO.Free.class,
                item -> item.setFreeCount(12).setFreePrice(1000)));
        mockAddress();
        mockTemplate(templateRespBO);

        // 调用
        calculator.calculate(reqBO, resultBO);

        // 断言：包邮，运费 = 0
        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(0);
    }

    @Test
    @DisplayName("按件计算运费：总金额未达到包邮金额 → 不包邮")
    public void testCalculate_expressTemplateFree_priceNotMatch() {
        // 总件数 12 > 包邮件数 10，但总金额 2200 < 包邮金额 5000，不包邮
        templateRespBO.setFree(randomPojo(DeliveryExpressTemplateRespBO.Free.class,
                item -> item.setFreeCount(10).setFreePrice(5000)));
        mockAddress();
        mockTemplate(templateRespBO);

        // 调用
        calculator.calculate(reqBO, resultBO);

        // 断言：仍按运费模板计算 = 3000
        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(3000);
    }

    @Test
    @DisplayName("按件计算运费：边界场景（总金额 = 包邮金额 → 包邮）")
    public void testCalculate_expressTemplateFree_priceBoundary() {
        // 总件数 12 >= 包邮件数 10 && 总金额 2200 等于 包邮金额 2200，包邮
        templateRespBO.setFree(randomPojo(DeliveryExpressTemplateRespBO.Free.class,
                item -> item.setFreeCount(10).setFreePrice(2200)));
        mockAddress();
        mockTemplate(templateRespBO);

        // 调用
        calculator.calculate(reqBO, resultBO);

        // 断言：包邮，运费 = 0
        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(0);
    }

    @Test
    @DisplayName("按重量计算运费")
    public void testCalculate_expressTemplate_byWeight() {
        // SKU1 weight 10 * count 2 = 20
        // SKU2 weight 10 * count 10 = 100
        // 总重量 120kg
        // 运费：首件 1000 + 续件 ceil((120-10)/10)=11 倍 * 2000 = 23000
        templateRespBO.setChargeMode(DeliveryExpressChargeModeEnum.WEIGHT.getType());
        templateRespBO.setFree(randomPojo(DeliveryExpressTemplateRespBO.Free.class,
                item -> item.setFreeCount(200).setFreePrice(100)));
        mockAddress();
        mockTemplate(templateRespBO);

        // 调用
        calculator.calculate(reqBO, resultBO);

        // 断言
        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(23000);
    }

    @Test
    @DisplayName("按体积计算运费")
    public void testCalculate_expressTemplate_byVolume() {
        // SKU1 volume 10 * count 2 = 20
        // SKU2 volume 10 * count 10 = 100
        // 总体积 120m³，与按重量场景同
        templateRespBO.setChargeMode(DeliveryExpressChargeModeEnum.VOLUME.getType());
        templateRespBO.setFree(randomPojo(DeliveryExpressTemplateRespBO.Free.class,
                item -> item.setFreeCount(200).setFreePrice(100)));
        mockAddress();
        mockTemplate(templateRespBO);

        // 调用
        calculator.calculate(reqBO, resultBO);

        // 断言
        assertThat(resultBO.getPrice().getDeliveryPrice()).isEqualTo(23000);
    }

    private void mockAddress() {
        MemberAddressRespDTO addressResp = randomPojo(MemberAddressRespDTO.class, item -> item.setAreaId(10));
        when(addressApi.getAddress(eq(10L), eq(1L))).thenReturn(addressResp);
    }

    private void mockTemplate(DeliveryExpressTemplateRespBO template) {
        when(deliveryExpressTemplateService.getExpressTemplateMapByIdsAndArea(eq(asSet(1L)), eq(10)))
                .thenReturn(MapUtil.of(1L, template));
    }

}
