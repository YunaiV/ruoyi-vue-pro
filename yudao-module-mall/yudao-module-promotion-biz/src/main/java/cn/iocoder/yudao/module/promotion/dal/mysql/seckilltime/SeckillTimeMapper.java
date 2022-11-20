package cn.iocoder.yudao.module.promotion.dal.mysql.seckilltime;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckilltime.SeckillTimeDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.promotion.controller.admin.seckilltime.vo.*;

/**
 * 秒杀时段 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface SeckillTimeMapper extends BaseMapperX<SeckillTimeDO> {

//    default PageResult<SeckillTimeDO> selectPage(SeckillTimePageReqVO reqVO) {
//        return selectPage(reqVO, new LambdaQueryWrapperX<SeckillTimeDO>()
//                .likeIfPresent(SeckillTimeDO::getName, reqVO.getName())
//                .geIfPresent(SeckillTimeDO::getStartTime, reqVO.getStartTime())
//                .leIfPresent(SeckillTimeDO::getEndTime, reqVO.getEndTime())
////                .betweenIfPresent(SeckillTimeDO::getStartTime, reqVO.getStartTime())
////                .betweenIfPresent(SeckillTimeDO::getEndTime, reqVO.getEndTime())
////                .betweenIfPresent(SeckillTimeDO::getCreateTime, reqVO.getCreateTime())
//                .orderByDesc(SeckillTimeDO::getId));
//    }

    default List<SeckillTimeDO> selectList(SeckillTimeExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<SeckillTimeDO>()
                .likeIfPresent(SeckillTimeDO::getName, reqVO.getName())
                .betweenIfPresent(SeckillTimeDO::getStartTime, reqVO.getStartTime())
                .betweenIfPresent(SeckillTimeDO::getEndTime, reqVO.getEndTime())
                .betweenIfPresent(SeckillTimeDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SeckillTimeDO::getId));
    }

}
