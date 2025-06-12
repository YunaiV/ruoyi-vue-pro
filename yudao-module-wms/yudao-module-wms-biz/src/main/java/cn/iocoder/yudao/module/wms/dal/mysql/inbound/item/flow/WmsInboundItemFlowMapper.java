package cn.iocoder.yudao.module.wms.dal.mysql.inbound.item.flow;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.flow.vo.WmsInboundItemFlowPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundItemFlowDetailVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.flow.WmsItemFlowDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 入库单库存详情扣减 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsInboundItemFlowMapper extends BaseMapperX<WmsItemFlowDO> {

    default PageResult<WmsItemFlowDO> selectPage(WmsInboundItemFlowPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsItemFlowDO>()
            .eqIfPresent(WmsItemFlowDO::getInboundId, reqVO.getInboundId())
            .eqIfPresent(WmsItemFlowDO::getInboundItemId, reqVO.getInboundItemId())
            .eqIfPresent(WmsItemFlowDO::getProductId, reqVO.getProductId())
            .eqIfPresent(WmsItemFlowDO::getBillType, reqVO.getBillType())
            .eqIfPresent(WmsItemFlowDO::getBillId, reqVO.getBillId())
            .eqIfPresent(WmsItemFlowDO::getBillItemId, reqVO.getBillItemId())
            .betweenIfPresent(WmsItemFlowDO::getCreateTime, reqVO.getCreateTime())
            .orderByDesc(WmsItemFlowDO::getId));
    }

    /**
     * 按 inboundId 查询 WmsItemFlowDO
     */
    default List<WmsItemFlowDO> selectByInboundId(Long inboundId, int limit) {
        WmsInboundItemFlowPageReqVO reqVO = new WmsInboundItemFlowPageReqVO();
        reqVO.setPageSize(limit);
        reqVO.setPageNo(1);
        LambdaQueryWrapperX<WmsItemFlowDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsItemFlowDO::getInboundId, inboundId);
        return selectPage(reqVO, wrapper).getList();
    }

    /**
     * 按 outbound_action_id 查询 WmsItemFlowDO 清单
     */
    default List<WmsItemFlowDO> selectByOutboundActionId(Long outboundActionId) {
        return selectList(new LambdaQueryWrapperX<WmsItemFlowDO>().eq(WmsItemFlowDO::getOutboundActionId, outboundActionId));
    }


    /**
     * 按 inboundId 查询 WmsItemFlowDO
     *
     * @param outboundId 出库单编号
     * @param productId  产品编号
     */
    default List<WmsItemFlowDO> selectByOutboundId(Long outboundId, Long productId) {
        LambdaQueryWrapperX<WmsItemFlowDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsItemFlowDO::getBillId, outboundId)
            .eq(WmsItemFlowDO::getProductId, productId)
            .orderByDesc(WmsItemFlowDO::getUpdateTime)
            .last("LIMIT 1");
        return selectList(wrapper);
    }

    default List<WmsItemFlowDO> selectByInboundIds(List<Long> inboundIds, int limit) {
        WmsInboundItemFlowPageReqVO reqVO = new WmsInboundItemFlowPageReqVO();
        reqVO.setPageSize(limit);
        reqVO.setPageNo(1);
        LambdaQueryWrapperX<WmsItemFlowDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.in(WmsItemFlowDO::getInboundId, inboundIds);
        return selectPage(reqVO, wrapper).getList();
    }

    /**
     * 根据产品ID，库位ID和仓库ID查询
     */
    default List<WmsInboundItemFlowDetailVO> selectByProductIdAndBinIdAndWarehouseId(Long productId, Long binId, Long warehouseId, Integer limit) {
        MPJLambdaWrapperX<WmsItemFlowDO> wrapper = new MPJLambdaWrapperX<>();
        wrapper.selectAs(WmsItemFlowDO::getId, "id")
            .selectAs(WmsItemFlowDO::getInboundItemId, "inboundItemId")
            .selectAs(WmsItemFlowDO::getProductId, "productId")
            .selectAs(WmsItemFlowDO::getOutboundAvailableQty, "outboundAvailableQty")
            .selectAs(WmsItemFlowDO::getCreateTime, "createTime")
            .innerJoin(WmsInboundItemDO.class, WmsInboundItemDO::getId, WmsItemFlowDO::getInboundItemId)
            .innerJoin(WmsInboundDO.class, WmsInboundDO::getId, WmsInboundItemDO::getInboundId)
            .innerJoin(WmsStockBinDO.class, WmsStockBinDO::getWarehouseId, WmsInboundDO::getWarehouseId)
            .eqIfPresent(WmsInboundItemDO::getProductId, productId)
            .eqIfPresent(WmsStockBinDO::getBinId, binId)
            .eqIfPresent(WmsInboundDO::getWarehouseId, warehouseId)
            .orderByAsc(WmsInboundDO::getCreateTime)
            .last("LIMIT " + limit);
        List<WmsItemFlowDO> rtnList1 = selectList(wrapper);
        List<WmsInboundItemFlowDetailVO> rtnList = selectJoinList(WmsInboundItemFlowDetailVO.class, wrapper);

        return rtnList;
    }
}
