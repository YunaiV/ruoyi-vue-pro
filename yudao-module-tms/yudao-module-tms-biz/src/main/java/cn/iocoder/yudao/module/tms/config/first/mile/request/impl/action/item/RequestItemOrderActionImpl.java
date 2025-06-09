package cn.iocoder.yudao.module.tms.config.first.mile.request.impl.action.item;

import cn.iocoder.yudao.framework.cola.statemachine.Action;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.module.tms.api.first.mile.request.TmsFistMileRequestItemDTO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.TmsFirstMileRequestDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.item.TmsFirstMileRequestItemDO;
import cn.iocoder.yudao.module.tms.enums.TmsEventEnum;
import cn.iocoder.yudao.module.tms.enums.status.TmsOrderStatus;
import cn.iocoder.yudao.module.tms.service.first.mile.request.TmsFirstMileRequestItemService;
import cn.iocoder.yudao.module.tms.service.first.mile.request.TmsFirstMileRequestService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static cn.iocoder.yudao.module.tms.enums.TmsStateMachines.FIRST_MILE_REQUEST_PURCHASE_ORDER_STATE_MACHINE;

@Slf4j
@Component
public class RequestItemOrderActionImpl implements Action<TmsOrderStatus, TmsEventEnum, TmsFistMileRequestItemDTO> {
    @Autowired
    @Lazy
    private TmsFirstMileRequestItemService tmsFirstMileRequestItemService;
    @Autowired
    @Lazy
    private TmsFirstMileRequestService tmsFirstMileRequestService;

    @Resource(name = FIRST_MILE_REQUEST_PURCHASE_ORDER_STATE_MACHINE)
    private StateMachine<TmsOrderStatus, TmsEventEnum, TmsFirstMileRequestDO> stateMachine;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(TmsOrderStatus from, TmsOrderStatus to, TmsEventEnum event, TmsFistMileRequestItemDTO dto) {
        TmsFirstMileRequestItemDO tmsFirstMileRequestItemDO = tmsFirstMileRequestItemService.validateFirstMileRequestItemExists(dto.getItemId());
        Integer oldClosedQty = tmsFirstMileRequestItemDO.getOrderClosedQty();
        oldClosedQty = oldClosedQty == null ? 0 : oldClosedQty;
        // 更新子表订单状态
        if (event == TmsEventEnum.ORDER_ADJUSTMENT) {
            //采购数量调整
            oldClosedQty += dto.getQty();
        }
        tmsFirstMileRequestItemService.updateFirstMileRequestItemStatus(dto.getItemId(), null, to.getCode(), oldClosedQty);

        log.debug("更新采购申请单子表订单状态，ID: {}, 从状态: {}, 到状态: {}, 事件: {}", dto.getItemId(), from, to, event);

        if (event != TmsEventEnum.ORDER_INIT) {
            TmsFirstMileRequestDO mileRequest = tmsFirstMileRequestService.getFirstMileRequest(tmsFirstMileRequestItemDO.getRequestId());
            //传递给主状态机
            stateMachine.fireEvent(TmsOrderStatus.fromCode(mileRequest.getOrderStatus()), TmsEventEnum.ORDER_ADJUSTMENT, mileRequest);
        }
    }
}
