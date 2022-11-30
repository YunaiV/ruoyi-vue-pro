package cn.iocoder.yudao.module.promotion.dal.mysql.seckill.seckilltime;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckilltime.SeckillTimeDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

/**
 * 秒杀时段 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface SeckillTimeMapper extends BaseMapperX<SeckillTimeDO> {

    // @TODO halfninety：){ 之间要有空格哈
    default List<SeckillTimeDO> selectListWithTime(LocalTime time){
        // TODO @halfninety：mapper 层，不做空判断，业务上面自己的保证
        if (time == null) {
            return Collections.emptyList();
        }
        // TODO @halfninety：在 BaseMapperX 上，可以封装一个 2 个字段的检索哈
        return selectList(new LambdaQueryWrapper<SeckillTimeDO>()
                .le(SeckillTimeDO::getStartTime,time)
                .ge(SeckillTimeDO::getEndTime,time));
    }

    // TODO @halfninety：selectListByXXX，使用 By 作为查询条件。
    default List<SeckillTimeDO> selectListWithTime(LocalTime startTime, LocalTime endTime){
        // TODO @halfninety：mapper 层，不做空判断，业务上面自己的保证
        if (startTime == null && endTime == null) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<SeckillTimeDO>()
                .ge(SeckillTimeDO::getStartTime,startTime)
                .le(SeckillTimeDO::getEndTime,endTime));
    }

    // TODO @halfninety：updateActivityCount + 和 -，可以执使用一个方法实现哈。多传递一个参数
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
