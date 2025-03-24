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
				.eqIfPresent(WmsInboundItemFlowDO::getOutboundId, reqVO.getOutboundId())
				.eqIfPresent(WmsInboundItemFlowDO::getOutboundItemId, reqVO.getOutboundItemId())
				.eqIfPresent(WmsInboundItemFlowDO::getOutboundQuantity, reqVO.getOutboundQuantity())
				.betweenIfPresent(WmsInboundItemFlowDO::getCreateTime, reqVO.getCreateTime())
				.orderByDesc(WmsInboundItemFlowDO::getId));
    }

    /**
     * 按 inboundId 查询 WmsInboundItemFlowDO
     */
    default List<WmsInboundItemFlowDO> selectByInboundId(Long inboundId, int limit) {
        WmsInboundItemFlowPageReqVO reqVO = new WmsInboundItemFlowPageReqVO();
        reqVO.setPageSize(limit);
        reqVO.setPageNo(1);
        LambdaQueryWrapperX<WmsInboundItemFlowDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsInboundItemFlowDO::getInboundId, inboundId);
        return selectPage(reqVO, wrapper).getList();
    }
}
