package cn.iocoder.yudao.module.promotion.dal.mysql.combination;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.CombinationActivityExportReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.CombinationActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationActivityDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 拼团活动 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface CombinationActivityMapper extends BaseMapperX<CombinationActivityDO> {

    default PageResult<CombinationActivityDO> selectPage(CombinationActivityPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CombinationActivityDO>()
                .likeIfPresent(CombinationActivityDO::getName, reqVO.getName())
                .eqIfPresent(CombinationActivityDO::getSpuId, reqVO.getSpuId())
                .eqIfPresent(CombinationActivityDO::getTotalLimitCount, reqVO.getTotalLimitCount())
                .eqIfPresent(CombinationActivityDO::getSingleLimitCount, reqVO.getSingleLimitCount())
                .betweenIfPresent(CombinationActivityDO::getStartTime, reqVO.getStartTime())
                .betweenIfPresent(CombinationActivityDO::getEndTime, reqVO.getEndTime())
                .eqIfPresent(CombinationActivityDO::getUserSize, reqVO.getUserSize())
                .eqIfPresent(CombinationActivityDO::getTotalNum, reqVO.getTotalNum())
                .eqIfPresent(CombinationActivityDO::getSuccessNum, reqVO.getSuccessNum())
                .eqIfPresent(CombinationActivityDO::getOrderUserCount, reqVO.getOrderUserCount())
                .eqIfPresent(CombinationActivityDO::getVirtualGroup, reqVO.getVirtualGroup())
                .eqIfPresent(CombinationActivityDO::getStatus, reqVO.getStatus())
                .eqIfPresent(CombinationActivityDO::getLimitDuration, reqVO.getLimitDuration())
                .betweenIfPresent(CombinationActivityDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CombinationActivityDO::getId));
    }

    default List<CombinationActivityDO> selectList(CombinationActivityExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<CombinationActivityDO>()
                .likeIfPresent(CombinationActivityDO::getName, reqVO.getName())
                .eqIfPresent(CombinationActivityDO::getSpuId, reqVO.getSpuId())
                .eqIfPresent(CombinationActivityDO::getTotalLimitCount, reqVO.getTotalLimitCount())
                .eqIfPresent(CombinationActivityDO::getSingleLimitCount, reqVO.getSingleLimitCount())
                .betweenIfPresent(CombinationActivityDO::getStartTime, reqVO.getStartTime())
                .betweenIfPresent(CombinationActivityDO::getEndTime, reqVO.getEndTime())
                .eqIfPresent(CombinationActivityDO::getUserSize, reqVO.getUserSize())
                .eqIfPresent(CombinationActivityDO::getTotalNum, reqVO.getTotalNum())
                .eqIfPresent(CombinationActivityDO::getSuccessNum, reqVO.getSuccessNum())
                .eqIfPresent(CombinationActivityDO::getOrderUserCount, reqVO.getOrderUserCount())
                .eqIfPresent(CombinationActivityDO::getVirtualGroup, reqVO.getVirtualGroup())
                .eqIfPresent(CombinationActivityDO::getStatus, reqVO.getStatus())
                .eqIfPresent(CombinationActivityDO::getLimitDuration, reqVO.getLimitDuration())
                .betweenIfPresent(CombinationActivityDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CombinationActivityDO::getId));
    }

}
