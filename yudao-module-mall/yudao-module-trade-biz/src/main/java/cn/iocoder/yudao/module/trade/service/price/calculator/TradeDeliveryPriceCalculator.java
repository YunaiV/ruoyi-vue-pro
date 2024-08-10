package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
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
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO.OrderItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.*;

/**
 * 运费的 {@link TradePriceCalculator} 实现类
 *
 * @author jason
 */
@Component
@Order(TradePriceCalculator.ORDER_DELIVERY)
@Slf4j
public class TradeDeliveryPriceCalculator implements TradePriceCalculator {

    @Resource
    private MemberAddressApi addressApi;

    @Resource
    private DeliveryPickUpStoreService deliveryPickUpStoreService;
    @Resource
    private DeliveryExpressTemplateService deliveryExpressTemplateService;
    @Resource
    private TradeConfigService tradeConfigService;

    @Override
    public void calculate(TradePriceCalculateReqBO param, TradePriceCalculateRespBO result) {
        if (param.getDeliveryType() == null) {
            return;
        }
        // TODO @puhui999：需要校验，是不是存在商品不能门店自提，或者不能快递发货的情况。就是说，配送方式不匹配哈
        if (DeliveryTypeEnum.PICK_UP.getType().equals(param.getDeliveryType())) {
            calculateByPickUp(param);
        } else if (DeliveryTypeEnum.EXPRESS.getType().equals(param.getDeliveryType())) {
            calculateExpress(param, result);
        }
    }

    private void calculateByPickUp(TradePriceCalculateReqBO param) {
        if (param.getPickUpStoreId() == null) {
            // 价格计算时，如果为空就不算~最终下单，会校验该字段不允许空
            return;
        }
        DeliveryPickUpStoreDO pickUpStore = deliveryPickUpStoreService.getDeliveryPickUpStore(param.getPickUpStoreId());
        if (pickUpStore == null || CommonStatusEnum.DISABLE.getStatus().equals(pickUpStore.getStatus())) {
            throw exception(PICK_UP_STORE_NOT_EXISTS);
        }
    }

    // ========= 快递发货 ==========

    private void calculateExpress(TradePriceCalculateReqBO param, TradePriceCalculateRespBO result) {
        // 0. 得到收件地址区域
        if (param.getAddressId() == null) {
            // 价格计算时，如果为空就不算~最终下单，会校验该字段不允许空
            return;
        }
        MemberAddressRespDTO address = addressApi.getAddress(param.getAddressId(), param.getUserId());
        Assert.notNull(address, "收件人({})的地址，不能为空", param.getUserId());

        // 情况一：全局包邮
        if (isGlobalExpressFree(result)) {
            return;
        }

        // 情况二：快递模版
        // 2.1 过滤出已选中的商品 SKU
        List<OrderItem> selectedItem = filterList(result.getItems(), OrderItem::getSelected);
        Set<Long> deliveryTemplateIds = convertSet(selectedItem, OrderItem::getDeliveryTemplateId);
        Map<Long, DeliveryExpressTemplateRespBO> expressTemplateMap =
                deliveryExpressTemplateService.getExpressTemplateMapByIdsAndArea(deliveryTemplateIds, address.getAreaId());
        // 2.2 计算配送费用
        if (CollUtil.isEmpty(expressTemplateMap)) {
            log.error("[calculate][找不到商品 templateIds {} areaId{} 对应的运费模板]", deliveryTemplateIds, address.getAreaId());
            throw exception(PRICE_CALCULATE_DELIVERY_PRICE_TEMPLATE_NOT_FOUND);
        }
        calculateDeliveryPrice(selectedItem, expressTemplateMap, result);
    }

    /**
     * 是否全局包邮
     *
     * @param result 计算结果
     * @return 是否包邮
     */
    private boolean isGlobalExpressFree(TradePriceCalculateRespBO result) {
        TradeConfigDO config = tradeConfigService.getTradeConfig();
        return config != null
                && Boolean.TRUE.equals(config.getDeliveryExpressFreeEnabled()) // 开启包邮
                && result.getPrice().getPayPrice() >= config.getDeliveryExpressFreePrice(); // 满足包邮的价格
    }

