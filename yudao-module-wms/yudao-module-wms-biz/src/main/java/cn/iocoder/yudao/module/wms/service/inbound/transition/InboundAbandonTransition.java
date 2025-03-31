package cn.iocoder.yudao.module.wms.service.inbound.transition;



import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundAuditStatus;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundStatus;
import cn.iocoder.yudao.module.wms.statemachine.ColaContext;
import org.springframework.stereotype.Component;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INBOUND_ABANDON_NOT_ALLOWED;

/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:16
 * @description: 作废
 */
@Component
public class InboundAbandonTransition extends BaseInboundTransition {
    public InboundAbandonTransition() {
        // 指定事件以及前后的状态
        super(
            // from
            new WmsInboundAuditStatus[]{
                WmsInboundAuditStatus.DRAFT,
                WmsInboundAuditStatus.REJECT,
                WmsInboundAuditStatus.AUDITING
            },
            // to
            WmsInboundAuditStatus.ABANDONED,
            // event
            WmsInboundAuditStatus.Event.REJECT
        );
    }

    @Override
    public boolean when(ColaContext<WmsInboundDO> context) {
        WmsInboundStatus inboundStatus = WmsInboundStatus.parse(context.data().getInboundStatus());
        if(inboundStatus.matchAny(WmsInboundStatus.PART, WmsInboundStatus.ALL)) {
            throw exception(INBOUND_ABANDON_NOT_ALLOWED);
        }
        return super.when(context);
    }
}
