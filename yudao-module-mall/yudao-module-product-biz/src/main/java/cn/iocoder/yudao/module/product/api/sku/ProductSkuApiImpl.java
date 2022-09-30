package cn.iocoder.yudao.module.product.api.sku;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.product.api.sku.dto.SkuDecrementStockBatchReqDTO;
import cn.iocoder.yudao.module.product.api.sku.dto.SkuInfoRespDTO;
import cn.iocoder.yudao.module.product.convert.sku.ProductSkuConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.mysql.sku.ProductSkuMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author LeeYan9
 * @since 2022-09-06
 */
@Service
@Validated
public class ProductSkuApiImpl implements ProductSkuApi {

    @Resource
    private ProductSkuMapper productSkuMapper;

    @Override
    public List<SkuInfoRespDTO> getSkusByIds(Collection<Long> skuIds) {
        if (CollectionUtils.isAnyEmpty(skuIds)) {
            return Collections.emptyList();
        }
        List<ProductSkuDO> productSkuDOList = productSkuMapper.selectBatchIds(skuIds);
        return ProductSkuConvert.INSTANCE.convertList03(productSkuDOList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementStockBatch(SkuDecrementStockBatchReqDTO batchReqDTO) {
        productSkuMapper.decrementStockBatch(batchReqDTO.getItems());
    }
}
