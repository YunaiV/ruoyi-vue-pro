package cn.iocoder.yudao.module.wms.service.outbound.transition;



import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import cn.iocoder.yudao.module.wms.service.approval.history.ApprovalHistoryTransitionHandler;
import cn.iocoder.yudao.module.wms.service.outbound.WmsOutboundService;
import cn.iocoder.yudao.module.wms.statemachine.TransitionContext;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;


/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:23
 * @description: OutboundAction 基类
 */
public class BaseOutboundTransitionHandler extends ApprovalHistoryTransitionHandler<WmsOutboundAuditStatus.Event, WmsOutboundDO> {

    @Resource
    @Lazy
    protected WmsOutboundService outboundService;

//    /**
//     * 多个 from 的构造函数
//     **/
//    public BaseOutboundTransition(WmsOutboundAuditStatus[] from, WmsOutboundAuditStatus to, WmsOutboundAuditStatus.Event event) {
//        super(Arrays.stream(from).map(WmsOutboundAuditStatus::getValue).toArray(Integer[]::new), to.getValue(), WmsOutboundDO::getAuditStatus, event);
//    }

//    /**
//     * 单个 from 的构造函数
//     **/
//    public BaseOutboundTransition(WmsOutboundAuditStatus from, WmsOutboundAuditStatus to, WmsOutboundAuditStatus.Event event) {
//        super(new Integer[]{from.getValue()}, to.getValue(), WmsOutboundDO::getAuditStatus, event);
//    }

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
