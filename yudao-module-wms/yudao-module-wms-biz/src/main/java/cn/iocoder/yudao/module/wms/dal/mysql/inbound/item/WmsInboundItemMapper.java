package cn.iocoder.yudao.module.wms.dal.mysql.inbound.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsPickupPendingPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.enums.inbound.InboundStatus;
import org.apache.ibatis.annotations.Mapper;
import java.util.Arrays;
import java.util.List;

/**
 * 入库单详情 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsInboundItemMapper extends BaseMapperX<WmsInboundItemDO> {

    default PageResult<WmsInboundItemDO> selectPage(WmsInboundItemPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsInboundItemDO>()
				.eqIfPresent(WmsInboundItemDO::getInboundId, reqVO.getInboundId())
				.eqIfPresent(WmsInboundItemDO::getProductId, reqVO.getProductId())
				.eqIfPresent(WmsInboundItemDO::getPlanQty, reqVO.getPlanQty())
				.eqIfPresent(WmsInboundItemDO::getActualQty, reqVO.getActualQty())
				.eqIfPresent(WmsInboundItemDO::getOutboundAvailableQty, reqVO.getOutboundAvailableQty())
				.eqIfPresent(WmsInboundItemDO::getSourceItemId, reqVO.getSourceItemId())
				.betweenIfPresent(WmsInboundItemDO::getCreateTime, reqVO.getCreateTime())
				.orderByDesc(WmsInboundItemDO::getId));
    }

    /**
     * 按 inbound_id,product_id 查询唯一的 WmsInboundItemDO
     */
    default WmsInboundItemDO getByInboundIdAndProductId(Long inboundId, Long productId) {
        LambdaQueryWrapperX<WmsInboundItemDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsInboundItemDO::getInboundId, inboundId);
        wrapper.eq(WmsInboundItemDO::getProductId, productId);
        return selectOne(wrapper);
    }

    /**
     * 按 inboundId 查询 WmsInboundItemDO
     */
    default List<WmsInboundItemDO> selectByInboundId(Long inboundId, int limit) {
        WmsInboundItemPageReqVO reqVO = new WmsInboundItemPageReqVO();
        reqVO.setPageSize(limit);
        reqVO.setPageNo(1);
        LambdaQueryWrapperX<WmsInboundItemDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsInboundItemDO::getInboundId, inboundId);
        return selectPage(reqVO, wrapper).getList();
    }

    default PageResult<WmsInboundItemDO> getPickupPending(WmsPickupPendingPageReqVO reqVO) {
        MPJLambdaWrapperX<WmsInboundItemDO> query = new MPJLambdaWrapperX<>();
        query.gt(WmsInboundItemDO::getActualQty, WmsInboundItemDO::getShelvedQty).innerJoin(WmsInboundDO.class, WmsInboundDO::getId, WmsInboundItemDO::getInboundId).likeIfExists(WmsInboundDO::getNo, reqVO.getInboundNo())
				.orderByDesc(WmsInboundItemDO::getId);
        return selectPage(reqVO, query);
    }

    default List<WmsInboundItemDO> selectItemListHasAvailableQty(Long warehouseId, Long productId) {
        MPJLambdaWrapperX<WmsInboundItemDO> query = new MPJLambdaWrapperX<>();
        query.select(WmsInboundItemDO::getId, WmsInboundItemDO::getInboundId, WmsInboundItemDO::getProductId, WmsInboundItemDO::getOutboundAvailableQty);
        query.innerJoin(WmsInboundDO.class, WmsInboundDO::getId, WmsInboundItemDO::getInboundId);
        query.in(WmsInboundDO::getInboundStatus, Arrays.asList(InboundStatus.ALL.getValue(), InboundStatus.PART.getValue()));
        query.eq(WmsInboundDO::getWarehouseId, warehouseId).eq(WmsInboundItemDO::getProductId, productId).gt(WmsInboundItemDO::getOutboundAvailableQty, 0).last("ORDER BY DATEDIFF(t1.inbound_time, now())+t1.init_age desc");
        return selectList(query);
    }
}