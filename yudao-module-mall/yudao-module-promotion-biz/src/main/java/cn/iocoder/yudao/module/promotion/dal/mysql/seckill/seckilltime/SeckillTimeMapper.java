package cn.iocoder.yudao.module.promotion.dal.mysql.seckill.seckilltime;

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
        return selectList(SeckillTimeDO::getStartTime, SeckillTimeDO::getEndTime, time);
    }

    default List<SeckillTimeDO> selectListByTime(LocalTime startTime, LocalTime endTime) {
        return selectList(new LambdaQueryWrapper<SeckillTimeDO>()
                .ge(SeckillTimeDO::getStartTime, startTime)
                .le(SeckillTimeDO::getEndTime, endTime));
    }

    default void updateActivityCount(Collection<Long> ids, String type, Integer count) {
        new LambdaUpdateChainWrapper<>(this)
                .in(SeckillTimeDO::getId, ids)
                .setSql("`seckill_activity_count` = `seckill_activity_count` " + type + count)
                .update();
    }
}
