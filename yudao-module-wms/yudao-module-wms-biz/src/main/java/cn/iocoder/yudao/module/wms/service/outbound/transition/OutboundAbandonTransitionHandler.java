package cn.iocoder.yudao.module.wms.service.outbound.transition;


import cn.iocoder.yudao.framework.cola.statemachine.builder.TransitionContext;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundStatus;
import org.springframework.stereotype.Component;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.OUTBOUND_ABANDON_NOT_ALLOWED;

/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:16
 * @description: 作废
 */
@Component
public class OutboundAbandonTransitionHandler extends BaseOutboundTransitionHandler {

    @Override
    public boolean when(TransitionContext<WmsOutboundDO> context) {
        WmsOutboundStatus outboundStatus = WmsOutboundStatus.parse(context.data().getOutboundStatus());
        // 出库状态为部分出库或者已出库，不允许作废
        if(outboundStatus.matchAny(WmsOutboundStatus.PART, WmsOutboundStatus.ALL)) {
            throw exception(OUTBOUND_ABANDON_NOT_ALLOWED);
        }
        return super.when(context);
    }
}
