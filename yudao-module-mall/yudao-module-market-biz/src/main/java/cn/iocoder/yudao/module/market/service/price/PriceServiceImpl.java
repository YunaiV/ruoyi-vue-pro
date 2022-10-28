package cn.iocoder.yudao.module.market.service.price;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.market.api.price.dto.PriceCalculateReqDTO;
import cn.iocoder.yudao.module.market.api.price.dto.PriceCalculateRespDTO;
import cn.iocoder.yudao.module.market.convert.price.PriceConvert;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SKU_NOT_EXISTS;

/**
 * 价格计算 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PriceServiceImpl implements PriceService {

    @Resource
    private ProductSkuApi productSkuApi;

    @Override
    public PriceCalculateRespDTO calculatePrice(PriceCalculateReqDTO calculateReqDTO) {
        // 获得商品 SKU 数组
        List<ProductSkuRespDTO> skuList = checkProductSkus(calculateReqDTO);
        // 初始化 PriceCalculateRespDTO 对象
        PriceCalculateRespDTO priceCalculate = PriceConvert.INSTANCE.convert(calculateReqDTO, skuList);

        // 计算【限时折扣】促销 TODO 待实现
        // 计算【满减送】促销 TODO 待实现
        // 计算【优惠劵】促销 TODO 待实现
        return priceCalculate;
    }

    private List<ProductSkuRespDTO> checkProductSkus(PriceCalculateReqDTO calculateReqDTO) {
        // 获得商品 SKU 数组
        Map<Long, Integer> skuIdCountMap = CollectionUtils.convertMap(calculateReqDTO.getItems(),
                PriceCalculateReqDTO.Item::getSkuId, PriceCalculateReqDTO.Item::getCount);
        List<ProductSkuRespDTO> skus = productSkuApi.getSkuList(skuIdCountMap.keySet());

        // 校验商品 SKU
        skus.forEach(sku -> {
            Integer count = skuIdCountMap.get(sku.getId());
            if (count == null) {
                throw exception(SKU_NOT_EXISTS);
            }
            // 不校验库存不足，避免购物车场景，商品无货的情况
        });
        return skus;
    }

}
