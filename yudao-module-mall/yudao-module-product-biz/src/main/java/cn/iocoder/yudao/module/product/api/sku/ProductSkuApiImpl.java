package cn.iocoder.yudao.module.product.api.sku;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.sku.dto.SkuDecrementStockBatchReqDTO;
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
 * TODO LeeYan9: 类注释;
 * @author LeeYan9
 * @since 2022-09-06
 */
@Service
@Validated
public class ProductSkuApiImpl implements ProductSkuApi {

    @Resource
    private ProductSkuMapper productSkuMapper;

    @Override
    public ProductSkuRespDTO getSku(Long id) {
        // TODO TODO LeeYan9: 需要实现
        return null;
    }

    @Override
    public List<ProductSkuRespDTO> getSkuList(Collection<Long> ids) {
        // TODO TODO LeeYan9: AllEmpty?
        if (CollectionUtils.isAnyEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ProductSkuDO> productSkuDOList = productSkuMapper.selectBatchIds(ids);
        return ProductSkuConvert.INSTANCE.convertList04(productSkuDOList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementStockBatch(SkuDecrementStockBatchReqDTO batchReqDTO) {
        // TODO @LeeYan9: 最好 Service 去 for 循环;
        productSkuMapper.decrementStockBatch(batchReqDTO.getItems());
    }
}
