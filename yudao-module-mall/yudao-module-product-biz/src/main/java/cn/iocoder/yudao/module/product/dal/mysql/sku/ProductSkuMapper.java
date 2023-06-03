package cn.iocoder.yudao.module.product.dal.mysql.sku;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ProductSkuMapper extends BaseMapperX<ProductSkuDO> {

    default List<ProductSkuDO> selectListBySpuId(Long spuId) {
        return selectList(ProductSkuDO::getSpuId, spuId);
    }

    default List<ProductSkuDO> selectListBySpuId(Collection<Long> spuIds) {
        return selectList(ProductSkuDO::getSpuId, spuIds);
    }

    default void deleteBySpuId(Long spuId) {
        delete(new LambdaQueryWrapperX<ProductSkuDO>().eq(ProductSkuDO::getSpuId, spuId));
    }

    /**
     * 更新 SKU 库存（增加）
     *
     * @param id        编号
     * @param incrCount 增加库存（正数）
     */
    default void updateStockIncr(Long id, Integer incrCount) {
        Assert.isTrue(incrCount > 0);
        LambdaUpdateWrapper<ProductSkuDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<ProductSkuDO>()
                .setSql(" stock = stock + " + incrCount)
                .eq(ProductSkuDO::getId, id);
        update(null, lambdaUpdateWrapper);
    }

    /**
     * 更新 SKU 库存（减少）
     *
     * @param id        编号
     * @param incrCount 减少库存（负数）
     * @return 更新条数
     */
    default int updateStockDecr(Long id, Integer incrCount) {
        Assert.isTrue(incrCount < 0);
        LambdaUpdateWrapper<ProductSkuDO> updateWrapper = new LambdaUpdateWrapper<ProductSkuDO>()
                .setSql(" stock = stock + " + incrCount) // 负数，所以使用 + 号
                .eq(ProductSkuDO::getId, id)
                .ge(ProductSkuDO::getStock, -incrCount); // cas 逻辑
        return update(null, updateWrapper);
    }

    default List<ProductSkuDO> selectListByAlarmStock() {
        return selectList(new QueryWrapper<ProductSkuDO>().apply("stock <= warn_stock"));
    }

    // TODO @puhui999：貌似 IN 不出来数据哈。直接全部查询出来，处理就好列；
    /**
     * 更新 sku 属性值时使用的分页查询
     *
     * @param pageParam 页面参数
     * @return {@link PageResult}<{@link ProductSkuDO}>
     */
    default PageResult<ProductSkuDO> selectPage(PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapper<ProductSkuDO>().isNotNull(ProductSkuDO::getProperties));
    }

    /**
     * 查询 sku properties 不等于 null 的数量
     *
     * @return {@link Long}
     */
    default Long selectCountByPropertyNotNull() {
        return selectCount(new LambdaQueryWrapper<ProductSkuDO>().isNotNull(ProductSkuDO::getProperties));
    }
}
