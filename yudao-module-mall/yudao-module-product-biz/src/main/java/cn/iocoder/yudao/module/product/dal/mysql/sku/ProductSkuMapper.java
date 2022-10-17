package cn.iocoder.yudao.module.product.dal.mysql.sku;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.product.api.sku.dto.SkuDecrementStockBatchReqDTO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品 SKU Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ProductSkuMapper extends BaseMapperX<ProductSkuDO> {

    default List<ProductSkuDO> selectListBySpuId(Long spuId) {
        return selectList(ProductSkuDO::getSpuId, spuId);
    }

    default void deleteBySpuId(Long spuId) {
        delete(new LambdaQueryWrapperX<ProductSkuDO>()
                .eqIfPresent(ProductSkuDO::getSpuId, spuId));
    }

    default void decrementStockBatch(List<SkuDecrementStockBatchReqDTO.Item> items) {
        for (SkuDecrementStockBatchReqDTO.Item item : items) {
            // 扣减库存 cas 逻辑
            LambdaUpdateWrapper<ProductSkuDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<ProductSkuDO>()
                    .setSql(" stock = stock-" + item.getCount())
                    .eq(ProductSkuDO::getSpuId, item.getProductId())
                    .eq(ProductSkuDO::getId, item.getSkuId())
                    .ge(ProductSkuDO::getStock, item.getCount());
            // 执行
            this.update(null, lambdaUpdateWrapper);
        }
    }
    default List<ProductSkuDO> selectRemindSpuIds(){
       return selectList(new QueryWrapper<ProductSkuDO>().apply("stock <= warn_stock"));
    }
}
