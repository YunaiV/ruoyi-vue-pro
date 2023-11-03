package cn.iocoder.yudao.module.promotion.dal.mysql.seckill.seckillactivity;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.SeckillProductDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 秒杀活动商品 Mapper
 *
 * @author halfninety
 */
@Mapper
public interface SeckillProductMapper extends BaseMapperX<SeckillProductDO> {

    default List<SeckillProductDO> selectListByActivityId(Long activityId) {
        return selectList(SeckillProductDO::getActivityId, activityId);
    }

    default SeckillProductDO selectByActivityIdAndSkuId(Long activityId, Long skuId) {
        return selectOne(SeckillProductDO::getActivityId, activityId,
                SeckillProductDO::getSkuId, skuId);
    }

    default List<SeckillProductDO> selectListByActivityId(Collection<Long> ids) {
        return selectList(SeckillProductDO::getActivityId, ids);
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
        return update(null, new LambdaUpdateWrapper<SeckillProductDO>()
                .eq(SeckillProductDO::getId, id)
                .ge(SeckillProductDO::getStock, count)
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
        return update(null, new LambdaUpdateWrapper<SeckillProductDO>()
                .eq(SeckillProductDO::getId, id)
                .setSql("stock = stock + " + count));
    }

}
