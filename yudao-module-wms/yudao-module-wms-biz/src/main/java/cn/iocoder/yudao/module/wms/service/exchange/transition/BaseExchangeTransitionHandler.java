package cn.iocoder.yudao.module.wms.service.exchange.transition;


import cn.iocoder.yudao.framework.cola.statemachine.builder.TransitionContext;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.WmsExchangeDO;
import cn.iocoder.yudao.module.wms.enums.exchange.WmsExchangeAuditStatus;
import cn.iocoder.yudao.module.wms.service.approval.history.ApprovalHistoryTransitionHandler;
import cn.iocoder.yudao.module.wms.service.exchange.WmsExchangeService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;


/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:23
 * @description: ExchangeTransition 基类
 */
public class BaseExchangeTransitionHandler extends ApprovalHistoryTransitionHandler<WmsExchangeAuditStatus.Event, WmsExchangeDO> {

    @Resource
    @Lazy
    protected WmsExchangeService stockCheckService;

    /**
     * 变更状态
     **/
    @Override
    public void perform(Integer from, Integer to, WmsExchangeAuditStatus.Event event, TransitionContext<WmsExchangeDO> context) {
        super.perform(from, to, event, context);
        WmsExchangeDO inboundDO = context.data();
        inboundDO.setAuditStatus(to);
        stockCheckService.updateExchangeAuditStatus(inboundDO.getId(), to);
    }

}
