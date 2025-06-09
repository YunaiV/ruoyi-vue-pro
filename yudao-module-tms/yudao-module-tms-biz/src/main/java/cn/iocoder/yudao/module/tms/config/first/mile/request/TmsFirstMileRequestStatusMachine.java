package cn.iocoder.yudao.module.tms.config.first.mile.request;

import cn.iocoder.yudao.framework.cola.statemachine.Action;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.cola.statemachine.builder.StateMachineBuilder;
import cn.iocoder.yudao.framework.cola.statemachine.builder.StateMachineBuilderFactory;
import cn.iocoder.yudao.module.tms.config.TmsFailCallbackImpl;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.vo.TmsFirstMileRequestAuditReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.TmsFirstMileRequestDO;
import cn.iocoder.yudao.module.tms.enums.TmsEventEnum;
import cn.iocoder.yudao.module.tms.enums.status.TmsAuditStatus;
import cn.iocoder.yudao.module.tms.enums.status.TmsOffStatus;
import cn.iocoder.yudao.module.tms.enums.status.TmsOrderStatus;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static cn.iocoder.yudao.module.tms.enums.TmsStateMachines.*;

@Slf4j
@Configuration
@SuppressWarnings({"rawtypes", "unchecked"})
public class TmsFirstMileRequestStatusMachine {

    @Resource
    TmsFailCallbackImpl tmsFailCallbackImpl;

    @Autowired
    Action<TmsAuditStatus, TmsEventEnum, TmsFirstMileRequestAuditReqVO> requestAuditActionImpl;
    @Autowired
    Action<TmsOffStatus, TmsEventEnum, TmsFirstMileRequestDO> requestOffActionImpl;
    //订购
    @Autowired
    Action<TmsOrderStatus, TmsEventEnum, TmsFirstMileRequestDO> requestOrderActionImpl;

    @Bean(FIRST_MILE_REQUEST_AUDIT_STATE_MACHINE)
    public StateMachine<TmsAuditStatus, TmsEventEnum, TmsFirstMileRequestAuditReqVO> getFirstMileRequestStateMachine() {
        StateMachineBuilder<TmsAuditStatus, TmsEventEnum, TmsFirstMileRequestAuditReqVO> builder = StateMachineBuilderFactory.create();

        // 初始化状态
        builder.internalTransition().within(TmsAuditStatus.DRAFT).on(TmsEventEnum.AUDIT_INIT).perform(requestAuditActionImpl);

        // 提交审核
        builder.externalTransitions().fromAmong(TmsAuditStatus.DRAFT, TmsAuditStatus.REVOKED, TmsAuditStatus.REJECTED).to(TmsAuditStatus.PENDING_REVIEW).on(TmsEventEnum.SUBMIT_FOR_REVIEW).perform(requestAuditActionImpl);

        // 审核通过
        builder.externalTransition().from(TmsAuditStatus.PENDING_REVIEW).to(TmsAuditStatus.APPROVED).on(TmsEventEnum.AGREE).perform(requestAuditActionImpl);

        // 审核不通过
        builder.externalTransition().from(TmsAuditStatus.PENDING_REVIEW).to(TmsAuditStatus.REJECTED).on(TmsEventEnum.REJECT).perform(requestAuditActionImpl);

        // 反审核
        builder.externalTransition().from(TmsAuditStatus.APPROVED).to(TmsAuditStatus.REVOKED).on(TmsEventEnum.WITHDRAW_REVIEW).perform(requestAuditActionImpl);

        builder.setFailCallback(tmsFailCallbackImpl);

        return builder.build(FIRST_MILE_REQUEST_AUDIT_STATE_MACHINE);
    }

    @Bean(FIRST_MILE_REQUEST_OFF_STATE_MACHINE)
    public StateMachine<TmsOffStatus, TmsEventEnum, TmsFirstMileRequestDO> getPurchaseRequestStateMachine() {
        StateMachineBuilder<TmsOffStatus, TmsEventEnum, TmsFirstMileRequestDO> builder = StateMachineBuilderFactory.create();
        // 初始化状态
        builder.internalTransition().within(TmsOffStatus.OPEN).on(TmsEventEnum.OFF_INIT).perform(requestOffActionImpl);
        // 开启
        builder.externalTransitions().fromAmong(TmsOffStatus.CLOSED, TmsOffStatus.MANUAL_CLOSED).to(TmsOffStatus.OPEN).on(TmsEventEnum.ACTIVATE).perform(requestOffActionImpl);
        // 手动关闭
        builder.externalTransition().from(TmsOffStatus.OPEN).to(TmsOffStatus.MANUAL_CLOSED).on(TmsEventEnum.MANUAL_CLOSE).perform(requestOffActionImpl);
        //自动关闭
        builder.externalTransition().from(TmsOffStatus.OPEN).to(TmsOffStatus.CLOSED).on(TmsEventEnum.AUTO_CLOSE).perform(requestOffActionImpl);
        //撤销关闭
        builder.externalTransitions().fromAmong(TmsOffStatus.MANUAL_CLOSED, TmsOffStatus.CLOSED).to(TmsOffStatus.OPEN).on(TmsEventEnum.CANCEL_DELETE).perform(requestOffActionImpl);

        builder.setFailCallback(tmsFailCallbackImpl);
        return builder.build(FIRST_MILE_REQUEST_OFF_STATE_MACHINE);
    }

    @Bean(FIRST_MILE_REQUEST_PURCHASE_ORDER_STATE_MACHINE)
    public StateMachine<TmsOrderStatus, TmsEventEnum, TmsFirstMileRequestDO> buildTmsFirstMileRequestItemOrderStateMachine() {
        StateMachineBuilder<TmsOrderStatus, TmsEventEnum, TmsFirstMileRequestDO> builder = StateMachineBuilderFactory.create();
        //初始化事件
        builder.internalTransition().within(TmsOrderStatus.OT_ORDERED).on(TmsEventEnum.ORDER_INIT).perform(requestOrderActionImpl);
        // 订购数量调整
        builder.externalTransitions().fromAmong(TmsOrderStatus.OT_ORDERED, TmsOrderStatus.PARTIALLY_ORDERED, TmsOrderStatus.ORDERED, TmsOrderStatus.ORDER_FAILED)
            .to(TmsOrderStatus.PARTIALLY_ORDERED).on(TmsEventEnum.ORDER_ADJUSTMENT).perform(requestOrderActionImpl);

        //放弃订购
        builder.externalTransitions().fromAmong(TmsOrderStatus.PARTIALLY_ORDERED, TmsOrderStatus.OT_ORDERED).to(TmsOrderStatus.ORDER_FAILED).on(TmsEventEnum.ORDER_CANCEL).perform(requestOrderActionImpl);
        builder.setFailCallback(tmsFailCallbackImpl);
        return builder.build(FIRST_MILE_REQUEST_PURCHASE_ORDER_STATE_MACHINE);
    }
}
