package cn.iocoder.yudao.module.trade.service.price.calculator;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.module.member.api.address.AddressApi;
import cn.iocoder.yudao.module.member.api.address.dto.AddressRespDTO;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressTemplateChargeDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressTemplateFreeDO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryExpressChargeModeEnum;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.service.delivery.DeliveryExpressTemplateService;
import cn.iocoder.yudao.module.trade.service.delivery.bo.SpuDeliveryExpressTemplateRespBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateReqBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO;
import cn.iocoder.yudao.module.trade.service.price.bo.TradePriceCalculateRespBO.OrderItem;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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

    @Override
    public void calculate(TradePriceCalculateReqBO param, TradePriceCalculateRespBO result) {
        // 1.1 判断配送方式
        if (param.getDeliveryType() == null || DeliveryTypeEnum.PICK_UP.getMode().equals(param.getDeliveryType())) {
            return;
        }
        // 1.2 得到收件地址区域
        if (param.getAddressId() == null) {
            return;
        }
        AddressRespDTO address = addressApi.getAddress(param.getAddressId(), param.getUserId());
        Assert.notNull(address, "收件人({})的地址，不能为空", param.getUserId());

        // 2. 过滤出已选中的商品SKU
        List<OrderItem> selectedItem = filterList(result.getItems(), OrderItem::getSelected);
        Set<Long> spuIds = convertSet(selectedItem, OrderItem::getSpuId);
        Map<Long, SpuDeliveryExpressTemplateRespBO> spuExpressTemplateMap =
                deliveryExpressTemplateService.getExpressTemplateBySpuIdsAndArea(spuIds, address.getAreaId());

        // 3. 计算配送费用
        // TODO @jason：这里应该不能判断空；如果找不到模版，就要报错；不然配送费就亏了
        if (CollUtil.isNotEmpty(spuExpressTemplateMap)) {
            calculateDeliveryPrice(selectedItem, spuExpressTemplateMap, result);
        }
    }

    private void calculateDeliveryPrice(List<OrderItem> selectedSkus,
                                        Map<Long, SpuDeliveryExpressTemplateRespBO> spuExpressTemplateMap,
                                        TradePriceCalculateRespBO result) {
        // 得到 SKU 详情
        // TODO @jason：可以去掉这里的读取；在 TradePriceCalculateRespBO 初始化的时候，把 weight、volume 拿到
        Set<Long> skuIds = convertSet(selectedSkus, OrderItem::getSkuId);
        Map<Long, ProductSkuRespDTO> skuRespMap = convertMap(productSkuApi.getSkuList(skuIds), ProductSkuRespDTO::getId);
        // 按 SPU 来计算商品的运费：一个 spuId 可能对应多条订单商品 SKU
        Map<Long, List<OrderItem>> spuIdItemMap = convertMultiMap(selectedSkus, OrderItem::getSpuId);

        // 依次计算每个 SPU 的快递运费
        for (Map.Entry<Long, List<OrderItem>> entry : spuIdItemMap.entrySet()) {
            Long spuId  = entry.getKey();
            List<OrderItem> orderItems = entry.getValue();
            // TODO @jason：如果找不到，则打印 error log
            SpuDeliveryExpressTemplateRespBO templateBO = spuExpressTemplateMap.get(spuId);
            if (templateBO == null) {
                // 记录错误日志
                continue;
            }
            // 总件数, 总金额, 总重量， 总体积
            int totalCount = 0;
            int totalPrice = 0;
            double totalWeight = 0;
            double totalVolume = 0;
            for (OrderItem orderItem : orderItems) {
                totalCount += orderItem.getCount();
                totalPrice += orderItem.getPayPrice(); // 先按应付总金额来算，后面确认一下 TODO @jason：是的哈
                ProductSkuRespDTO skuResp = skuRespMap.get(orderItem.getSkuId());
                // TODO @jason：是不是要保持风格统一，都用 +=
                totalWeight = totalWeight + skuResp.getWeight() * orderItem.getCount();
                totalVolume = totalVolume + skuResp.getVolume() * orderItem.getCount();
            }
            // 优先判断是否包邮. 如果包邮不计算快递运费
            if (checkExpressFree(templateBO.getChargeMode(), totalCount, totalWeight,
                            totalVolume, totalPrice, templateBO.getTemplateFree())) {
                continue;
            }
            // TODO @jason：这块判断，可以收到 calculateExpressFeeByChargeMode 里；另外找不到，要打 error log
            if (templateBO.getTemplateCharge() == null) {
                continue;
            }
            // 计算快递运费
            calculateExpressFeeByChargeMode(totalCount, totalWeight, totalVolume,
                    templateBO.getChargeMode(), templateBO.getTemplateCharge(), orderItems);

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
                                                 int chargeMode, DeliveryExpressTemplateChargeDO templateCharge,
                                                 List<OrderItem> orderItems) {
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
    private void calculateExpressFee(double total, DeliveryExpressTemplateChargeDO templateCharge, List<OrderItem> orderItems) {
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
        // TODO @jason：分摊的话，是不是要按照比例呀？重量、价格、数量等等
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
    // TODO @jason：isExpressFree 更合适；因为 check 是一种校验，往往抛出异常；
    private boolean checkExpressFree(Integer chargeMode, int totalCount, double totalWeight,
                                     double totalVolume, int totalPrice, DeliveryExpressTemplateFreeDO templateFree) {
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
