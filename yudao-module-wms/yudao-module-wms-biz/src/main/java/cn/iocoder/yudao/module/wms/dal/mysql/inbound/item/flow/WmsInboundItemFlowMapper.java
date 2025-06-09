package cn.iocoder.yudao.module.wms.dal.mysql.inbound.item.flow;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.flow.vo.WmsInboundItemFlowPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.flow.WmsInboundItemFlowDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

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
				.eqIfPresent(WmsInboundItemFlowDO::getBillType, reqVO.getBillType())
				.eqIfPresent(WmsInboundItemFlowDO::getBillId, reqVO.getBillId())
				.eqIfPresent(WmsInboundItemFlowDO::getBillItemId, reqVO.getBillItemId())
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

    /**
     * 按 outbound_action_id 查询 WmsInboundItemFlowDO 清单
     */
    default List<WmsInboundItemFlowDO> selectByOutboundActionId(Long outboundActionId) {
        return selectList(new LambdaQueryWrapperX<WmsInboundItemFlowDO>().eq(WmsInboundItemFlowDO::getOutboundActionId, outboundActionId));
    }


    /**
     * 按 inboundId 查询 WmsInboundItemFlowDO
     *
     * @param outboundId 出库单编号
     * @param productId  产品编号
     */
    default List<WmsInboundItemFlowDO> selectByOutboundId(Long outboundId, Long productId) {
        LambdaQueryWrapperX<WmsInboundItemFlowDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsInboundItemFlowDO::getBillId, outboundId)
            .eq(WmsInboundItemFlowDO::getProductId, productId)
            .orderByDesc(WmsInboundItemFlowDO::getUpdateTime)
            .last("LIMIT 1");
        return selectList(wrapper);
    }
}
