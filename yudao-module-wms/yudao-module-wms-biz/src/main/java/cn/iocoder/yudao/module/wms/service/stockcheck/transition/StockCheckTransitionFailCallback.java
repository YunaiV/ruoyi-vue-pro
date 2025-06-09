package cn.iocoder.yudao.module.wms.service.stockcheck.transition;

import cn.iocoder.yudao.framework.cola.statemachine.builder.FailCallback;
import cn.iocoder.yudao.framework.cola.statemachine.builder.TransitionContext;
import cn.iocoder.yudao.module.wms.dal.dataobject.stockcheck.WmsStockCheckDO;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import cn.iocoder.yudao.module.wms.enums.stock.check.WmsStockCheckAuditStatus;
import org.springframework.stereotype.Component;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.*;

/**
 * @author: LeeFJ
 * @date: 2025/4/1 13:43
 * @description:
 */
@Component
public class StockCheckTransitionFailCallback implements FailCallback<Integer, WmsStockCheckAuditStatus.Event, TransitionContext<WmsStockCheckDO>> {
    /**
     * 状态机失败情况的处理
     **/
    @Override
    public void onFail(Integer from, Integer to, WmsStockCheckAuditStatus.Event event, TransitionContext<WmsStockCheckDO> context) {
        // 当前状态
        WmsOutboundAuditStatus currStatus = WmsOutboundAuditStatus.parse(context.data().getAuditStatus());
        if (currStatus == null) {
            throw exception(STOCKCHECK_STATUS_PARSE_ERROR);
        }

        if(to==null) {
            throw exception(STOCKCHECK_AUDIT_ERROR, currStatus.getLabel());
        }

        WmsOutboundAuditStatus toAuditStatus = WmsOutboundAuditStatus.parse(to);
        if (toAuditStatus == null) {
            throw exception(STOCKCHECK_STATUS_PARSE_ERROR);
        }
        // 组装消息
        throw exception(STOCKCHECK_AUDIT_FAIL, currStatus.getLabel(), toAuditStatus.getLabel(), event.getLabel());


    }

}
