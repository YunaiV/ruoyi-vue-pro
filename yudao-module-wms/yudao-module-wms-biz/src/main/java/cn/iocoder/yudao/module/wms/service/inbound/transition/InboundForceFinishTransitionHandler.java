package cn.iocoder.yudao.module.wms.service.inbound.transition;



import cn.iocoder.yudao.framework.cola.statemachine.TransitionContext;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundStatus;
import org.springframework.stereotype.Component;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INBOUND_FORCE_FINISH_NOT_ALLOWED;


/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:14
 * @description: 强制完成
 */

@Component
public class InboundForceFinishTransitionHandler extends BaseInboundTransitionHandler {

    @Override
    public boolean when(TransitionContext<WmsInboundDO> context) {
        WmsInboundStatus inboundStatus = WmsInboundStatus.parse(context.data().getInboundStatus());
        if(inboundStatus.matchAny(WmsInboundStatus.PART)) {
            throw exception(INBOUND_FORCE_FINISH_NOT_ALLOWED);
        }
        return super.when(context);
    }
}