package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.iocoder.yudao.module.member.api.address.AddressApi;
import cn.iocoder.yudao.module.member.api.address.dto.AddressRespDTO;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressTemplateChargeDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressTemplateDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressTemplateFreeDO;
import cn.iocoder.yudao.module.trade.dal.mysql.delivery.DeliveryExpressTemplateChargeMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.delivery.DeliveryExpressTemplateFreeMapper;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryExpressChargeModeEnum;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryExpressTemplateService;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO.OrderItem;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;

/**
 * 运费的 {@link TradePriceCalculator} 实现类
 *
 * @author jason
 */
@Component
@Order(TradePriceCalculator.ORDER_DELIVERY)
public class TradeDeliveryPriceCalculator implements TradePriceCalculator {
    @Resource
    private AddressApi addressApi;
    @Resource
    private ProductSkuApi productSkuApi;
    @Resource
    private DeliveryExpressTemplateService deliveryExpressTemplateService;
    @Resource
    private DeliveryExpressTemplateChargeMapper templateChargeMapper;
    @Resource
    private DeliveryExpressTemplateFreeMapper templateFreeMapper;

    @Override
    public void calculate(TradePriceCalculateReqBO param, TradePriceCalculateRespBO result) {
        // 1.1 判断配送方式
        if (param.getDeliveryType() == null || DeliveryTypeEnum.PICK_UP.getMode().equals(param.getDeliveryType())) {
            return;
        }

        if (param.getTemplateId() == null || param.getAddressId() == null) {
            return;
        }
        // 1.2 校验运费模板是否存在
        DeliveryExpressTemplateDO template = deliveryExpressTemplateService.validateDeliveryExpressTemplate(param.getTemplateId());

        // 得到包邮配置
        List<DeliveryExpressTemplateFreeDO> expressTemplateFreeList = templateFreeMapper.selectListByTemplateId(template.getId());
        Map<Integer, DeliveryExpressTemplateFreeDO> areaTemplateFreeMap = new HashMap<>();
        expressTemplateFreeList.forEach(item -> {
            for (Integer areaId : item.getAreaIds()) {
                // TODO 需要保证 areaId 不能重复
                if (!areaTemplateFreeMap.containsKey(areaId)) {
                    areaTemplateFreeMap.put(areaId, item);
                }
            }
        });
        // 得到快递运费配置
        List<DeliveryExpressTemplateChargeDO> expressTemplateChargeList = templateChargeMapper.selectListByTemplateId(template.getId());
        Map<Integer, DeliveryExpressTemplateChargeDO> areaTemplateChargeMap = new HashMap<>();
        expressTemplateChargeList.forEach(item -> {
            for (Integer areaId : item.getAreaIds()) {
                // areaId 不能重复
                if (!areaTemplateChargeMap.containsKey(areaId)) {
                    areaTemplateChargeMap.put(areaId, item);
                }
            }
        });
        // 得到收件地址区域
        AddressRespDTO address = addressApi.getAddress(param.getAddressId(), param.getUserId());
        // 1.3 计算快递费用
        calculateDeliveryPrice(address.getAreaId(), template.getChargeMode(),
                areaTemplateFreeMap, areaTemplateChargeMap, result);
    }

