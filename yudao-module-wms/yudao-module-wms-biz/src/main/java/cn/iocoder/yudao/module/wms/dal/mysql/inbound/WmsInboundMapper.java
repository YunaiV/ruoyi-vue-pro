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
				.eqIfPresent(WmsInboundDO::getAuditStatus, reqVO.getAuditStatus())
				.eqIfPresent(WmsInboundDO::getInboundStatus, reqVO.getInboundStatus())
				.eqIfPresent(WmsInboundDO::getSourceBillId, reqVO.getSourceBillId())
				.eqIfPresent(WmsInboundDO::getSourceBillNo, reqVO.getSourceBillNo())
				.eqIfPresent(WmsInboundDO::getSourceBillType, reqVO.getSourceBillType())
				.eqIfPresent(WmsInboundDO::getTraceNo, reqVO.getTraceNo()).// .betweenIfPresent(WmsInboundDO::getArrivalPlanTime, reqVO.getArrivalPlanTime())
        eqIfPresent(// .betweenIfPresent(WmsInboundDO::getArrivalPlanTime, reqVO.getArrivalPlanTime())
        WmsInboundDO::getShippingMethod, // .betweenIfPresent(WmsInboundDO::getArrivalActualTime, reqVO.getArrivalActualTime())
        reqVO.getShippingMethod())
				.eqIfPresent(WmsInboundDO::getCreatorComment, reqVO.getCreatorComment())
				.eqIfPresent(WmsInboundDO::getInitAge, reqVO.getInitAge())
				.betweenIfPresent(WmsInboundDO::getCreateTime, reqVO.getCreateTime())
				.orderByDesc(WmsInboundDO::getId));
    }

    /**
     * 按 no 查询唯一的 WmsInboundDO
     */
    default WmsInboundDO getByNo(String no) {
        LambdaQueryWrapperX<WmsInboundDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsInboundDO::getNo, no);
        return selectOne(wrapper);
    }

    /**
     * 按 warehouse_id 查询 WmsInboundDO 清单
     */
    default List<WmsInboundDO> selectByWarehouseId(Long warehouseId) {
        return selectList(new LambdaQueryWrapperX<WmsInboundDO>().eq(WmsInboundDO::getWarehouseId, warehouseId));
    }

    /**
     * 按 type 查询 WmsInboundDO 清单
     */
    default List<WmsInboundDO> selectByType(Integer type) {
        return selectList(new LambdaQueryWrapperX<WmsInboundDO>().eq(WmsInboundDO::getType, type));
    }

    /**
     * 按 warehouseId 查询 WmsInboundDO
     */
    default List<WmsInboundDO> selectByWarehouseId(Long warehouseId, int limit) {
        WmsInboundPageReqVO reqVO = new WmsInboundPageReqVO();
        reqVO.setPageSize(limit);
        reqVO.setPageNo(1);
        LambdaQueryWrapperX<WmsInboundDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsInboundDO::getWarehouseId, warehouseId);
        return selectPage(reqVO, wrapper).getList();
    }
}