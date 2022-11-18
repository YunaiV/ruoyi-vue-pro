package cn.iocoder.yudao.module.product.convert.sku;

import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuUpdateStockReqDTO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuCreateOrUpdateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuOptionRespVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuRespVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuDetailRespVO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 商品 SKU Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface ProductSkuConvert {

    ProductSkuConvert INSTANCE = Mappers.getMapper(ProductSkuConvert.class);

    ProductSkuDO convert(ProductSkuCreateOrUpdateReqVO bean);

    ProductSkuRespVO convert(ProductSkuDO bean);

    List<ProductSkuRespVO> convertList(List<ProductSkuDO> list);

    List<ProductSkuDO> convertSkuDOList(List<ProductSkuCreateOrUpdateReqVO> list);

    ProductSkuRespDTO convert02(ProductSkuDO bean);

    List<ProductSkuRespDTO> convertList02(List<ProductSkuDO> list);

    List<ProductSpuDetailRespVO.Sku> convertList03(List<ProductSkuDO> list);

    List<ProductSkuRespDTO> convertList04(List<ProductSkuDO> list);

    List<ProductSkuOptionRespVO> convertList05(List<ProductSkuDO> skus);

    /**
     * 获得 SPU 的库存变化 Map
     *
     * @param items SKU 库存变化
     * @param skus SKU 列表
     * @return SPU 的库存变化 Map
     */
    default Map<Long, Integer> convertSpuStockMap(List<ProductSkuUpdateStockReqDTO.Item> items,
                                                  List<ProductSkuDO> skus) {
        Map<Long, Long> skuIdAndSpuIdMap = convertMap(skus, ProductSkuDO::getId, ProductSkuDO::getSpuId); // SKU 与 SKU 编号的 Map 关系
        Map<Long, Integer> spuIdAndStockMap = new HashMap<>(); // SPU 的库存变化 Map 关系
        items.forEach(item -> {
            Long spuId = skuIdAndSpuIdMap.get(item.getId());
            if (spuId == null) {
                return;
            }
            Integer stock = spuIdAndStockMap.getOrDefault(spuId, 0) + item.getIncrCount();
            spuIdAndStockMap.put(spuId, stock);
        });
        return spuIdAndStockMap;
    }

}
