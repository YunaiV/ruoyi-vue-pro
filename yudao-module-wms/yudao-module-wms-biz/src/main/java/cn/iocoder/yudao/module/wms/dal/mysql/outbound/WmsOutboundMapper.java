package cn.iocoder.yudao.module.wms.dal.mysql.outbound;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.WmsOutboundPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 出库单 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsOutboundMapper extends BaseMapperX<WmsOutboundDO> {

    default PageResult<WmsOutboundDO> selectPage(WmsOutboundPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsOutboundDO>()
				.eqIfPresent(WmsOutboundDO::getNo, reqVO.getNo())
				.eqIfPresent(WmsOutboundDO::getWarehouseId, reqVO.getWarehouseId())
				.eqIfPresent(WmsOutboundDO::getType, reqVO.getType())
				.eqIfPresent(WmsOutboundDO::getOutboundStatus, reqVO.getOutboundStatus())
				.eqIfPresent(WmsOutboundDO::getAuditStatus, reqVO.getAuditStatus())
				.eqIfPresent(WmsOutboundDO::getSourceBillId, reqVO.getSourceBillId())
				.eqIfPresent(WmsOutboundDO::getSourceBillNo, reqVO.getSourceBillNo())
				.eqIfPresent(WmsOutboundDO::getSourceBillType, reqVO.getSourceBillType())
				.eqIfPresent(WmsOutboundDO::getCreatorComment, reqVO.getCreatorComment())
				.betweenIfPresent(WmsOutboundDO::getCreateTime, reqVO.getCreateTime())
				.orderByDesc(WmsOutboundDO::getId));
    }

    /**
     * 按 no 查询唯一的 WmsOutboundDO
     */
    default WmsOutboundDO getByNo(String no) {
        LambdaQueryWrapperX<WmsOutboundDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsOutboundDO::getNo, no);
        return selectOne(wrapper);
    }

    /**
     * 按 warehouse_id 查询 WmsOutboundDO 清单
     */
    default List<WmsOutboundDO> selectByWarehouseId(Long warehouseId) {
        return selectList(new LambdaQueryWrapperX<WmsOutboundDO>().eq(WmsOutboundDO::getWarehouseId, warehouseId));
    }

    /**
     * 按 type 查询 WmsOutboundDO 清单
     */
    default List<WmsOutboundDO> selectByType(Integer type) {
        return selectList(new LambdaQueryWrapperX<WmsOutboundDO>().eq(WmsOutboundDO::getType, type));
    }

    default List<WmsOutboundDO> getSimpleList(WmsOutboundPageReqVO pageReqVO) {
        LambdaQueryWrapperX<WmsOutboundDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.likeIfPresent(WmsOutboundDO::getNo, pageReqVO.getNo());
        wrapper.eqIfPresent(WmsOutboundDO::getWarehouseId, pageReqVO.getWarehouseId());
        wrapper.eqIfPresent(WmsOutboundDO::getType, pageReqVO.getType());
        return selectList(wrapper);
    }
}
