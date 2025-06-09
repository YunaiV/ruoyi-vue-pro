package cn.iocoder.yudao.module.wms.dal.mysql.inbound;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundAuditStatus;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 入库单 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsInboundMapper extends BaseMapperX<WmsInboundDO> {

    static final String PRODUCT_EXISTS_SQL = "select 1 from wms_inbound_item pi where pi.inbound_id=wms_inbound.id and pi.product_id={0}";

    default PageResult<WmsInboundDO> selectPage(WmsInboundPageReqVO reqVO) {

        LambdaQueryWrapperX query = new LambdaQueryWrapperX<WmsInboundDO>()
                .eqIfPresent(WmsInboundDO::getCode, reqVO.getCode())
                .eqIfPresent(WmsInboundDO::getType, reqVO.getType())
                .eqIfPresent(WmsInboundDO::getWarehouseId, reqVO.getWarehouseId())
                .eqIfPresent(WmsInboundDO::getAuditStatus, reqVO.getAuditStatus())
                .eqIfPresent(WmsInboundDO::getInboundStatus, reqVO.getInboundStatus())
            .eqIfPresent(WmsInboundDO::getUpstreamId, reqVO.getUpstreamId())
            .likeIfPresent(WmsInboundDO::getUpstreamCode, reqVO.getUpstreamCode())
            .eqIfPresent(WmsInboundDO::getUpstreamType, reqVO.getUpstreamType())
                .eqIfPresent(WmsInboundDO::getTraceNo, reqVO.getTraceNo())
                .eqIfPresent(WmsInboundDO::getShippingMethod, reqVO.getShippingMethod())
                .likeIfPresent(WmsInboundDO::getRemark, reqVO.getRemark())
                .eqIfPresent(WmsInboundDO::getInitAge, reqVO.getInitAge())
                .betweenIfPresent(WmsInboundDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WmsInboundDO::getId);

        if (reqVO.getProductId() != null) {
            query.exists(PRODUCT_EXISTS_SQL, reqVO.getProductId());
        }


        return selectPage(reqVO, query);
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

    default List<WmsInboundDO> getSimpleList(WmsInboundPageReqVO pageReqVO) {
        LambdaQueryWrapperX<WmsInboundDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.likeIfPresent(WmsInboundDO::getCode, pageReqVO.getCode());
        wrapper.likeIfPresent(WmsInboundDO::getTraceNo, pageReqVO.getTraceNo());
        wrapper.likeIfPresent(WmsInboundDO::getUpstreamCode, pageReqVO.getUpstreamCode());
        wrapper.eqIfPresent(WmsInboundDO::getType, pageReqVO.getType());
        wrapper.eqIfPresent(WmsInboundDO::getWarehouseId, pageReqVO.getWarehouseId());
        wrapper.eqIfPresent(WmsInboundDO::getTraceNo, pageReqVO.getTraceNo());
        return selectList(wrapper);
    }

    /**
     * 按 code 查询唯一的 WmsInboundDO
     */
    default WmsInboundDO getByCode(String code) {
        LambdaQueryWrapperX<WmsInboundDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsInboundDO::getCode, code);
        return selectOne(wrapper);
    }

    default List<WmsInboundDO> getInboundList(Integer upstreamType, Long upstreamId) {
        LambdaQueryWrapperX<WmsInboundDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsInboundDO::getUpstreamId, upstreamId);
        wrapper.eq(WmsInboundDO::getUpstreamType, upstreamType);
        //非作废审核状态
        wrapper.ne(WmsInboundDO::getAuditStatus, WmsInboundAuditStatus.ABANDONED.getValue());
        return selectList(wrapper);
    }
}