    /**
     * 校验订单是否满足包邮条件
     *
     * @param receiverAreaId        收件人地区的区域编号
     * @param chargeMode            配送计费方式
     * @param areaTemplateFreeMap   运费模板包邮区域设置 Map
     * @param areaTemplateChargeMap 运费模板快递费用设置 Map
     */
    private void calculateDeliveryPrice(Integer receiverAreaId,
                                        Integer chargeMode,
                                        Map<Integer, DeliveryExpressTemplateFreeDO> areaTemplateFreeMap,
                                        Map<Integer, DeliveryExpressTemplateChargeDO> areaTemplateChargeMap,
                                        TradePriceCalculateRespBO result) {
        // 过滤出已选中的商品SKU
        List<OrderItem> selectedItem = filterList(result.getItems(), OrderItem::getSelected);
        Set<Long> skuIds = convertSet(selectedItem, OrderItem::getSkuId);
        // 得到SKU 详情。得到 重量体积
        Map<Long, ProductSkuRespDTO> skuRespMap = convertMap(productSkuApi.getSkuList(skuIds), ProductSkuRespDTO::getId);
        // 一个 spuId 可能对应多条订单商品 SKU
        Map<Long, List<OrderItem>> spuIdItemMap = convertMultiMap(selectedItem, OrderItem::getSpuId);
        // 依次计算每个 SPU 的快递运费
        for (Map.Entry<Long, List<OrderItem>> entry : spuIdItemMap.entrySet()) {
            List<OrderItem> orderItems = entry.getValue();
            // 总件数, 总金额, 总重量， 总体积
            int totalCount = 0, totalPrice = 0;
            double totalWeight = 0;
            double totalVolume = 0;
            for (OrderItem orderItem : orderItems) {
                totalCount += orderItem.getCount();
                totalPrice += orderItem.getPrice();
                ProductSkuRespDTO skuResp = skuRespMap.get(orderItem.getSkuId());
                if (skuResp != null) {
                    totalWeight = totalWeight + skuResp.getWeight();
                    totalVolume = totalVolume + skuResp.getVolume();
                }
            }
            // 优先判断是否包邮. 如果包邮不计算快递运费
            if (areaTemplateFreeMap.containsKey(receiverAreaId) &&
                    checkExpressFree(chargeMode, totalCount, totalWeight,
                            totalVolume, totalPrice, areaTemplateFreeMap.get(receiverAreaId))) {
                continue;
            }
            // 计算快递运费
            if (areaTemplateChargeMap.containsKey(receiverAreaId)) {
                DeliveryExpressTemplateChargeDO templateCharge = areaTemplateChargeMap.get(receiverAreaId);
                DeliveryExpressChargeModeEnum chargeModeEnum = DeliveryExpressChargeModeEnum.valueOf(chargeMode);
                switch (chargeModeEnum) {
                    case PIECE: {
                        calculateExpressFeeBySpu(totalCount, templateCharge, orderItems);
                        break;
                    }
                    case WEIGHT: {
                        calculateExpressFeeBySpu(totalWeight, templateCharge, orderItems);
                        break;
                    }
                    case VOLUME: {
                        calculateExpressFeeBySpu(totalVolume, templateCharge, orderItems);
                        break;
                    }
                }
            }
        }
        TradePriceCalculatorHelper.recountAllPrice(result);
    }

    /**
     * 按 spu 来计算快递费用
     *
     * @param total          总件数/总重量/总体积
     * @param templateCharge 快递运费配置
     * @param orderItems     SKU 商品项目
     */
    private void calculateExpressFeeBySpu(double total, DeliveryExpressTemplateChargeDO templateCharge, List<OrderItem> orderItems) {
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
        //
        // TODO @芋艿 分摊快递费用到 SKU. 是不是搞复杂了
        divideDeliveryPrice(deliveryPrice, orderItems);
    }

    /**
     * 快递运费分摊到每个 SKU 商品上
     *
     * @param deliveryPrice 快递运费
     * @param orderItems    SKU 商品
     */
    private void divideDeliveryPrice(int deliveryPrice, List<OrderItem> orderItems) {
        int dividePrice = deliveryPrice / orderItems.size();
        for (OrderItem item : orderItems) {
            // 更新快递运费
            item.setDeliveryPrice(dividePrice);
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
    private boolean checkExpressFree(Integer chargeMode, int totalCount, double totalWeight,
                                     double totalVolume, int totalPrice, DeliveryExpressTemplateFreeDO templateFree) {
        DeliveryExpressChargeModeEnum chargeModeEnum = DeliveryExpressChargeModeEnum.valueOf(chargeMode);
        switch (chargeModeEnum) {
            case PIECE:
                // 两个条件都满足才包邮
                if (totalCount >= templateFree.getFreeCount() && totalPrice >= templateFree.getFreePrice()) {
                    return true;
                }
                break;
            case WEIGHT:
                //  freeCount 是不是应该是 double ??
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
