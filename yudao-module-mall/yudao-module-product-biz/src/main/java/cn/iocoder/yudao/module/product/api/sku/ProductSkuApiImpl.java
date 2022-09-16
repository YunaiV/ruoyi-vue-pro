package cn.iocoder.yudao.module.product.api.sku;

import cn.iocoder.yudao.module.product.api.sku.dto.SkuDecrementStockBatchReqDTO;
import cn.iocoder.yudao.module.product.api.sku.dto.SkuInfoRespDTO;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * todo 注释
 */
@Service
public class ProductSkuApiImpl implements ProductSkuApi {

    @Override
    public List<SkuInfoRespDTO> getSkusByIds(Collection<Long> skuIds) {
        return null;
    }

    @Override
    public void decrementStockBatch(SkuDecrementStockBatchReqDTO batchReqDTO) {

    }

}
