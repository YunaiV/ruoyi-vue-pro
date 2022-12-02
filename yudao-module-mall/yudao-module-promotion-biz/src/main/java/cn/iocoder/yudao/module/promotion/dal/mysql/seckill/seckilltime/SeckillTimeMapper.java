package cn.iocoder.yudao.module.promotion.dal.mysql.seckill.seckilltime;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckilltime.SeckillTimeDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

/**
 * 秒杀时段 Mapper
 *
 * @author halfninety
 */
@Mapper
public interface SeckillTimeMapper extends BaseMapperX<SeckillTimeDO> {

    default List<SeckillTimeDO> selectListByTime(LocalTime time) {
        return selectList(SeckillTimeDO::getStartTime,SeckillTimeDO::getEndTime,time);
    }

    default List<SeckillTimeDO> selectListByTime(LocalTime startTime, LocalTime endTime) {
        return selectList(new LambdaQueryWrapper<SeckillTimeDO>()
                .ge(SeckillTimeDO::getStartTime, startTime)
                .le(SeckillTimeDO::getEndTime, endTime));
    }

    // TODO @halfninety：updateActivityCount + 和 -，可以执使用一个方法实现哈。多传递一个参数
    default void sekillActivityCountAdd(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        new LambdaUpdateChainWrapper<SeckillTimeDO>(this)
                .in(SeckillTimeDO::getId, ids)
                .setSql("`seckill_activity_count` = `seckill_activity_count` + 1 ")
                .update();
    }

    default void sekillActivityCountReduce(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        new LambdaUpdateChainWrapper<SeckillTimeDO>(this)
                .in(SeckillTimeDO::getId, ids)
                .setSql("`seckill_activity_count` = `seckill_activity_count` - 1 ")
                .update();
    }

}
