package cn.iocoder.yudao.module.wms.service.inbound.transition;

import cn.iocoder.yudao.framework.cola.statemachine.builder.FailCallback;
import cn.iocoder.yudao.framework.cola.statemachine.builder.TransitionContext;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundAuditStatus;
import org.springframework.stereotype.Component;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INBOUND_AUDIT_ERROR;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INBOUND_AUDIT_FAIL;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INBOUND_STATUS_PARSE_ERROR;

/**
 * @author: LeeFJ
 * @date: 2025/4/1 13:43
 * @description:
 */
@Component
public class InboundTransitionFailCallback implements FailCallback<Integer, WmsInboundAuditStatus.Event, TransitionContext<WmsInboundDO>> {

    /**
     * 状态机失败情况的处理
     **/
    @Override
    public void onFail(Integer from, Integer to, WmsInboundAuditStatus.Event event, TransitionContext<WmsInboundDO> context) {
        // 当前状态
        WmsInboundAuditStatus currStatus= WmsInboundAuditStatus.parse(context.data().getAuditStatus());
        if (currStatus == null) {
            throw exception(INBOUND_STATUS_PARSE_ERROR);
        }
        if(to==null) {
            throw exception(INBOUND_AUDIT_ERROR);
        }
        WmsInboundAuditStatus toStatus = WmsInboundAuditStatus.parse(to);
        if (toStatus == null) {
            throw exception(INBOUND_STATUS_PARSE_ERROR);
        }
        // 组装消息
        throw exception(INBOUND_AUDIT_FAIL, currStatus.getLabel(), toStatus.getLabel(), event.getLabel());
    }


}
