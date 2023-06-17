package cn.iocoder.yudao.module.promotion.dal.mysql.seckill.seckillconfig;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.config.SeckillConfigPageReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckillconfig.SeckillConfigDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

@Mapper
public interface SeckillConfigMapper extends BaseMapperX<SeckillConfigDO> {

    default List<SeckillConfigDO> selectListByTime(LocalTime time) {
        return selectList(SeckillConfigDO::getStartTime, SeckillConfigDO::getEndTime, time);
    }

    default List<SeckillConfigDO> selectListByTime(LocalTime startTime, LocalTime endTime) {
        return selectList(new LambdaQueryWrapper<SeckillConfigDO>()
                .ge(SeckillConfigDO::getStartTime, startTime)
                .le(SeckillConfigDO::getEndTime, endTime));
    }

    default void updateActivityCount(Collection<Long> ids, String type, Integer count) {
        new LambdaUpdateChainWrapper<>(this)
                .in(SeckillConfigDO::getId, ids)
                .setSql("`seckill_activity_count` = `seckill_activity_count` " + type + count)
                .update();
    }

    default PageResult<SeckillConfigDO> selectPage(SeckillConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SeckillConfigDO>()
                .likeIfPresent(SeckillConfigDO::getName, reqVO.getName())
                .betweenIfPresent(SeckillConfigDO::getStartTime, reqVO.getStartTime())
                .betweenIfPresent(SeckillConfigDO::getEndTime, reqVO.getEndTime())
                .eqIfPresent(SeckillConfigDO::getStatus, reqVO.getStatus())
                .orderByDesc(SeckillConfigDO::getId));
    }

}
