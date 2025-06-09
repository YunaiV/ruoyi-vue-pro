package cn.iocoder.yudao.module.wms.service.stockcheck.transition;


import cn.iocoder.yudao.framework.cola.statemachine.builder.TransitionContext;
import cn.iocoder.yudao.module.wms.dal.dataobject.stockcheck.WmsStockCheckDO;
import cn.iocoder.yudao.module.wms.enums.stock.check.WmsStockCheckAuditStatus;
import cn.iocoder.yudao.module.wms.service.approval.history.ApprovalHistoryTransitionHandler;
import cn.iocoder.yudao.module.wms.service.stockcheck.WmsStockCheckService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;


/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:23
 * @description: StockCheckTransition 基类
 */
public class BaseStockCheckTransitionHandler extends ApprovalHistoryTransitionHandler<WmsStockCheckAuditStatus.Event, WmsStockCheckDO> {

    @Resource
    @Lazy
    protected WmsStockCheckService stockCheckDOService;

    /**
     * 变更状态
     **/
    @Override
    public void perform(Integer from, Integer to, WmsStockCheckAuditStatus.Event event, TransitionContext<WmsStockCheckDO> context) {
        super.perform(from, to, event, context);
        WmsStockCheckDO inboundDO = context.data();
        inboundDO.setAuditStatus(to);
        stockCheckDOService.updateOutboundAuditStatus(inboundDO.getId(), to);
    }

}
