package cn.iocoder.yudao.module.wms.service.inbound.transition;



import cn.iocoder.yudao.framework.cola.statemachine.TransitionContext;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundStatus;
import org.springframework.stereotype.Component;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INBOUND_ABANDON_NOT_ALLOWED;

/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:16
 * @description: 作废
 */
@Component
public class InboundAbandonTransitionHandler extends BaseInboundTransitionHandler {

    @Override
    public boolean when(TransitionContext<WmsInboundDO> context) {
        WmsInboundStatus inboundStatus = WmsInboundStatus.parse(context.data().getInboundStatus());
        // 入库状态为部分入库或者已入库，不允许作废
        if(inboundStatus.matchAny(WmsInboundStatus.PART, WmsInboundStatus.ALL)) {
            throw exception(INBOUND_ABANDON_NOT_ALLOWED);
        }
        return super.when(context);
    }
}
