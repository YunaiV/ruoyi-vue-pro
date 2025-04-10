package cn.iocoder.yudao.module.wms.service.outbound.transition;



import cn.iocoder.yudao.framework.cola.statemachine.TransitionContext;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import cn.iocoder.yudao.module.wms.service.approval.history.ApprovalHistoryTransitionHandler;
import cn.iocoder.yudao.module.wms.service.outbound.WmsOutboundService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;


/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:23
 * @description: OutboundTransition 基类
 */
public class BaseOutboundTransitionHandler extends ApprovalHistoryTransitionHandler<WmsOutboundAuditStatus.Event, WmsOutboundDO> {

    @Resource
    @Lazy
    protected WmsOutboundService outboundService;

    /**
     * 变更状态
     **/
    @Override
    public void perform(Integer from, Integer to, WmsOutboundAuditStatus.Event event, TransitionContext<WmsOutboundDO> context) {
        super.perform(from, to, event, context);
        WmsOutboundDO inboundDO = context.data();
        inboundDO.setAuditStatus(to);
        outboundService.updateOutboundAuditStatus(inboundDO.getId(),to);
    }

}
