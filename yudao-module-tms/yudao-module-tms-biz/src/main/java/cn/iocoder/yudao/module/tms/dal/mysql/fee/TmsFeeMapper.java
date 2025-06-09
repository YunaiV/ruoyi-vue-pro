package cn.iocoder.yudao.module.tms.dal.mysql.fee;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.tms.controller.admin.fee.vo.TmsFeePageReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.fee.TmsFeeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 出运订单费用明细 Mapper
 *
 * @author wdy
 */
@Mapper
public interface TmsFeeMapper extends BaseMapperX<TmsFeeDO> {
    //build wrapper MPJLambdaWrapperX
    default MPJLambdaWrapperX<TmsFeeDO> buildWrapper(TmsFeePageReqVO reqVO) {
        return new MPJLambdaWrapperX<TmsFeeDO>()
            .eqIfPresent(TmsFeeDO::getUpstreamType, reqVO.getUpstreamType())
            .eqIfPresent(TmsFeeDO::getUpstreamId, reqVO.getUpstreamId())
            .eqIfPresent(TmsFeeDO::getCostType, reqVO.getCostType())
            .betweenIfPresent(TmsFeeDO::getAmount, reqVO.getAmount())
            .betweenIfPresent(TmsFeeDO::getCurrencyType, reqVO.getCurrencyType())
            .likeIfPresent(TmsFeeDO::getRemark, reqVO.getRemark())
            .betweenIfPresent(TmsFeeDO::getCreateTime, reqVO.getCreateTime())
            .orderByDesc(TmsFeeDO::getId);
    }


    default PageResult<TmsFeeDO> selectPage(TmsFeePageReqVO reqVO) {
        return selectPage(reqVO, buildWrapper(reqVO));
    }

    default List<TmsFeeDO> selectListBySourceId(Long sourceId) {
        return selectList(TmsFeeDO::getUpstreamId, sourceId);
    }

    default int deleteBySourceId(Long sourceId) {
        return delete(TmsFeeDO::getUpstreamId, sourceId);
    }

    default List<TmsFeeDO> selectListBySourceIdAndType(Long sourceId, Integer sourceType) {
        return selectList(new MPJLambdaWrapperX<TmsFeeDO>()
            .eq(TmsFeeDO::getUpstreamId, sourceId)
            .eq(TmsFeeDO::getUpstreamType, sourceType));
    }

    default List<Long> selectFirstMileIdsByFeePageReqVO(TmsFeePageReqVO reqVO) {
        return selectList(new MPJLambdaWrapperX<TmsFeeDO>()
            .eqIfPresent(TmsFeeDO::getUpstreamType, reqVO.getUpstreamType())
            .eqIfPresent(TmsFeeDO::getCostType, reqVO.getCostType())
            .betweenIfPresent(TmsFeeDO::getAmount, reqVO.getAmount())
            .betweenIfPresent(TmsFeeDO::getCurrencyType, reqVO.getCurrencyType())
            .likeIfPresent(TmsFeeDO::getRemark, reqVO.getRemark())
            .betweenIfPresent(TmsFeeDO::getCreateTime, reqVO.getCreateTime()))
            .stream()
            .map(TmsFeeDO::getUpstreamId)
            .distinct()
            .collect(Collectors.toList());
    }

    default List<TmsFeeDO> selectBySourceIdAndSourceType(Long id, Integer sourceType) {
        return selectList(new MPJLambdaWrapperX<TmsFeeDO>()
            .eq(TmsFeeDO::getUpstreamId, id)
            .eq(TmsFeeDO::getUpstreamType, sourceType));
    }

    default int deleteByIdAndType(Long id, Integer sourceType) {
        return delete(new MPJLambdaWrapperX<TmsFeeDO>()
            .eq(TmsFeeDO::getId, id)
            .eq(TmsFeeDO::getUpstreamType, sourceType));
    }

    default void deleteBatchIdsBySourceType(List<Long> ids, Integer sourceType) {
        delete(new MPJLambdaWrapperX<TmsFeeDO>()
            .in(TmsFeeDO::getId, ids)
            .eq(TmsFeeDO::getUpstreamType, sourceType));
    }
    default int deleteBySourceIdAndType(Long sourceId, Integer sourceType) {
        return delete(new MPJLambdaWrapperX<TmsFeeDO>()
            .eq(TmsFeeDO::getUpstreamId, sourceId)
            .eq(TmsFeeDO::getUpstreamType, sourceType));
    }

    default int deleteBySourceIdAndSourceType(Long sourceId, Integer sourceType) {
        return delete(new MPJLambdaWrapperX<TmsFeeDO>()
            .eq(TmsFeeDO::getUpstreamId, sourceId)
            .eq(TmsFeeDO::getUpstreamType, sourceType));
    }

}