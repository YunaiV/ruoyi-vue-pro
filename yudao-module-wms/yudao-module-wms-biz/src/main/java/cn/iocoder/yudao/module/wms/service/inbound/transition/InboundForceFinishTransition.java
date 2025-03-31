package cn.iocoder.yudao.module.wms.service.inbound.transition;



import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundAuditStatus;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundStatus;
import cn.iocoder.yudao.module.wms.statemachine.ColaContext;
import org.springframework.stereotype.Component;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INBOUND_FORCE_FINISH_NOT_ALLOWED;


/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:14
 * @description: 强制完成
 */

@Component
public class InboundForceFinishTransition extends BaseInboundTransition {
    public InboundForceFinishTransition() {
        // 指定事件以及前后的状态
        super(
            // from
            WmsInboundAuditStatus.AUDITING,
            // to
            WmsInboundAuditStatus.REJECT,
            // event
            WmsInboundAuditStatus.Event.FORCE_FINISH
        );
    }

    @Override
    public boolean when(ColaContext<WmsInboundDO> context) {
        WmsInboundStatus inboundStatus = WmsInboundStatus.parse(context.data().getInboundStatus());
        if(inboundStatus.matchAny(WmsInboundStatus.PART)) {
            throw exception(INBOUND_FORCE_FINISH_NOT_ALLOWED);
        }
        return super.when(context);
    }
}