package cn.iocoder.yudao.module.wms.service.inbound.transition;

/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:15
 * @description: 拒绝
 */

import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundAuditStatus;
import org.springframework.stereotype.Component;

@Component
public class InboundRejectTransition extends BaseInboundTransition {
    public InboundRejectTransition() {
        // 指定事件以及前后的状态
        super(
            // from
            WmsInboundAuditStatus.AUDITING,
            // to
            WmsInboundAuditStatus.REJECT,
            // event
            WmsInboundAuditStatus.Event.REJECT
        );
    }
}
