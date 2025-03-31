package cn.iocoder.yudao.module.wms.service.outbound.transition;



import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import cn.iocoder.yudao.module.wms.service.quantity.OutboundRejectExecutor;
import cn.iocoder.yudao.module.wms.service.quantity.context.OutboundContext;
import cn.iocoder.yudao.module.wms.statemachine.ColaContext;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;


/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:27
 * @description: 拒绝
 */
@Component
public class OutboundRejectTransition extends BaseOutboundTransition {

    @Resource
    private OutboundRejectExecutor outboundRejectExecutor;

    public OutboundRejectTransition() {
        // 指定事件以及前后的状态
        super(
            // from
            WmsOutboundAuditStatus.AUDITING,
            // to
            WmsOutboundAuditStatus.REJECT,
            // event
            WmsOutboundAuditStatus.Event.REJECT
        );
    }

    @Override
    public void perform(Integer from, Integer to, WmsOutboundAuditStatus.Event event, ColaContext<WmsOutboundDO> context) {
        super.perform(from, to, event, context);
        // 调整库存
        OutboundContext outboundContext = new OutboundContext();
        outboundContext.setOutboundId(context.data().getId());
        outboundRejectExecutor.execute(outboundContext);
    }


}
