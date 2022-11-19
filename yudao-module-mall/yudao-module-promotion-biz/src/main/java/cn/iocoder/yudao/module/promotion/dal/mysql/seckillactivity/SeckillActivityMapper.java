package cn.iocoder.yudao.module.promotion.dal.mysql.seckillactivity;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckillactivity.SeckillActivityDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.promotion.controller.admin.seckillactivity.vo.*;

/**
 * 秒杀活动 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface SeckillActivityMapper extends BaseMapperX<SeckillActivityDO> {

    default PageResult<SeckillActivityDO> selectPage(SeckillActivityPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SeckillActivityDO>()
                .likeIfPresent(SeckillActivityDO::getName, reqVO.getName())
                .eqIfPresent(SeckillActivityDO::getStatus, reqVO.getStatus())
                .eqIfPresent(SeckillActivityDO::getTimeId, reqVO.getTimeId())
                .betweenIfPresent(SeckillActivityDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SeckillActivityDO::getId));
    }

    default List<SeckillActivityDO> selectList(SeckillActivityExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<SeckillActivityDO>()
                .likeIfPresent(SeckillActivityDO::getName, reqVO.getName())
                .eqIfPresent(SeckillActivityDO::getStatus, reqVO.getStatus())
                .eqIfPresent(SeckillActivityDO::getTimeId, reqVO.getTimeId())
                .betweenIfPresent(SeckillActivityDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SeckillActivityDO::getId));
    }

}
