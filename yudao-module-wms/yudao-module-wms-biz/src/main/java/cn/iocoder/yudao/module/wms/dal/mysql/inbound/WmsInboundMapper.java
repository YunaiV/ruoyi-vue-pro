package cn.iocoder.yudao.module.wms.dal.mysql.inbound;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.*;

/**
 * 入库单 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsInboundMapper extends BaseMapperX<WmsInboundDO> {

    default PageResult<WmsInboundDO> selectPage(WmsInboundPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsInboundDO>()
                .eqIfPresent(WmsInboundDO::getNo, reqVO.getNo())
                .eqIfPresent(WmsInboundDO::getType, reqVO.getType())
                .eqIfPresent(WmsInboundDO::getWarehouseId, reqVO.getWarehouseId())
                .eqIfPresent(WmsInboundDO::getStatus, reqVO.getStatus())
                .eqIfPresent(WmsInboundDO::getSourceBillId, reqVO.getSourceBillId())
                .eqIfPresent(WmsInboundDO::getSourceBillNo, reqVO.getSourceBillNo())
                .eqIfPresent(WmsInboundDO::getSourceBillType, reqVO.getSourceBillType())
                .eqIfPresent(WmsInboundDO::getReferNo, reqVO.getReferNo())
                .eqIfPresent(WmsInboundDO::getTraceNo, reqVO.getTraceNo())
                .eqIfPresent(WmsInboundDO::getShippingMethod, reqVO.getShippingMethod())
                .betweenIfPresent(WmsInboundDO::getPlanArrivalTime, reqVO.getPlanArrivalTime())
                .betweenIfPresent(WmsInboundDO::getActualArrivalTime, reqVO.getActualArrivalTime())
                .eqIfPresent(WmsInboundDO::getCreatorComment, reqVO.getCreatorComment())
                .eqIfPresent(WmsInboundDO::getInitAge, reqVO.getInitAge())
                .betweenIfPresent(WmsInboundDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WmsInboundDO::getId));
    }

}