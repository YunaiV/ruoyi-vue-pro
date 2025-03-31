package cn.iocoder.yudao.module.wms.service.outbound.transition;



import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import org.springframework.stereotype.Component;


/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:25
 * @description: 同意
 */
@Component
public class OutboundAgreeTransition extends BaseOutboundTransition {

    public OutboundAgreeTransition() {
        // 指定事件以及前后的状态
        super(
            // from
            WmsOutboundAuditStatus.AUDITING,
            // to
            WmsOutboundAuditStatus.PASS,
            // event
            WmsOutboundAuditStatus.Event.AGREE
        );
    }

}
