package cn.iocoder.yudao.module.wms.service.exchange.transition;

import cn.iocoder.yudao.framework.cola.statemachine.builder.FailCallback;
import cn.iocoder.yudao.framework.cola.statemachine.builder.TransitionContext;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.WmsExchangeDO;
import cn.iocoder.yudao.module.wms.enums.exchange.WmsExchangeAuditStatus;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import org.springframework.stereotype.Component;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.*;

/**
 * @author: LeeFJ
 * @date: 2025/4/1 13:43
 * @description:
 */
@Component
public class ExchangeTransitionFailCallback implements FailCallback<Integer, WmsExchangeAuditStatus.Event, TransitionContext<WmsExchangeDO>> {
    /**
     * 状态机失败情况的处理
     **/
    @Override
    public void onFail(Integer from, Integer to ,WmsExchangeAuditStatus.Event event, TransitionContext<WmsExchangeDO> context) {
        // 当前状态
        WmsOutboundAuditStatus currStatus= WmsOutboundAuditStatus.parse(context.data().getAuditStatus());
        if (currStatus == null) {
            throw exception(EXCHANGE_STATUS_PARSE_ERROR);
        }

        if(to==null) {
            throw exception(EXCHANGE_AUDIT_ERROR);
        }

        WmsOutboundAuditStatus toAuditStatus = WmsOutboundAuditStatus.parse(to);
        if (toAuditStatus == null) {
            throw exception(EXCHANGE_STATUS_PARSE_ERROR);
        }
        // 组装消息
        throw exception(EXCHANGE_AUDIT_FAIL, currStatus.getLabel(), toAuditStatus.getLabel(), event.getLabel());


    }

}
