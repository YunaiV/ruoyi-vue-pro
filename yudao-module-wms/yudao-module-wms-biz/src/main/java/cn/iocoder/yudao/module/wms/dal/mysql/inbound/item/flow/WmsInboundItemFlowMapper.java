package cn.iocoder.yudao.module.wms.dal.mysql.inbound.item.flow;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.flow.WmsInboundItemFlowDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.flow.vo.*;

/**
 * 入库单库存详情扣减 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsInboundItemFlowMapper extends BaseMapperX<WmsInboundItemFlowDO> {

    default PageResult<WmsInboundItemFlowDO> selectPage(WmsInboundItemFlowPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsInboundItemFlowDO>()
                .eqIfPresent(WmsInboundItemFlowDO::getInboundId, reqVO.getInboundId())
                .eqIfPresent(WmsInboundItemFlowDO::getInboundItemId, reqVO.getInboundItemId())
                .eqIfPresent(WmsInboundItemFlowDO::getProductId, reqVO.getProductId())
                .eqIfPresent(WmsInboundItemFlowDO::getProductSku, reqVO.getProductSku())
                .eqIfPresent(WmsInboundItemFlowDO::getOutboundId, reqVO.getOutboundId())
                .eqIfPresent(WmsInboundItemFlowDO::getOutboundItemId, reqVO.getOutboundItemId())
                .eqIfPresent(WmsInboundItemFlowDO::getChangedQuantity, reqVO.getChangedQuantity())
                .betweenIfPresent(WmsInboundItemFlowDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WmsInboundItemFlowDO::getId));
    }

}