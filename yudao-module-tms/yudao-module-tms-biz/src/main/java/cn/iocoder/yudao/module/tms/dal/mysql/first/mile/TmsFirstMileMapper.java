package cn.iocoder.yudao.module.tms.dal.mysql.first.mile;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.TmsFirstMileDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 头程申请单 Mapper 接口
 *
 * @author 芋道源码
 */
@Mapper
public interface TmsFirstMileMapper extends BaseMapperX<TmsFirstMileDO> {
//
//    //build wrapper
//    default MPJLambdaWrapperX<TmsFirstMileDO> buildWrapper(TmsFirstMilePageReqVO reqVO) {
//        return new MPJLambdaWrapperX<TmsFirstMileDO>().betweenIfPresent(TmsFirstMileDO::getCreateTime, reqVO.getCreateTime()) // 创建时间范围
//            .likeIfPresent(TmsFirstMileDO::getCode, reqVO.getCode()) // 单据编号
//            .betweenIfPresent(TmsFirstMileDO::getBillTime, reqVO.getBillTime()) // 单据日期范围
//            .eqIfPresent(TmsFirstMileDO::getCarrierId, reqVO.getCarrierId()) // 承运商ID
//            .betweenIfPresent(TmsFirstMileDO::getSettlementDate, reqVO.getSettlementDate()) // 结算日期范围
//            .eqIfPresent(TmsFirstMileDO::getBalance, reqVO.getBalance()) // 结算状态
//            .eqIfPresent(TmsFirstMileDO::getAuditorId, reqVO.getAuditorId()) // 审核人ID
//            .betweenIfPresent(TmsFirstMileDO::getAuditTime, reqVO.getAuditTime()) // 审核时间范围
//            .eqIfPresent(TmsFirstMileDO::getAuditStatus, reqVO.getAuditStatus()) // 审核状态
//            .eqIfPresent(TmsFirstMileDO::getToWarehouseId, reqVO.getToWarehouseId()) // 目的仓库ID
//            .likeIfPresent(TmsFirstMileDO::getLadingNo, reqVO.getLadingNo()) // 提单号
//            .eqIfPresent(TmsFirstMileDO::getCabinetType, reqVO.getCabinetType()) // 柜型
//            .betweenIfPresent(TmsFirstMileDO::getPackTime, reqVO.getPackTime()) // 装箱时间范围
//            .betweenIfPresent(TmsFirstMileDO::getArrivePlanTime, reqVO.getArrivePlanTime()) // 预计到港时间范围
//            .betweenIfPresent(TmsFirstMileDO::getDeliveryEstimateTime, reqVO.getDeliveryEstimateTime()) // 预计提货时间范围
//            .betweenIfPresent(TmsFirstMileDO::getDeliveryActualTime, reqVO.getDeliveryActualTime()) // 实际提货时间范围
//            .betweenIfPresent(TmsFirstMileDO::getTotalVolume, reqVO.getTotalVolume()) // 总体积范围
//            .betweenIfPresent(TmsFirstMileDO::getTotalWeight, reqVO.getTotalWeight()) // 总重量范围
//            .betweenIfPresent(TmsFirstMileDO::getNetWeight, reqVO.getNetWeight()) // 净重范围
//            .betweenIfPresent(TmsFirstMileDO::getTotalValue, reqVO.getTotalValue()) // 总价值范围
//            .betweenIfPresent(TmsFirstMileDO::getTotalQty, reqVO.getTotalQty()) // 总数量范围
//            .likeIfPresent(TmsFirstMileDO::getRemark, reqVO.getRemark()) // 备注
//            .eqIfPresent(TmsFirstMileDO::getOutboundStatus, reqVO.getOutboundStatus()) // 出库状态
//            .betweenIfPresent(TmsFirstMileDO::getOutboundTime, reqVO.getOutboundTime()) // 出库时间范围
//            .eqIfPresent(TmsFirstMileDO::getInboundStatus, reqVO.getInboundStatus()) // 入库状态
//            .betweenIfPresent(TmsFirstMileDO::getInboundTime, reqVO.getInboundTime()) // 入库时间范围
//            .orderByDesc(TmsFirstMileDO::getId);
//    }
//
//    /**
//     * 分页查询头程申请单
//     *
//     * @param reqVO 查询条件
//     * @return 头程申请单分页结果
//     */
//    default PageResult<TmsFirstMileDO> selectPage(TmsFirstMilePageReqVO reqVO) {
//        return selectPage(reqVO, buildWrapper(reqVO));
//    }

    default boolean selectByCode(String code) {
        return selectCount(TmsFirstMileDO::getCode, code) > 0;
    }

    default TmsFirstMileDO selectByCodeRaw(String code) {
        return selectOne(TmsFirstMileDO::getCode, code);
    }

}