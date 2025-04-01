package cn.iocoder.yudao.module.wms.service.outbound.transition;



import cn.iocoder.yudao.framework.cola.statemachine.TransitionContext;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import cn.iocoder.yudao.module.wms.service.quantity.OutboundSubmitExecutor;
import cn.iocoder.yudao.module.wms.service.quantity.context.OutboundContext;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;


/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:24
 * @description: 提交
 */
@Component
public class OutboundSubmitTransitionHandler extends BaseOutboundTransitionHandler {

    @Resource
    private OutboundSubmitExecutor outboundSubmitExecutor;

    @Override
    public void perform(Integer from, Integer to, WmsOutboundAuditStatus.Event event, TransitionContext<WmsOutboundDO> context) {
        super.perform(from, to, event, context);
        // 调整库存
        OutboundContext outboundContext = new OutboundContext();
        outboundContext.setOutboundId(context.data().getId());
        outboundSubmitExecutor.execute(outboundContext);
    }
}
