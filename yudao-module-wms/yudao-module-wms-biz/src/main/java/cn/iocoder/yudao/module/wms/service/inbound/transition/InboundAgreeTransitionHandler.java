package cn.iocoder.yudao.module.wms.service.inbound.transition;

/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:14
 * @description: 同意
 */

import cn.iocoder.yudao.framework.cola.statemachine.TransitionContext;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundAuditStatus;
import cn.iocoder.yudao.module.wms.service.quantity.InboundExecutor;
import cn.iocoder.yudao.module.wms.service.quantity.context.InboundContext;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;


@Component
public class InboundAgreeTransitionHandler extends BaseInboundTransitionHandler {

    @Resource
    private InboundExecutor inboundExecutor;

    @Override
    public void perform(Integer from, Integer to, WmsInboundAuditStatus.Event event, TransitionContext<WmsInboundDO> context) {
        super.perform(from, to, event, context);
        // 调整库存
        InboundContext inboundContext=new InboundContext();
        inboundContext.setInboundId(context.data().getId());
        inboundExecutor.execute(inboundContext);

    }
}
