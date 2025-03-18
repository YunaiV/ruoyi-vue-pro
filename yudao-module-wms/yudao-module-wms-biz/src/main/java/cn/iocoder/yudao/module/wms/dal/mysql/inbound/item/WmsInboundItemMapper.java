package cn.iocoder.yudao.module.wms.dal.mysql.inbound.item;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.*;

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
				.eqIfPresent(WmsInboundItemDO::getProductSku, reqVO.getProductSku())
				.eqIfPresent(WmsInboundItemDO::getPlanQuantity, reqVO.getPlanQuantity())
				.eqIfPresent(WmsInboundItemDO::getActualQuantity, reqVO.getActualQuantity())
				.eqIfPresent(WmsInboundItemDO::getLeftQuantity, reqVO.getLeftQuantity())
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
     * 按 inbound_id,product_sku 查询唯一的 WmsInboundItemDO
     */
    default WmsInboundItemDO getByInboundIdAndProductSku(Long inboundId, String productSku) {
        LambdaQueryWrapperX<WmsInboundItemDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsInboundItemDO::getInboundId, inboundId);
        wrapper.eq(WmsInboundItemDO::getProductSku, productSku);
        return selectOne(wrapper);
    }
}