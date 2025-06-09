package cn.iocoder.yudao.module.wms.dal.mysql.pickup.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.pickup.item.vo.WmsPickupItemPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.item.WmsPickupItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Set;

/**
 * 拣货单详情 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsPickupItemMapper extends BaseMapperX<WmsPickupItemDO> {

    default PageResult<WmsPickupItemDO> selectPage(WmsPickupItemPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsPickupItemDO>()
				.eqIfPresent(WmsPickupItemDO::getPickupId, reqVO.getPickupId())
				.eqIfPresent(WmsPickupItemDO::getInboundId, reqVO.getInboundId())
				.eqIfPresent(WmsPickupItemDO::getInboundItemId, reqVO.getInboundItemId())
				.eqIfPresent(WmsPickupItemDO::getProductId, reqVO.getProductId())
				.eqIfPresent(WmsPickupItemDO::getQty, reqVO.getQty())
				.eqIfPresent(WmsPickupItemDO::getBinId, reqVO.getBinId())
				.betweenIfPresent(WmsPickupItemDO::getCreateTime, reqVO.getCreateTime())
				.orderByDesc(WmsPickupItemDO::getId));
    }

    /**
     * 按 pickup_id 查询 WmsPickupItemDO 清单
     */
    default List<WmsPickupItemDO> selectByPickupId(Long pickupId) {
        return selectList(new LambdaQueryWrapperX<WmsPickupItemDO>().eq(WmsPickupItemDO::getPickupId, pickupId));
    }

    default List<WmsPickupItemDO> getPickupItemListByInboundItemIds(Set<Long> inboundItemIds) {
        return selectList(new LambdaQueryWrapperX<WmsPickupItemDO>().in(WmsPickupItemDO::getInboundItemId, inboundItemIds));
    }

    default WmsPickupItemDO getByInboundIdAndProductId(Long inboundId, Long productId) {
        return selectOne(new LambdaQueryWrapperX<WmsPickupItemDO>().eq(WmsPickupItemDO::getInboundId, inboundId)
                .eq(WmsPickupItemDO::getProductId, productId));
    }
}
