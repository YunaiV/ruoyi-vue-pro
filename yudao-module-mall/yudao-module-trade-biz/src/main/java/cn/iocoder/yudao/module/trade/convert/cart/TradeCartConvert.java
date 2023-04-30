package cn.iocoder.yudao.module.trade.convert.cart;

import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.trade.controller.app.base.sku.AppProductSkuBaseRespVO;
import cn.iocoder.yudao.module.trade.controller.app.base.spu.AppProductSpuBaseRespVO;
import cn.iocoder.yudao.module.trade.controller.app.cart.vo.AppTradeCartListRespVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.cart.TradeCartDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

@Mapper
public interface TradeCartConvert {

    TradeCartConvert INSTANCE = Mappers.getMapper(TradeCartConvert.class);

    default AppTradeCartListRespVO convertList(List<TradeCartDO> carts,
                                               List<ProductSpuRespDTO> spus, List<ProductSkuRespDTO> skus) {
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spus, ProductSpuRespDTO::getId);
        Map<Long, ProductSkuRespDTO> skuMap = convertMap(skus, ProductSkuRespDTO::getId);
        // 遍历，开始转换
        List<AppTradeCartListRespVO.Cart> validList = new ArrayList<>(carts.size());
        List<AppTradeCartListRespVO.Cart> invalidList = new ArrayList<>();
        carts.forEach(cart -> {
            AppTradeCartListRespVO.Cart cartVO = new AppTradeCartListRespVO.Cart();
            cartVO.setId(cart.getId()).setCount(cart.getCount());
            ProductSpuRespDTO spu = spuMap.get(cart.getSpuId());
            ProductSkuRespDTO sku = skuMap.get(cart.getSkuId());
            cartVO.setSpu(convert(spu)).setSku(convert(sku));
            // 如果 spu 或 sku 不存在，或者 spu 被禁用，说明是非法的，或者 sku 库存不足
            if (spu == null
                || sku == null
                || !ProductSpuStatusEnum.isEnable(spu.getStatus())
                || sku.getStock() <= 0) {
                invalidList.add(cartVO);
            } else {
                validList.add(cartVO);
            }
        });
        return new AppTradeCartListRespVO().setValidList(validList).setValidList(invalidList);
    }
    AppProductSpuBaseRespVO convert(ProductSpuRespDTO spu);
    AppProductSkuBaseRespVO convert(ProductSkuRespDTO sku);

}
