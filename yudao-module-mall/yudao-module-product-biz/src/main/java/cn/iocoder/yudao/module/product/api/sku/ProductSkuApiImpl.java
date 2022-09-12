package cn.iocoder.yudao.module.product.api.sku;

import cn.iocoder.yudao.module.product.api.sku.dto.SkuDecrementStockBatchReqDTO;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.convert.sku.ProductSkuConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.service.sku.ProductSkuService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * 商品 SKU API 实现类
 *
 * @author 芋道源码
 * @since 2022-08-26
 */
@Service
public class ProductSkuApiImpl implements ProductSkuApi {

    @Resource
    private ProductSkuService productSkuService;

    @Override
    public ProductSkuRespDTO getSku(Long id) {
        ProductSkuDO skuDO = productSkuService.getSku(id);
        return ProductSkuConvert.INSTANCE.convert02(skuDO);
    }

    @Override
    public List<ProductSkuRespDTO> getSkuList(Collection<Long> ids) {
        List<ProductSkuDO> list = productSkuService.getSkuList(ids);
        return ProductSkuConvert.INSTANCE.convertList02(list);
    }

    @Override
    public void decrementStockBatch(SkuDecrementStockBatchReqDTO batchReqDTO) {

    }

}
