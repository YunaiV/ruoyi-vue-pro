package cn.iocoder.yudao.module.promotion.dal.mysql.seckill.seckillactivity;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.seckillactivity.vo.SeckillActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckillactivity.SeckillActivityDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀活动 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface SeckillActivityMapper extends BaseMapperX<SeckillActivityDO> {
// TODO: 2022/11/28 halfninety 秒杀活动通过场次查询使用like会出现问题，查询活动场次编号为1，则活动场次编号为 1，11，......的都会被查出来
    default PageResult<SeckillActivityDO> selectPage(SeckillActivityPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SeckillActivityDO>()
                .likeIfPresent(SeckillActivityDO::getName, reqVO.getName())
                .eqIfPresent(SeckillActivityDO::getStatus, reqVO.getStatus())
                .likeIfPresent(SeckillActivityDO::getTimeId, reqVO.getTimeId())
//                .like(StringUtils.hasText(reqVO.getTimeId()),SeckillActivityDO::getTimeId, reqVO.getTimeId() + ",")
                .betweenIfPresent(SeckillActivityDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SeckillActivityDO::getId));
    }

}
