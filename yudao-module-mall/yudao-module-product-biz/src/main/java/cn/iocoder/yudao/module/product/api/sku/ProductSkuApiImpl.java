package cn.iocoder.yudao.module.product.api.sku;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuUpdateStockReqDTO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.service.sku.ProductSkuService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * 商品 SKU API 实现类
 *
 * @author LeeYan9
 * @since 2022-09-06
 */
@Service
@Validated
public class ProductSkuApiImpl implements ProductSkuApi {

    @Resource
    private ProductSkuService productSkuService;

    @Override
    public ProductSkuRespDTO getSku(Long id) {
        ProductSkuDO sku = productSkuService.getSku(id);
        return BeanUtils.toBean(sku, ProductSkuRespDTO.class);
    }

    @Override
    public List<ProductSkuRespDTO> getSkuList(Collection<Long> ids) {
        List<ProductSkuDO> skus = productSkuService.getSkuList(ids);
        return BeanUtils.toBean(skus, ProductSkuRespDTO.class);
    }

    @Override
    public List<ProductSkuRespDTO> getSkuListBySpuId(Collection<Long> spuIds) {
        List<ProductSkuDO> skus = productSkuService.getSkuListBySpuId(spuIds);
        return BeanUtils.toBean(skus, ProductSkuRespDTO.class);
    }

    @Override
    public void updateSkuStock(ProductSkuUpdateStockReqDTO updateStockReqDTO) {
        productSkuService.updateSkuStock(updateStockReqDTO);
    }

}
