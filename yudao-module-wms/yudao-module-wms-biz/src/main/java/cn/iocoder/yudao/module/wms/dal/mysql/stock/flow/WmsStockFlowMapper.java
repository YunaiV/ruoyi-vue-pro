package cn.iocoder.yudao.module.wms.dal.mysql.stock.flow;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.flow.WmsStockFlowDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.wms.controller.admin.stock.flow.vo.*;

/**
 * 库存流水 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsStockFlowMapper extends BaseMapperX<WmsStockFlowDO> {

    default PageResult<WmsStockFlowDO> selectPage(WmsStockFlowPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsStockFlowDO>()
                .eqIfPresent(WmsStockFlowDO::getStockType, reqVO.getStockType())
                .eqIfPresent(WmsStockFlowDO::getStockId, reqVO.getStockId())
                .eqIfPresent(WmsStockFlowDO::getReason, reqVO.getReason())
                .eqIfPresent(WmsStockFlowDO::getReasonBillId, reqVO.getReasonBillId())
                .eqIfPresent(WmsStockFlowDO::getReasonItemId, reqVO.getReasonItemId())
                .eqIfPresent(WmsStockFlowDO::getPrevFlowId, reqVO.getPrevFlowId())
                .eqIfPresent(WmsStockFlowDO::getDeltaQuantity, reqVO.getDeltaQuantity())
                .eqIfPresent(WmsStockFlowDO::getPurchasePlanQuantity, reqVO.getPurchasePlanQuantity())
                .eqIfPresent(WmsStockFlowDO::getPurchaseTransitQuantity, reqVO.getPurchaseTransitQuantity())
                .eqIfPresent(WmsStockFlowDO::getReturnTransitQuantity, reqVO.getReturnTransitQuantity())
                .eqIfPresent(WmsStockFlowDO::getPendingShelvingQuantity, reqVO.getPendingShelvingQuantity())
                .eqIfPresent(WmsStockFlowDO::getAvailableQuantity, reqVO.getAvailableQuantity())
                .eqIfPresent(WmsStockFlowDO::getSellableQuantity, reqVO.getSellableQuantity())
                .eqIfPresent(WmsStockFlowDO::getPendingOutboundQuantity, reqVO.getPendingOutboundQuantity())
                .eqIfPresent(WmsStockFlowDO::getDefectiveQuantity, reqVO.getDefectiveQuantity())
                .betweenIfPresent(WmsStockFlowDO::getFlowTime, reqVO.getFlowTime())
                .betweenIfPresent(WmsStockFlowDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WmsStockFlowDO::getId));
    }

}