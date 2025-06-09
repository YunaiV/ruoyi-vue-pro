package cn.iocoder.yudao.module.tms.dal.mysql.vessel.tracking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.tms.controller.admin.vessel.tracking.vo.TmsVesselTrackingPageReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.vessel.tracking.TmsVesselTrackingDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 出运跟踪信息表（由外部API更新，船） Mapper
 *
 * @author wdy
 */
@Mapper
public interface TmsVesselTrackingMapper extends BaseMapperX<TmsVesselTrackingDO> {
    //build wrapper
    default MPJLambdaWrapperX<TmsVesselTrackingDO> buildWrapper(TmsVesselTrackingPageReqVO vo) {
        if (vo == null) {
            vo = new TmsVesselTrackingPageReqVO();
        }
        return new MPJLambdaWrapperX<TmsVesselTrackingDO>()
            // 基础信息
            .eqIfPresent(TmsVesselTrackingDO::getId, vo.getId()) // 主键ID
            .eqIfPresent(TmsVesselTrackingDO::getUpstreamType, vo.getUpstreamType()) // 上游类型
            .eqIfPresent(TmsVesselTrackingDO::getUpstreamId, vo.getUpstreamId()) // 上游ID
            // 时间信息
            .betweenIfPresent(TmsVesselTrackingDO::getArriveEstimateTime, vo.getArriveEstimateTime()) // 预计到达时间
            .betweenIfPresent(TmsVesselTrackingDO::getDepartEstimateTime, vo.getDepartEstimateTime()) // 预计离开时间
            .betweenIfPresent(TmsVesselTrackingDO::getArriveActualTime, vo.getArriveActualTime()) // 实际到达时间
            .betweenIfPresent(TmsVesselTrackingDO::getDepartActualTime, vo.getDepartActualTime()) // 实际离开时间
            .betweenIfPresent(TmsVesselTrackingDO::getPickupTime, vo.getPickupTime()) // 提货时间
            .betweenIfPresent(TmsVesselTrackingDO::getReturnTime, vo.getReturnTime()) // 还柜时间
            // 同步信息
            .eqIfPresent(TmsVesselTrackingDO::getApiSource, vo.getApiSource()) // API来源
            .betweenIfPresent(TmsVesselTrackingDO::getLastSyncTime, vo.getLastSyncTime()) // 最后同步时间
            .eqIfPresent(TmsVesselTrackingDO::getRevision, vo.getRevision()) // 乐观锁版本号
            .betweenIfPresent(TmsVesselTrackingDO::getCreateTime, vo.getCreateTime()) // 创建时间
            // 港口信息
            .eqIfPresent(TmsVesselTrackingDO::getTransitPort, vo.getTransitPort()) // 中转港
            .eqIfPresent(TmsVesselTrackingDO::getToPort, vo.getToPort()) // 目的港
            .eqIfPresent(TmsVesselTrackingDO::getFromPort, vo.getFromPort()) // 起运港
            // 承运信息
            .eqIfPresent(TmsVesselTrackingDO::getCarrierCompanyId, vo.getCarrierCompanyId()) // 承运公司ID
            .eqIfPresent(TmsVesselTrackingDO::getVessel, vo.getVessel()) // 船名
            .eqIfPresent(TmsVesselTrackingDO::getVoyage, vo.getVoyage()) // 航次
            // 货代信息
            .eqIfPresent(TmsVesselTrackingDO::getForwarderCompanyId, vo.getForwarderCompanyId()) // 货代公司ID
            .eqIfPresent(TmsVesselTrackingDO::getContainerNo, vo.getContainerNo()) // 集装箱号
            // 排序
            .orderByDesc(TmsVesselTrackingDO::getCreateTime);
    }

    default PageResult<TmsVesselTrackingDO> selectPage(TmsVesselTrackingPageReqVO vo) {
        return selectPage(vo, buildWrapper(vo));
    }

    // 根据上游ID+上游单据类型获得 出运跟踪信息
    default TmsVesselTrackingDO getVesselTrackingByUpstreamIdAndUpstreamType(Long upstreamId, Integer upstreamType) {
        return selectOne(new MPJLambdaWrapperX<TmsVesselTrackingDO>()
            .eq(TmsVesselTrackingDO::getUpstreamId, upstreamId)
            .eq(TmsVesselTrackingDO::getUpstreamType, upstreamType)
        );
    }

    default void deleteByUpstreamIdAndUpstreamType(Long upstreamId, Integer billType) {
        delete(new MPJLambdaWrapperX<TmsVesselTrackingDO>()
            .eq(TmsVesselTrackingDO::getUpstreamId, upstreamId)
            .eq(TmsVesselTrackingDO::getUpstreamType, billType)
        );
    }
}