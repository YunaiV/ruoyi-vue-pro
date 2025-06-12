package cn.iocoder.yudao.module.tms.dal.mysql.first.mile.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.system.enums.somle.BillType;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.item.vo.TmsFirstMileItemPageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.req.TmsFirstMilePageReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.TmsFirstMileDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.item.TmsFirstMileItemDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.vessel.tracking.TmsVesselTrackingDO;
import cn.iocoder.yudao.module.tms.service.bo.TmsFirstMileItemBO;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 头程单明细 Mapper
 *
 * @author wdy
 */
@Mapper
public interface TmsFirstMileItemMapper extends BaseMapperX<TmsFirstMileItemDO> {

    //MPJLambdaWrapperX build
    default MPJLambdaWrapperX<TmsFirstMileItemDO> buildWrapper(TmsFirstMileItemPageReqVO vo) {
        if (vo == null) {
            vo = new TmsFirstMileItemPageReqVO();
        }
        return new MPJLambdaWrapperX<TmsFirstMileItemDO>()
            .selectAll(TmsFirstMileItemDO.class)
            .inIfPresent(TmsFirstMileItemDO::getId, vo.getId())
            .eqIfPresent(TmsFirstMileItemDO::getCreator, vo.getCreator())
            .betweenIfPresent(TmsFirstMileItemDO::getCreateTime, vo.getCreateTime())
            .eqIfPresent(TmsFirstMileItemDO::getUpdater, vo.getUpdater())
            .betweenIfPresent(TmsFirstMileItemDO::getUpdateTime, vo.getUpdateTime())
            .eqIfPresent(TmsFirstMileItemDO::getRequestItemId, vo.getRequestItemId())
            .eqIfPresent(TmsFirstMileItemDO::getProductId, vo.getProductId())
            .eqIfPresent(TmsFirstMileItemDO::getQty, vo.getQty())
            .betweenIfPresent(TmsFirstMileItemDO::getBoxQty, vo.getBoxQty())
            .eqIfPresent(TmsFirstMileItemDO::getCompanyId, vo.getCompanyId())
            .eqIfPresent(TmsFirstMileItemDO::getDeptId, vo.getDeptId())
            .likeIfPresent(TmsFirstMileItemDO::getRemark, vo.getRemark())
            .betweenIfPresent(TmsFirstMileItemDO::getOutboundClosedQty, vo.getOutboundClosedQty())
            .betweenIfPresent(TmsFirstMileItemDO::getOutboundPlanQty, vo.getOutboundPlanQty())
            .betweenIfPresent(TmsFirstMileItemDO::getInboundClosedQty, vo.getInboundClosedQty())
            .eqIfPresent(TmsFirstMileItemDO::getFromWarehouseId, vo.getFromWarehouseId())
            .betweenIfPresent(TmsFirstMileItemDO::getPackageLength, vo.getPackageLength())
            .betweenIfPresent(TmsFirstMileItemDO::getPackageWidth, vo.getPackageWidth())
            .betweenIfPresent(TmsFirstMileItemDO::getPackageHeight, vo.getPackageHeight())
            .betweenIfPresent(TmsFirstMileItemDO::getPackageWeight, vo.getPackageWeight())
            .eqIfPresent(TmsFirstMileItemDO::getSalesCompanyId, vo.getSalesCompanyId())
            .orderByDesc(TmsFirstMileItemDO::getCreateTime);
    }

