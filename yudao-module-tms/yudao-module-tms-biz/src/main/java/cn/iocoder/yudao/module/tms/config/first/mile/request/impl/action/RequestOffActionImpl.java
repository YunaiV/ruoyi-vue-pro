package cn.iocoder.yudao.module.tms.config.first.mile.request.impl.action;

import cn.iocoder.yudao.framework.cola.statemachine.Action;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.TmsFirstMileRequestDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.item.TmsFirstMileRequestItemDO;
import cn.iocoder.yudao.module.tms.enums.TmsEventEnum;
import cn.iocoder.yudao.module.tms.enums.status.TmsOffStatus;
import cn.iocoder.yudao.module.tms.service.first.mile.request.TmsFirstMileRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
public class RequestOffActionImpl implements Action<TmsOffStatus, TmsEventEnum, TmsFirstMileRequestDO> {
    @Autowired
    @Lazy
    private TmsFirstMileRequestService tmsFirstMileRequestService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void execute(TmsOffStatus from, TmsOffStatus to, TmsEventEnum event, TmsFirstMileRequestDO context) {
        if (event != TmsEventEnum.OFF_INIT) {
            List<TmsFirstMileRequestItemDO> items = tmsFirstMileRequestService.getFirstMileRequestItemListByRequestId(context.getId());
            if (items.isEmpty()) {
                log.error("采购申请单子表不存在，ID: {}", context.getId());
                return;
            }
            // 更新主表状态
            updateMasterStatus(context.getId(), items);
        } else {
            // 更新主表状态
            tmsFirstMileRequestService.updateFirstMileRequestStatus(context.getId(), to.getCode(), null, null, null);
        }
    }

    /**
     * 更新主表状态
     *
     * @param requestId 主表ID
     * @param items     子表列表
     */
    private void updateMasterStatus(Long requestId, List<TmsFirstMileRequestItemDO> items) {
        // 判断子表状态
        boolean allOpen = isAllMatchStatus(items, TmsOffStatus.OPEN);
        boolean allManualClosed = isAllMatchStatus(items, TmsOffStatus.MANUAL_CLOSED);
        boolean allClosed = isAllMatchStatus(items, TmsOffStatus.CLOSED);
        boolean hasManualClosed = isAnyMatchStatus(items, TmsOffStatus.MANUAL_CLOSED);

        // 根据子表状态更新主表状态
        if (allOpen) {
            updateStatus(requestId, TmsOffStatus.OPEN);
        } else if (allManualClosed) {
            updateStatus(requestId, TmsOffStatus.MANUAL_CLOSED);
        } else if (allClosed) {
            updateStatus(requestId, TmsOffStatus.CLOSED);
        } else if (hasManualClosed) {
            updateStatus(requestId, TmsOffStatus.MANUAL_CLOSED);
        } else {
            updateStatus(requestId, TmsOffStatus.CLOSED);
        }
    }

    /**
     * 判断是否所有子表都匹配指定状态
     */
    private boolean isAllMatchStatus(List<TmsFirstMileRequestItemDO> items, TmsOffStatus status) {
        return items.stream()
            .allMatch(item -> item.getOffStatus() != null &&
                item.getOffStatus().equals(status.getCode()));
    }

    /**
     * 判断是否存在子表匹配指定状态
     */
    private boolean isAnyMatchStatus(List<TmsFirstMileRequestItemDO> items, TmsOffStatus status) {
        return items.stream()
            .anyMatch(item -> item.getOffStatus() != null &&
                item.getOffStatus().equals(status.getCode()));
    }

    /**
     * 更新主表状态
     */
    private void updateStatus(Long requestId, TmsOffStatus status) {
        tmsFirstMileRequestService.updateFirstMileRequestStatus(requestId, status.getCode(), null, null, null);
    }
}