    private void calculateDeliveryPrice(List<OrderItem> selectedSkus,
                                        Map<Long, DeliveryExpressTemplateRespBO> expressTemplateMap,
                                        TradePriceCalculateRespBO result) {
        // 按商品运费模板来计算商品的运费：相同的运费模板可能对应多条订单商品 SKU
        Map<Long, List<OrderItem>> template2ItemMap = convertMultiMap(selectedSkus, OrderItem::getDeliveryTemplateId);
        // 依次计算快递运费
        for (Map.Entry<Long, List<OrderItem>> entry : template2ItemMap.entrySet()) {
            Long templateId  = entry.getKey();
            List<OrderItem> orderItems = entry.getValue();
            DeliveryExpressTemplateRespBO templateBO = expressTemplateMap.get(templateId);
            if (templateBO == null) {
                log.error("[calculateDeliveryPrice][不能计算快递运费，找不到 templateId({}) 对应的运费模板配置]", templateId);
                continue;
            }
            // 1. 优先判断是否包邮。如果包邮不计算快递运费
            if (isExpressTemplateFree(orderItems, templateBO.getChargeMode(), templateBO.getFree())) {
                continue;
            }
            // 2. 计算快递运费
            calculateExpressFeeByChargeMode(orderItems, templateBO.getChargeMode(), templateBO.getCharge());
        }
        TradePriceCalculatorHelper.recountAllPrice(result);
    }

    /**
     * 按配送方式来计算运费
     *
     * @param orderItems SKU 商品项目
     * @param chargeMode  配送计费方式
     * @param templateCharge 快递运费配置
     */
    private void calculateExpressFeeByChargeMode(List<OrderItem> orderItems, Integer chargeMode,
                                                 DeliveryExpressTemplateRespBO.Charge templateCharge) {
        if (templateCharge == null) {
            log.error("[calculateExpressFeeByChargeMode][计算快递运费时，找不到 SKU({}) 对应的运费模版]", orderItems);
            return;
        }
        double totalChargeValue = getTotalChargeValue(orderItems, chargeMode);
        // 1. 计算 SKU 商品快递费用
        int deliveryPrice;
        if (totalChargeValue <= templateCharge.getStartCount()) {
            deliveryPrice = templateCharge.getStartPrice();
        } else {
            double remainWeight = totalChargeValue - templateCharge.getStartCount();
            // 剩余重量/ 续件 = 续件的次数. 向上取整
            int extraNum = (int) Math.ceil(remainWeight / templateCharge.getExtraCount());
            int extraPrice = templateCharge.getExtraPrice() * extraNum;
            deliveryPrice = templateCharge.getStartPrice() + extraPrice;
        }

        // 2. 分摊快递费用到 SKU. 退费的时候，可能按照 SKU 考虑退费金额
        int remainPrice = deliveryPrice;
        for (int i = 0; i < orderItems.size(); i++) {
            TradePriceCalculateRespBO.OrderItem item = orderItems.get(i);
            int partPrice;
            double chargeValue = getChargeValue(item, chargeMode);
            if (i < orderItems.size() - 1) { // 减一的原因，是因为拆分时，如果按照比例，可能会出现.所以最后一个，使用反减
                partPrice = (int) (deliveryPrice * (chargeValue / totalChargeValue));
                remainPrice -= partPrice;
            } else {
                partPrice = remainPrice;
            }
            Assert.isTrue(partPrice >= 0, "分摊金额必须大于等于 0");
            // 更新快递运费
            item.setDeliveryPrice(partPrice);
            TradePriceCalculatorHelper.recountPayPrice(item);
        }
    }

    /**
     * 检查是否包邮
     *
     * @param chargeMode   配送计费方式
     * @param templateFree 包邮配置
     */
    private boolean isExpressTemplateFree(List<OrderItem> orderItems, Integer chargeMode,
                                          DeliveryExpressTemplateRespBO.Free templateFree) {
        if (templateFree == null) {
            return false;
        }
        double totalChargeValue = getTotalChargeValue(orderItems, chargeMode);
        double totalPrice = TradePriceCalculatorHelper.calculateTotalPayPrice(orderItems);
        return totalChargeValue >= templateFree.getFreeCount() && totalPrice >= templateFree.getFreePrice();
    }

    private double getTotalChargeValue(List<OrderItem> orderItems, Integer chargeMode) {
        double total = 0;
        for (OrderItem orderItem : orderItems) {
            total += getChargeValue(orderItem, chargeMode);
        }
        return total;
    }

    private double getChargeValue(OrderItem orderItem, Integer chargeMode) {
        DeliveryExpressChargeModeEnum chargeModeEnum = DeliveryExpressChargeModeEnum.valueOf(chargeMode);
        switch (chargeModeEnum) {
            case COUNT:
                return orderItem.getCount();
            case WEIGHT:
                return orderItem.getWeight() != null ? orderItem.getWeight() * orderItem.getCount() : 0;
            case VOLUME:
                return orderItem.getVolume() != null ? orderItem.getVolume() * orderItem.getCount() : 0;
            default:
                throw new IllegalArgumentException(StrUtil.format("未知的计费模式({})", chargeMode));
        }
    }

}