    //build BO wrapperX
    default MPJLambdaWrapper<TmsFirstMileItemDO> buildBOWrapper(TmsFirstMilePageReqVO vo) {
        if (vo == null) {
            vo = new TmsFirstMilePageReqVO();
        }
        if (vo.getMainQueryVO() == null) {
            vo.setMainQueryVO(new TmsFirstMilePageReqVO.mainQueryVO());
        }
        if (vo.getTrackingQueryVO() == null) {
            vo.setTrackingQueryVO(new TmsFirstMilePageReqVO.TmsVesselTrackingFirstMileQueryVO());
        }
        MPJLambdaWrapper<TmsFirstMileItemDO> lambdaWrapper = buildWrapper(vo.getItemPageReqVO())
            .leftJoin(TmsFirstMileDO.class, TmsFirstMileDO::getId, TmsFirstMileItemDO::getFirstMileId)
            .selectAll(TmsFirstMileDO.class)
            .eqIfPresent(TmsFirstMileDO::getId, vo.getMainQueryVO().getId()) // 头程单IDs
            .betweenIfPresent(TmsFirstMileDO::getCreateTime, vo.getMainQueryVO().getCreateTime()) // 创建时间范围
            .likeIfPresent(TmsFirstMileDO::getCode, vo.getMainQueryVO().getCode()) // 单据编号
            .betweenIfPresent(TmsFirstMileDO::getBillTime, vo.getMainQueryVO().getBillTime()) // 单据日期范围
            .eqIfPresent(TmsFirstMileDO::getCarrierId, vo.getMainQueryVO().getCarrierId()) // 承运商ID
            .betweenIfPresent(TmsFirstMileDO::getSettlementDate, vo.getMainQueryVO().getSettlementDate()) // 结算日期范围
            .eqIfPresent(TmsFirstMileDO::getBalance, vo.getMainQueryVO().getBalance()) // 结算状态
            .eqIfPresent(TmsFirstMileDO::getAuditorId, vo.getMainQueryVO().getAuditorId()) // 审核人ID
            .betweenIfPresent(TmsFirstMileDO::getAuditTime, vo.getMainQueryVO().getAuditTime()) // 审核时间范围
            .eqIfPresent(TmsFirstMileDO::getAuditStatus, vo.getMainQueryVO().getAuditStatus()) // 审核状态
            .eqIfPresent(TmsFirstMileDO::getToWarehouseId, vo.getMainQueryVO().getToWarehouseId()) // 目的仓库ID
            .eqIfPresent(TmsFirstMileDO::getCabinetType, vo.getMainQueryVO().getCabinetType()) // 柜型
            .betweenIfPresent(TmsFirstMileDO::getPackTime, vo.getMainQueryVO().getPackTime()) // 装箱时间范围
            .betweenIfPresent(TmsFirstMileDO::getArrivePlanTime, vo.getMainQueryVO().getArrivePlanTime()) // 预计到港时间范围
            .likeIfPresent(TmsFirstMileDO::getRemark, vo.getMainQueryVO().getRemark()) // 备注
            .eqIfPresent(TmsFirstMileDO::getOutboundStatus, vo.getMainQueryVO().getOutboundStatus()) // 出库状态
            .betweenIfPresent(TmsFirstMileDO::getOutboundTime, vo.getMainQueryVO().getOutboundTime()) // 出库时间范围
            .eqIfPresent(TmsFirstMileDO::getInboundStatus, vo.getMainQueryVO().getInboundStatus()) // 入库状态
            .betweenIfPresent(TmsFirstMileDO::getInboundTime, vo.getMainQueryVO().getInboundTime()) // 入库时间范围
            .orderByDesc(TmsFirstMileDO::getCreateTime)
            //TODO 关联船运
            .leftJoin(TmsVesselTrackingDO.class, on ->
                on.eq(TmsVesselTrackingDO::getUpstreamType, BillType.TMS_FIRST_MILE.getValue()).eq(TmsVesselTrackingDO::getUpstreamId, TmsFirstMileDO::getId)
            )
            .selectAll(TmsVesselTrackingDO.class)
            // 时间
//            .betweenIfPresent(TmsVesselTrackingDO::getArriveEstimateTime, vo.getTrackingQueryVO().getArriveEstimateTime()) // 预计到达时间
//            .betweenIfPresent(TmsVesselTrackingDO::getDepartEstimateTime, vo.getTrackingQueryVO().getDepartEstimateTime()) // 预计离开时间
//            .betweenIfPresent(TmsVesselTrackingDO::getArriveActualTime, vo.getTrackingQueryVO().getArriveActualTime()) // 实际到达时间
//            .betweenIfPresent(TmsVesselTrackingDO::getDepartActualTime, vo.getTrackingQueryVO().getDepartActualTime()) // 实际离开时间
//            .betweenIfPresent(TmsVesselTrackingDO::getPickupTime, vo.getTrackingQueryVO().getPickupTime()) // 提货时间
//            .betweenIfPresent(TmsVesselTrackingDO::getReturnTime, vo.getTrackingQueryVO().getReturnTime()) // 还柜时间
            // 同步
            .eqIfExists(TmsVesselTrackingDO::getApiSource, vo.getTrackingQueryVO().getApiSource()) // API来源
//            .betweenIfPresent(TmsVesselTrackingDO::getLastSyncTime, vo.getTrackingQueryVO().getLastSyncTime()) // 最后同步时间
            // 港口
            .eqIfExists(TmsVesselTrackingDO::getTransitPort, vo.getTrackingQueryVO().getTransitPort()) // 中转港
            .eqIfExists(TmsVesselTrackingDO::getToPort, vo.getTrackingQueryVO().getToPort()) // 目的港
            .eqIfExists(TmsVesselTrackingDO::getFromPort, vo.getTrackingQueryVO().getFromPort()) // 起运港
            // 承运
            .eqIfExists(TmsVesselTrackingDO::getCarrierCompanyId, vo.getTrackingQueryVO().getCarrierCompanyId()) // 承运公司ID
            .eqIfExists(TmsVesselTrackingDO::getVessel, vo.getTrackingQueryVO().getVessel()) // 船名
            .eqIfExists(TmsVesselTrackingDO::getVoyage, vo.getTrackingQueryVO().getVoyage()) // 航次
            // 货代
            .eqIfExists(TmsVesselTrackingDO::getForwarderCompanyId, vo.getTrackingQueryVO().getForwarderCompanyId()) // 货代公司ID
            .eqIfExists(TmsVesselTrackingDO::getContainerNo, vo.getTrackingQueryVO().getContainerNo()) // 集装箱号
            //deliveryEstimateTime
            .eqIfExists(TmsVesselTrackingDO::getDeliveryEstimateTime, vo.getTrackingQueryVO().getDeliveryEstimateTime())
            // 排序
            .orderByDesc(TmsVesselTrackingDO::getCreateTime);

        if (vo.getTrackingQueryVO().getArriveEstimateTime() != null && vo.getTrackingQueryVO().getArriveEstimateTime().length == 2) {
            //    if (val1 != null && val2 != null) {
            //            return (MPJLambdaWrapperX<T>) super.between(column, val1, val2);
            //        }
            //        if (val1 != null) {
            //            return (MPJLambdaWrapperX<T>) super.ge(column, val1);
            //        }
            //        if (val2 != null) {
            //            return (MPJLambdaWrapperX<T>) super.le(column, val2);
            //        }
            lambdaWrapper.between(TmsVesselTrackingDO::getArriveEstimateTime, vo.getTrackingQueryVO().getArriveEstimateTime()[0], vo.getTrackingQueryVO().getArriveEstimateTime()[1]);
        }

        return lambdaWrapper;
    }

