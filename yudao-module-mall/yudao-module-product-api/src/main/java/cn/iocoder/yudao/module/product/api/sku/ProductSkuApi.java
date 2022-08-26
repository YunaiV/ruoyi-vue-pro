package cn.iocoder.yudao.module.product.api.sku;

import cn.iocoder.yudao.module.product.api.sku.dto.SkuDecrementStockBatchReqDTO;
import cn.iocoder.yudao.module.product.api.sku.dto.SkuInfoRespDTO;

import java.util.Collection;
import java.util.List;

/**
 * @author LeeYan9
 * @since 2022-08-26
 */
public interface ProductSkuApi {


    /**
     * 根据skuId列表 查询sku信息
     *
     * @param skuIds sku ID列表
     * @return sku信息列表
     */
    List<SkuInfoRespDTO> getSkusByIds(Collection<Long> skuIds);

    /**
     * 批量扣减sku库存
     *
     * @param batchReqDTO sku库存信息列表
     */
    void decrementStockBatch(SkuDecrementStockBatchReqDTO batchReqDTO);
}
