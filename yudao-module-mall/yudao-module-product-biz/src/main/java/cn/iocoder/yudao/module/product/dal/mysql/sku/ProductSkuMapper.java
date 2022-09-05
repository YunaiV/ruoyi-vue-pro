package cn.iocoder.yudao.module.product.dal.mysql.sku;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品 SKU Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ProductSkuMapper extends BaseMapperX<ProductSkuDO> {

    // TODO @franky：方法名 selectList; 可以直接调用 selectList
    default List<ProductSkuDO> selectListBySpuIds(List<Long> spuIds) {
        return selectList(ProductSkuDO::getSpuId, spuIds);
    }

    default List<ProductSkuDO> selectListBySpuId(Long spuId) {
        return selectList(ProductSkuDO::getSpuId, spuId);
    }

    default void deleteBySpuId(Long spuId) {
        // TODO @franky：直接 delete(new XXX) 即可，更简洁一些
        LambdaQueryWrapperX<ProductSkuDO> lambdaQueryWrapperX = new LambdaQueryWrapperX<ProductSkuDO>()
                .eqIfPresent(ProductSkuDO::getSpuId, spuId);
        delete(lambdaQueryWrapperX);
    }

}
