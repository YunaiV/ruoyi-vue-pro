package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.module.member.api.address.AddressApi;
import cn.iocoder.yudao.module.member.api.address.dto.AddressRespDTO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryExpressChargeModeEnum;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryExpressTemplateService;
import cn.iocoder.yudao.module.trade.service.delivery.bo.DeliveryExpressTemplateRespBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO.OrderItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PRICE_CALCULATE_DELIVERY_PRICE_USER_ADDR_IS_EMPTY;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.PRICE_CALCULATE_DELIVERY_PRICE_TEMPLATE_NOT_FOUND;

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
    private AddressApi addressApi;
    @Resource
    private DeliveryExpressTemplateService deliveryExpressTemplateService;

    @Override
    public void calculate(TradePriceCalculateReqBO param, TradePriceCalculateRespBO result) {
        // TODO @芋艿：如果门店自提，需要校验是否开启；
        // 1.1 判断配送方式
        if (param.getDeliveryType() == null || DeliveryTypeEnum.PICK_UP.getMode().equals(param.getDeliveryType())) {
            return;
        }
        if (param.getAddressId() == null) {
            throw exception(PRICE_CALCULATE_DELIVERY_PRICE_USER_ADDR_IS_EMPTY);
        }
        // 1.2 得到收件地址区域
        AddressRespDTO address = addressApi.getAddress(param.getAddressId(), param.getUserId());
        Assert.notNull(address, "收件人({})的地址，不能为空", param.getUserId());

        // 2. 过滤出已选中的商品SKU
        List<OrderItem> selectedItem = filterList(result.getItems(), OrderItem::getSelected);
        Set<Long> deliveryTemplateIds = convertSet(selectedItem, OrderItem::getDeliveryTemplateId);
        Map<Long, DeliveryExpressTemplateRespBO> expressTemplateMap =
                deliveryExpressTemplateService.getExpressTemplateMapByIdsAndArea(deliveryTemplateIds, address.getAreaId());
        // 3. 计算配送费用
        if (CollUtil.isEmpty(expressTemplateMap)) {
            log.error("[calculate][找不到商品 templateIds {} areaId{} 对应的运费模板]", deliveryTemplateIds, address.getAreaId());
            throw exception(PRICE_CALCULATE_DELIVERY_PRICE_TEMPLATE_NOT_FOUND);
        }
        calculateDeliveryPrice(selectedItem, expressTemplateMap, result);
    }

    private void calculateDeliveryPrice(List<OrderItem> selectedSkus,
                                        Map<Long, DeliveryExpressTemplateRespBO> expressTemplateMap,
                                        TradePriceCalculateRespBO result) {
        // 按商品运费模板来计算商品的运费：相同的运费模板可能对应多条订单商品 SKU
        Map<Long, List<OrderItem>> tplIdItemMap = convertMultiMap(selectedSkus, OrderItem::getDeliveryTemplateId);
        // 依次计算快递运费
        for (Map.Entry<Long, List<OrderItem>> entry : tplIdItemMap.entrySet()) {
            Long templateId  = entry.getKey();
            List<OrderItem> orderItems = entry.getValue();
            DeliveryExpressTemplateRespBO templateBO = expressTemplateMap.get(templateId);
            if (templateBO == null) {
                log.error("[calculateDeliveryPrice][不能计算快递运费，找不到 templateId({}) 对应的运费模板配置]", templateId);
                continue;
            }
            // 总件数, 总金额, 总重量， 总体积
            int totalCount = 0;
            int totalPrice = 0;
            double totalWeight = 0;
            double totalVolume = 0;
            for (OrderItem orderItem : orderItems) {
                totalCount  += orderItem.getCount();
                totalPrice  += orderItem.getPayPrice();
                totalWeight += totalWeight + orderItem.getWeight() * orderItem.getCount();
                totalVolume += totalVolume + orderItem.getVolume() * orderItem.getCount();
            }
            // 优先判断是否包邮. 如果包邮不计算快递运费
            if (isExpressFree(templateBO.getChargeMode(), totalCount, totalWeight,
                            totalVolume, totalPrice, templateBO.getFree())) {
                continue;
            }
            // 计算快递运费
            calculateExpressFeeByChargeMode(totalCount, totalWeight, totalVolume,
                    templateBO.getChargeMode(), templateBO.getCharge(), orderItems);

        }
        TradePriceCalculatorHelper.recountAllPrice(result);
    }

    /**
     * 按配送方式来计算运费
     *
     * @param totalCount  总件数
     * @param totalWeight 总重量
     * @param totalVolume 总体积
     * @param chargeMode  配送计费方式
     * @param templateCharge 快递运费配置
     * @param orderItems SKU 商品项目
     */
    private void calculateExpressFeeByChargeMode(double totalCount, double totalWeight, double totalVolume,
                                                 int chargeMode, DeliveryExpressTemplateRespBO.Charge templateCharge,
                                                 List<OrderItem> orderItems) {
        if (templateCharge == null) {
            log.error("[calculateExpressFeeByChargeMode][计算快递运费时，找不到 SKU({}) 对应的运费模版]", orderItems);
            return;
        }
        DeliveryExpressChargeModeEnum chargeModeEnum = DeliveryExpressChargeModeEnum.valueOf(chargeMode);
        switch (chargeModeEnum) {
            case PIECE: {
                calculateExpressFee(totalCount, templateCharge, orderItems);
                break;
            }
            case WEIGHT: {
                calculateExpressFee(totalWeight, templateCharge, orderItems);
                break;
            }
            case VOLUME: {
                calculateExpressFee(totalVolume, templateCharge, orderItems);
                break;
            }
        }
    }

    /**
     * 计算 SKU 商品快递费用
     *
     * @param total          总件数/总重量/总体积
     * @param templateCharge 快递运费配置
     * @param orderItems     SKU 商品项目
     */
    private void calculateExpressFee(double total, DeliveryExpressTemplateRespBO.Charge templateCharge, List<OrderItem> orderItems) {
        int deliveryPrice;
        if (total <= templateCharge.getStartCount()) {
            deliveryPrice = templateCharge.getStartPrice();
        } else {
            double remainWeight = total - templateCharge.getStartCount();
            // 剩余重量/ 续件 = 续件的次数. 向上取整
            int extraNum = (int) Math.ceil(remainWeight / templateCharge.getExtraCount());
            int extraPrice = templateCharge.getExtraPrice() * extraNum;
            deliveryPrice = templateCharge.getStartPrice() + extraPrice;
        }
        // 分摊快递费用到 SKU. 退费的时候，可能按照 SKU 考虑退费金额
        divideDeliveryPrice(deliveryPrice, orderItems);
    }

    /**
     * 快递运费分摊到每个 SKU 商品上
     *
     * @param deliveryPrice 快递运费
     * @param orderItems    SKU 商品
     */
    private void divideDeliveryPrice(int deliveryPrice, List<OrderItem> orderItems) {
        // TODO @jason：分摊的话，是不是要按照比例呀？重量、价格、数量等等,
        //  按比例是不是有点复杂。后面看看是否需要；
        // TODO 可以看看别的项目怎么搞的哈。
        int dividePrice = deliveryPrice / orderItems.size();
        for (OrderItem item : orderItems) {
            // 更新快递运费
            item.setDeliveryPrice(dividePrice);
            TradePriceCalculatorHelper.recountPayPrice(item);
        }
    }

    /**
     * 检查是否包邮
     *
     * @param chargeMode   配送计费方式
     * @param totalCount   总件数
     * @param totalWeight  总重量
     * @param totalVolume  总体积
     * @param totalPrice   总金额
     * @param templateFree 包邮配置
     */
    private boolean isExpressFree(Integer chargeMode, int totalCount, double totalWeight,
                                  double totalVolume, int totalPrice, DeliveryExpressTemplateRespBO.Free templateFree) {
        if (templateFree == null) {
            return false;
        }
        DeliveryExpressChargeModeEnum chargeModeEnum = DeliveryExpressChargeModeEnum.valueOf(chargeMode);
        switch (chargeModeEnum) {
            case PIECE:
                // 两个条件都满足才包邮
                if (totalCount >= templateFree.getFreeCount() && totalPrice >= templateFree.getFreePrice()) {
                    return true;
                }
                break;
            case WEIGHT:
                // freeCount 是不是应该是 double ??
                // TODO @jason：要不配置的时候，把它的单位和商品对齐？到底是 kg、还是斤
                // TODO @芋艿 目前 包邮 件数/重量/体积 都用的是这个字段
                // TODO @jason：那要不快递模版也改成 kg？这样是不是就不用 double ？
                if (totalWeight >= templateFree.getFreeCount()
                        && totalPrice >= templateFree.getFreePrice()) {
                    return true;
                }
                break;
            case VOLUME:
                if (totalVolume >= templateFree.getFreeCount()
                        && totalPrice >= templateFree.getFreePrice()) {
                    return true;
                }
                break;
        }
        return false;
    }
}
