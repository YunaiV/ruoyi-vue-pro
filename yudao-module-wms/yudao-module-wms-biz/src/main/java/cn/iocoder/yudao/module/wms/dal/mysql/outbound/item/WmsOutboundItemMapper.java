package cn.iocoder.yudao.module.wms.dal.mysql.outbound.item;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.item.WmsOutboundItemDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo.*;

/**
 * 出库单详情 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsOutboundItemMapper extends BaseMapperX<WmsOutboundItemDO> {

    default PageResult<WmsOutboundItemDO> selectPage(WmsOutboundItemPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsOutboundItemDO>()
				.eqIfPresent(WmsOutboundItemDO::getOutboundId, reqVO.getOutboundId())
				.eqIfPresent(WmsOutboundItemDO::getProductId, reqVO.getProductId())
				.eqIfPresent(WmsOutboundItemDO::getPlanQty, reqVO.getPlanQty())
				.eqIfPresent(WmsOutboundItemDO::getActualQty, reqVO.getActualQty())
				.eqIfPresent(WmsOutboundItemDO::getSourceItemId, reqVO.getSourceItemId())
				.betweenIfPresent(WmsOutboundItemDO::getCreateTime, reqVO.getCreateTime())
				.orderByDesc(WmsOutboundItemDO::getId));
    }

    /**
     * 按 outbound_id,product_id 查询唯一的 WmsOutboundItemDO
     */
    default WmsOutboundItemDO getByOutboundIdAndProductId(Long outboundId, Long productId) {
        LambdaQueryWrapperX<WmsOutboundItemDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsOutboundItemDO::getOutboundId, outboundId);
        wrapper.eq(WmsOutboundItemDO::getProductId, productId);
        return selectOne(wrapper);
    }

    default List<WmsOutboundItemDO> selectByOutboundId(Long outboundId) {
        LambdaQueryWrapperX<WmsOutboundItemDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsOutboundItemDO::getOutboundId, outboundId);
        return selectList(wrapper);
    }
}