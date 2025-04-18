package cn.iocoder.yudao.module.wms.service.inventory.transition;

import cn.iocoder.yudao.framework.cola.statemachine.builder.FailCallback;
import cn.iocoder.yudao.framework.cola.statemachine.builder.TransitionContext;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryDO;
import cn.iocoder.yudao.module.wms.enums.inventory.WmsInventoryAuditStatus;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import org.springframework.stereotype.Component;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INBOUND_AUDIT_ERROR;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INVENTORY_AUDIT_FAIL;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INVENTORY_STATUS_PARSE_ERROR;

/**
 * @author: LeeFJ
 * @date: 2025/4/1 13:43
 * @description:
 */
@Component
public class InventoryTransitionFailCallback implements FailCallback<Integer, WmsInventoryAuditStatus.Event, TransitionContext<WmsInventoryDO>> {
    /**
     * 状态机失败情况的处理
     **/
    @Override
    public void onFail(Integer from, Integer to ,WmsInventoryAuditStatus.Event event, TransitionContext<WmsInventoryDO> context) {
        // 当前状态
        WmsOutboundAuditStatus currStatus= WmsOutboundAuditStatus.parse(context.data().getAuditStatus());
        if (currStatus == null) {
            throw exception(INVENTORY_STATUS_PARSE_ERROR);
        }

        if(to==null) {
            throw exception(INBOUND_AUDIT_ERROR);
        }

        WmsOutboundAuditStatus toAuditStatus = WmsOutboundAuditStatus.parse(to);
        if (toAuditStatus == null) {
            throw exception(INVENTORY_STATUS_PARSE_ERROR);
        }
        // 组装消息
        throw exception(INVENTORY_AUDIT_FAIL, currStatus.getLabel(), toAuditStatus.getLabel(), event.getLabel());


    }

}
