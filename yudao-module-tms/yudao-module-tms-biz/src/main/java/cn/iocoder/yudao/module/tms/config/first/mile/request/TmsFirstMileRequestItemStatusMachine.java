package cn.iocoder.yudao.module.tms.config.first.mile.request;

import cn.iocoder.yudao.framework.cola.statemachine.Action;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.cola.statemachine.builder.StateMachineBuilder;
import cn.iocoder.yudao.framework.cola.statemachine.builder.StateMachineBuilderFactory;
import cn.iocoder.yudao.module.tms.api.first.mile.request.TmsFistMileRequestItemDTO;
import cn.iocoder.yudao.module.tms.config.TmsFailCallbackImpl;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.item.TmsFirstMileRequestItemDO;
import cn.iocoder.yudao.module.tms.enums.TmsEventEnum;
import cn.iocoder.yudao.module.tms.enums.status.TmsOffStatus;
import cn.iocoder.yudao.module.tms.enums.status.TmsOrderStatus;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static cn.iocoder.yudao.module.tms.enums.TmsStateMachines.FIRST_MILE_REQUEST_ITEM_OFF_STATE_MACHINE;
import static cn.iocoder.yudao.module.tms.enums.TmsStateMachines.FIRST_MILE_REQUEST_ITEM_ORDER_STATE_MACHINE;

@Slf4j
@Configuration
@SuppressWarnings({"rawtypes", "unchecked"})
public class TmsFirstMileRequestItemStatusMachine {

    @Resource
    TmsFailCallbackImpl tmsFailCallbackImpl;


    @Autowired
    Action<TmsOffStatus, TmsEventEnum, TmsFirstMileRequestItemDO> requestItemOrderAction;
    @Bean(FIRST_MILE_REQUEST_ITEM_OFF_STATE_MACHINE)
    public StateMachine<TmsOffStatus, TmsEventEnum, TmsFirstMileRequestItemDO> buildTmsFirstMileRequestStateMachine() {
        StateMachineBuilder<TmsOffStatus, TmsEventEnum, TmsFirstMileRequestItemDO> builder = StateMachineBuilderFactory.create();
        // 初始化状态
        builder.internalTransition().within(TmsOffStatus.OPEN).on(TmsEventEnum.OFF_INIT).perform(requestItemOrderAction);
        // 开启
        builder.externalTransitions().fromAmong(TmsOffStatus.CLOSED, TmsOffStatus.MANUAL_CLOSED).to(TmsOffStatus.OPEN).on(TmsEventEnum.ACTIVATE).perform(requestItemOrderAction);
        // 手动关闭
        builder.externalTransition().from(TmsOffStatus.OPEN).to(TmsOffStatus.MANUAL_CLOSED).on(TmsEventEnum.MANUAL_CLOSE).perform(requestItemOrderAction);
        //自动关闭
        builder.externalTransition().from(TmsOffStatus.OPEN).to(TmsOffStatus.CLOSED).on(TmsEventEnum.AUTO_CLOSE).perform(requestItemOrderAction);
        //撤销关闭
        builder.externalTransitions().fromAmong(TmsOffStatus.MANUAL_CLOSED, TmsOffStatus.CLOSED).to(TmsOffStatus.OPEN).on(TmsEventEnum.CANCEL_DELETE).perform(requestItemOrderAction);

        builder.setFailCallback(tmsFailCallbackImpl);
        return builder.build(FIRST_MILE_REQUEST_ITEM_OFF_STATE_MACHINE);
    }

    @Autowired
    Action<TmsOrderStatus, TmsEventEnum, TmsFistMileRequestItemDTO> requestItemOrderActionImpl;
    @Bean(FIRST_MILE_REQUEST_ITEM_ORDER_STATE_MACHINE)
    public StateMachine<TmsOrderStatus, TmsEventEnum, TmsFistMileRequestItemDTO> buildTmsFirstMileRequestItemOrderStateMachine() {
        StateMachineBuilder<TmsOrderStatus, TmsEventEnum, TmsFistMileRequestItemDTO> builder = StateMachineBuilderFactory.create();
        //初始化事件
        builder.internalTransition().within(TmsOrderStatus.OT_ORDERED).on(TmsEventEnum.ORDER_INIT).perform(requestItemOrderActionImpl);
        // 订购数量调整
        builder.externalTransitions().fromAmong(TmsOrderStatus.OT_ORDERED, TmsOrderStatus.PARTIALLY_ORDERED, TmsOrderStatus.ORDERED, TmsOrderStatus.ORDER_FAILED)
            .to(TmsOrderStatus.PARTIALLY_ORDERED).on(TmsEventEnum.ORDER_ADJUSTMENT).perform(requestItemOrderActionImpl);

        //放弃订购
        builder.externalTransitions().fromAmong(TmsOrderStatus.PARTIALLY_ORDERED, TmsOrderStatus.OT_ORDERED).to(TmsOrderStatus.ORDER_FAILED).on(TmsEventEnum.ORDER_CANCEL).perform(requestItemOrderActionImpl);
        builder.setFailCallback(tmsFailCallbackImpl);
        return builder.build(FIRST_MILE_REQUEST_ITEM_ORDER_STATE_MACHINE);
    }
}
