package cn.iocoder.yudao.module.promotion.dal.mysql.point;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.point.vo.activity.PointActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.point.PointActivityDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分商城活动 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface PointActivityMapper extends BaseMapperX<PointActivityDO> {

    default PageResult<PointActivityDO> selectPage(PointActivityPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PointActivityDO>()
                .eqIfPresent(PointActivityDO::getSpuId, reqVO.getSpuId())
                .eqIfPresent(PointActivityDO::getStatus, reqVO.getStatus())
                .eqIfPresent(PointActivityDO::getRemark, reqVO.getRemark())
                .eqIfPresent(PointActivityDO::getSort, reqVO.getSort())
                .betweenIfPresent(PointActivityDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PointActivityDO::getId));
    }

}