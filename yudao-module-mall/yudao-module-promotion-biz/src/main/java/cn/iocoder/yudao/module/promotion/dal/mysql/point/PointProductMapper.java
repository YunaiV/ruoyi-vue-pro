package cn.iocoder.yudao.module.promotion.dal.mysql.point;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.promotion.dal.dataobject.point.PointProductDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 积分商城商品 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface PointProductMapper extends BaseMapperX<PointProductDO> {

    default List<PointProductDO> selectListByActivityId(Collection<Long> activityIds) {
        return selectList(PointProductDO::getActivityId, activityIds);
    }

    default List<PointProductDO> selectListByActivityId(Long activityId) {
        return selectList(PointProductDO::getActivityId, activityId);
    }

    default void updateByActivityId(PointProductDO pointProductDO) {
        update(pointProductDO, new LambdaUpdateWrapper<PointProductDO>()
                .eq(PointProductDO::getActivityId, pointProductDO.getActivityId()));
    }

    default PointProductDO selectListByActivityIdAndSkuId(Long activityId, Long skuId) {
        return selectOne(PointProductDO::getActivityId, activityId,
                PointProductDO::getSkuId, skuId);
    }

    /**
     * 更新活动库存（减少）
     *
     * @param id    活动编号
     * @param count 扣减的库存数量(减少库存)
     * @return 影响的行数
     */
    default int updateStockDecr(Long id, int count) {
        Assert.isTrue(count > 0);
        return update(null, new LambdaUpdateWrapper<PointProductDO>()
                .eq(PointProductDO::getId, id)
                .ge(PointProductDO::getStock, count)
                .setSql("stock = stock - " + count));
    }

    /**
     * 更新活动库存（增加）
     *
     * @param id    活动编号
     * @param count 需要增加的库存（增加库存）
     * @return 影响的行数
     */
    default int updateStockIncr(Long id, int count) {
        Assert.isTrue(count > 0);
        return update(null, new LambdaUpdateWrapper<PointProductDO>()
                .eq(PointProductDO::getId, id)
                .setSql("stock = stock + " + count));
    }
}