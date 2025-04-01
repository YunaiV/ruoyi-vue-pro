package cn.iocoder.yudao.module.wms.service.outbound.transition;

import cn.iocoder.yudao.framework.cola.statemachine.TransitionContext;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundAuditStatus;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import com.alibaba.cola.statemachine.builder.FailCallback;
import org.springframework.stereotype.Component;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * @author: LeeFJ
 * @date: 2025/4/1 13:43
 * @description:
 */
@Component
public class OutboundTransitionFailCallback implements FailCallback<Integer, WmsOutboundAuditStatus.Event, TransitionContext<WmsOutboundDO>> {
    /**
     * 状态机失败情况的处理
     **/
    @Override
    public void onFail(Integer from, WmsOutboundAuditStatus.Event event, TransitionContext<WmsOutboundDO> context) {
        // 当前状态
        WmsOutboundAuditStatus currStatus= WmsOutboundAuditStatus.parse(context.data().getAuditStatus());
        if (currStatus == null) {
            throw exception(OUTBOUND_STATUS_PARSE_ERROR);
        }
        // 目标状态
        Integer to = context.getTo(from, event);
        WmsOutboundAuditStatus toAuditStatus = WmsOutboundAuditStatus.parse(to);
        if (toAuditStatus == null) {
            throw exception(OUTBOUND_STATUS_PARSE_ERROR);
        }
        // 组装消息
        throw exception(OUTBOUND_AUDIT_FAIL, currStatus.getLabel(), toAuditStatus.getLabel(), event.getLabel());
    }

}
