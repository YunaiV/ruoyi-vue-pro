package cn.iocoder.yudao.module.tms.config.first.mile.request.impl.action;

import cn.iocoder.yudao.framework.cola.statemachine.Action;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.TmsFirstMileRequestDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.item.TmsFirstMileRequestItemDO;
import cn.iocoder.yudao.module.tms.enums.TmsEventEnum;
import cn.iocoder.yudao.module.tms.enums.status.TmsOrderStatus;
import cn.iocoder.yudao.module.tms.service.first.mile.request.TmsFirstMileRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
public class RequestOrderActionImpl implements Action<TmsOrderStatus, TmsEventEnum, TmsFirstMileRequestDO> {
    @Autowired
    @Lazy
    private TmsFirstMileRequestService tmsFirstMileRequestService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(TmsOrderStatus from, TmsOrderStatus to, TmsEventEnum event, TmsFirstMileRequestDO context) {
        // 如果是初始化事件，直接更新状态
        if (event == TmsEventEnum.ORDER_INIT) {
            updateMasterStatus(context.getId(), to);
            return;
        }

        List<TmsFirstMileRequestItemDO> items = tmsFirstMileRequestService.getFirstMileRequestItemListByRequestId(context.getId());
        if (items.isEmpty()) {
            log.error("采购申请单子表不存在，ID: {}", context.getId());
            return;
        }
        updateMasterStatusByItems(context.getId(), items);
    }

    /**
     * 根据子表状态更新主表状态
     */
    private void updateMasterStatusByItems(Long requestId, List<TmsFirstMileRequestItemDO> items) {
        // 判断子表状态
        boolean allUnordered = isAllMatchStatus(items, TmsOrderStatus.OT_ORDERED);
        boolean allOrdered = isAllMatchStatus(items, TmsOrderStatus.ORDERED);
        boolean hasOrderFailed = isAnyMatchStatus(items, TmsOrderStatus.ORDER_FAILED);

        // 根据优先级更新主表状态
        if (hasOrderFailed) {
            // 优先级1：存在订购失败
            updateMasterStatus(requestId, TmsOrderStatus.ORDER_FAILED);
        } else if (allOrdered) {
            // 优先级2：全部已订购
            updateMasterStatus(requestId, TmsOrderStatus.ORDERED);
        } else if (allUnordered) {
            // 优先级3：全部未订购
            updateMasterStatus(requestId, TmsOrderStatus.OT_ORDERED);
        } else {
            // 优先级4：部分订购
            updateMasterStatus(requestId, TmsOrderStatus.PARTIALLY_ORDERED);
        }
    }

    /**
     * 判断是否所有子表都匹配指定状态
     */
    private boolean isAllMatchStatus(List<TmsFirstMileRequestItemDO> items, TmsOrderStatus status) {
        return items.stream().allMatch(item -> item.getOrderStatus() != null && item.getOrderStatus().equals(status.getCode()));
    }

    /**
     * 判断是否存在子表匹配指定状态
     */
    private boolean isAnyMatchStatus(List<TmsFirstMileRequestItemDO> items, TmsOrderStatus status) {
        return items.stream().anyMatch(item -> item.getOrderStatus() != null && item.getOrderStatus().equals(status.getCode()));
    }

    /**
     * 更新主表状态
     */
    private void updateMasterStatus(Long requestId, TmsOrderStatus status) {
        tmsFirstMileRequestService.updateFirstMileRequestStatus(requestId, null, status.getCode(), null, null);
        log.debug("更新采购申请单订单状态，ID: {}, 状态: {}", requestId, status);
    }
}
