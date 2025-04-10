package cn.iocoder.yudao.module.wms.service.inbound.transition;



import cn.iocoder.yudao.framework.cola.statemachine.TransitionContext;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundAuditStatus;
import cn.iocoder.yudao.module.wms.service.approval.history.ApprovalHistoryTransitionHandler;
import cn.iocoder.yudao.module.wms.service.inbound.WmsInboundService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;

/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:17
 * @description: InboundTransition 基类
 */
public class BaseInboundTransitionHandler extends ApprovalHistoryTransitionHandler<WmsInboundAuditStatus.Event, WmsInboundDO> {

    @Resource
    @Lazy
    protected WmsInboundService inboundService;

    /**
     * 默认的条件判断都为通过
     **/
    @Override
    public boolean when(TransitionContext<WmsInboundDO> context) {
        return context.success();
    }

    /**
     * 变更状态
     **/
    @Override
    public void perform(Integer from, Integer to, WmsInboundAuditStatus.Event event, TransitionContext<WmsInboundDO> context) {
        super.perform(from, to, event, context);
        // 变更状态值
        WmsInboundDO inboundDO = context.data();
        inboundDO.setAuditStatus(to);
        inboundService.updateInboundAuditStatus(inboundDO.getId(),to);
    }

}
