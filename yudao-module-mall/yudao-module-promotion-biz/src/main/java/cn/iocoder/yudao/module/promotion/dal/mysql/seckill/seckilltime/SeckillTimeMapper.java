package cn.iocoder.yudao.module.promotion.dal.mysql.seckill.seckilltime;

import java.time.LocalTime;
import java.util.*;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckilltime.SeckillTimeDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀时段 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface SeckillTimeMapper extends BaseMapperX<SeckillTimeDO> {
    default List<SeckillTimeDO> selectListWithTime(LocalTime time){
        if (time == null) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<SeckillTimeDO>()
                .le(SeckillTimeDO::getStartTime,time)
                .ge(SeckillTimeDO::getEndTime,time));
    }
    
    default List<SeckillTimeDO> selectListWithTime(LocalTime startTime, LocalTime endTime){
        if (startTime == null && endTime == null) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<SeckillTimeDO>()
                .ge(SeckillTimeDO::getStartTime,startTime)
                .le(SeckillTimeDO::getEndTime,endTime));
    }

    default void sekillActivityCountAdd(List<Long> ids){
        if (CollUtil.isEmpty(ids)){
            return;
        }
        new LambdaUpdateChainWrapper<SeckillTimeDO>(this)
                .in(SeckillTimeDO::getId,ids)
                .setSql("`seckill_activity_count` = `seckill_activity_count` + 1 ")
                .update();
    }

    default void sekillActivityCountReduce(List<Long> ids){
        if (CollUtil.isEmpty(ids)){
            return;
        }
        new LambdaUpdateChainWrapper<SeckillTimeDO>(this)
                .in(SeckillTimeDO::getId,ids)
                .setSql("`seckill_activity_count` = `seckill_activity_count` - 1 ")
                .update();
    }

}