    default PageResult<TmsFirstMileItemBO> selectPageBO(TmsFirstMilePageReqVO vo) {
        if (vo == null) {
            vo = new TmsFirstMilePageReqVO();
        }
        return selectJoinPage(vo, TmsFirstMileItemBO.class, buildBOWrapper(vo)
            .selectAssociation(TmsFirstMileDO.class, TmsFirstMileItemBO::getTmsFirstMileDO)
            .selectAssociation(TmsVesselTrackingDO.class, TmsFirstMileItemBO::getTmsVesselTrackingDO)
        );
    }

    //获得itemBO,N个
    default List<TmsFirstMileItemBO> selectBOById(Long id) {
        return selectJoinList(TmsFirstMileItemBO.class, buildBOWrapper(new TmsFirstMilePageReqVO().setMainQueryVO(new TmsFirstMilePageReqVO.mainQueryVO().setId(id))));
    }

    default List<TmsFirstMileItemDO> selectListByFirstMileId(Long firstMileId) {
        return selectList(TmsFirstMileItemDO::getFirstMileId, firstMileId);
    }

    default int deleteByFirstMileId(Long firstMileId) {
        return delete(TmsFirstMileItemDO::getFirstMileId, firstMileId);
    }

    default List<TmsFirstMileItemDO> selectListByRequestItemId(Long id) {
        return selectList(TmsFirstMileItemDO::getRequestItemId, id);
    }
}