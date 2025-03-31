package cn.iocoder.yudao.module.wms.service.outbound.transition;



import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import cn.iocoder.yudao.module.wms.service.quantity.OutboundSubmitExecutor;
import cn.iocoder.yudao.module.wms.service.quantity.context.OutboundContext;
import cn.iocoder.yudao.module.wms.statemachine.ColaContext;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;


/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:24
 * @description: 提交
 */
@Component
public class OutboundSubmitTransition extends BaseOutboundTransition {

    @Resource
    private OutboundSubmitExecutor outboundSubmitExecutor;

    public OutboundSubmitTransition() {
        // 指定事件以及前后的状态
        super(
            // from
            new WmsOutboundAuditStatus[]{
                WmsOutboundAuditStatus.DRAFT,
                WmsOutboundAuditStatus.REJECT
            },
            // to
            WmsOutboundAuditStatus.AUDITING,
            // event
            WmsOutboundAuditStatus.Event.SUBMIT
        );
    }

    @Override
    public void perform(Integer from, Integer to, WmsOutboundAuditStatus.Event event, ColaContext<WmsOutboundDO> context) {
        super.perform(from, to, event, context);
        // 调整库存
        OutboundContext outboundContext = new OutboundContext();
        outboundContext.setOutboundId(context.data().getId());
        outboundSubmitExecutor.execute(outboundContext);
    }
}
