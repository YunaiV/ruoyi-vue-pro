package cn.iocoder.yudao.module.srm.config.purchase.order;

import cn.iocoder.yudao.framework.cola.statemachine.Action;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.cola.statemachine.builder.StateMachineBuilder;
import cn.iocoder.yudao.framework.cola.statemachine.builder.StateMachineBuilderFactory;
import cn.iocoder.yudao.module.srm.config.BaseFailCallbackImpl;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.req.SrmPurchaseOrderAuditReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.*;
import static cn.iocoder.yudao.module.srm.enums.status.SrmAuditStatus.*;
import static cn.iocoder.yudao.module.srm.enums.status.SrmExecutionStatus.*;
import static cn.iocoder.yudao.module.srm.enums.status.SrmOffStatus.*;
import static cn.iocoder.yudao.module.srm.enums.status.SrmPaymentStatus.*;
import static cn.iocoder.yudao.module.srm.enums.status.SrmStorageStatus.*;

@Slf4j
@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings({"rawtypes", "unchecked"})
public class SrmPurchaseOrderStatusMachine {


    private final Action<SrmOffStatus, SrmEventEnum, SrmPurchaseOrderDO> orderOffActionImpl;
    private final Action<SrmAuditStatus, SrmEventEnum, SrmPurchaseOrderAuditReqVO> orderAuditActionImpl;
    private final Action<SrmStorageStatus, SrmEventEnum, SrmPurchaseOrderDO> orderInActionImpl;
    private final Action<SrmExecutionStatus, SrmEventEnum, SrmPurchaseOrderDO> orderExecuteActionImpl;
    private final Action<SrmPaymentStatus, SrmEventEnum, SrmPurchaseOrderDO> orderPayActionImpl;
    private final BaseFailCallbackImpl baseFailCallbackImpl;

    //订单主项开关状态机
    @Bean(PURCHASE_ORDER_OFF_STATE_MACHINE_NAME)
    public StateMachine<SrmOffStatus, SrmEventEnum, SrmPurchaseOrderDO> getPurchaseOrderItemStateMachine() {
        StateMachineBuilder<SrmOffStatus, SrmEventEnum, SrmPurchaseOrderDO> builder = StateMachineBuilderFactory.create();
        // 初始化状态
        builder.internalTransition().within(OPEN).on(SrmEventEnum.OFF_INIT).perform(orderOffActionImpl);
        // 开启
        builder.externalTransitions().fromAmong(MANUAL_CLOSED).to(OPEN).on(SrmEventEnum.ACTIVATE).perform(orderOffActionImpl);
        // 手动关闭
        builder.externalTransition().from(OPEN).to(MANUAL_CLOSED).on(SrmEventEnum.MANUAL_CLOSE).perform(orderOffActionImpl);
        //自动关闭
        builder.externalTransition().from(OPEN).to(CLOSED).on(SrmEventEnum.AUTO_CLOSE).perform(orderOffActionImpl);
        //撤销关闭
        builder.externalTransitions().fromAmong(MANUAL_CLOSED, CLOSED, OPEN).to(OPEN).on(SrmEventEnum.CANCEL_DELETE).perform(orderOffActionImpl);
        //错误回调函数
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(PURCHASE_ORDER_OFF_STATE_MACHINE_NAME);
    }

    @Bean(PURCHASE_ORDER_AUDIT_STATE_MACHINE_NAME)
    public StateMachine<SrmAuditStatus, SrmEventEnum, SrmPurchaseOrderAuditReqVO> getPurchaseRequestStateMachine() {
        StateMachineBuilder<SrmAuditStatus, SrmEventEnum, SrmPurchaseOrderAuditReqVO> builder = StateMachineBuilderFactory.create();

        // 初始化状态
        builder.internalTransition().within(DRAFT).on(SrmEventEnum.AUDIT_INIT).perform(orderAuditActionImpl);

        // 提交审核
        builder.externalTransitions().fromAmong(DRAFT, REVOKED, REJECTED).to(PENDING_REVIEW).on(SrmEventEnum.SUBMIT_FOR_REVIEW).perform(orderAuditActionImpl);

        // 审核通过
        builder.externalTransition().from(PENDING_REVIEW).to(APPROVED).on(SrmEventEnum.AGREE).perform(orderAuditActionImpl);

        // 审核不通过
        builder.externalTransition().from(PENDING_REVIEW).to(REJECTED).on(SrmEventEnum.REJECT).perform(orderAuditActionImpl);

        // 反审核
        builder.externalTransition().from(APPROVED).to(REVOKED).on(SrmEventEnum.WITHDRAW_REVIEW).perform(orderAuditActionImpl);
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(PURCHASE_ORDER_AUDIT_STATE_MACHINE_NAME);
    }

    //    入库状态机
    @Bean(PURCHASE_ORDER_STORAGE_STATE_MACHINE_NAME)
    public StateMachine<SrmStorageStatus, SrmEventEnum, SrmPurchaseOrderDO> getPurchaseStorageStateMachine() {
        StateMachineBuilder<SrmStorageStatus, SrmEventEnum, SrmPurchaseOrderDO> builder = StateMachineBuilderFactory.create();
        // 初始化状态
        builder.internalTransition().within(NONE_IN_STORAGE).on(SrmEventEnum.STORAGE_INIT).perform(orderInActionImpl);
        //
        builder.externalTransitions().fromAmong(NONE_IN_STORAGE, PARTIALLY_IN_STORAGE, ALL_IN_STORAGE).to(NONE_IN_STORAGE).on(SrmEventEnum.STOCK_ADJUSTMENT).perform(orderInActionImpl);
        //结束事件

        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(PURCHASE_ORDER_STORAGE_STATE_MACHINE_NAME);
    }

    // 采购订单执行状态机
    @Bean(PURCHASE_ORDER_EXECUTION_STATE_MACHINE_NAME)
    public StateMachine<SrmExecutionStatus, SrmEventEnum, SrmPurchaseOrderDO> getPurchaseOrderExecutionStateMachine() {
        StateMachineBuilder<SrmExecutionStatus, SrmEventEnum, SrmPurchaseOrderDO> builder = StateMachineBuilderFactory.create();

        // 初始化待执行状态
        builder.internalTransition().within(PENDING).on(SrmEventEnum.EXECUTION_INIT).perform(orderExecuteActionImpl);

        // 开始执行
        builder.externalTransition().from(PENDING).to(IN_PROGRESS).on(SrmEventEnum.START_EXECUTION).perform(orderExecuteActionImpl);

        // 执行完成
        builder.externalTransition().from(IN_PROGRESS).to(COMPLETED).on(SrmEventEnum.COMPLETE_EXECUTION).perform(orderExecuteActionImpl);

        // 暂停执行
        builder.externalTransition().from(IN_PROGRESS).to(PAUSED).on(SrmEventEnum.PAUSE_EXECUTION).perform(orderExecuteActionImpl);

        // 恢复执行
        builder.externalTransition().from(PAUSED).to(IN_PROGRESS).on(SrmEventEnum.RESUME_EXECUTION).perform(orderExecuteActionImpl);

        // 取消执行
        builder.externalTransitions().fromAmong(PENDING, IN_PROGRESS, PAUSED).to(CANCELLED).on(SrmEventEnum.CANCEL_EXECUTION).perform(orderExecuteActionImpl);

        // 执行失败
        builder.externalTransition().from(IN_PROGRESS).to(FAILED).on(SrmEventEnum.EXECUTION_FAILED).perform(orderExecuteActionImpl);
        // 执行状态动态调整,不限制起始执行状态
        builder.externalTransitions().fromAmong(PENDING, IN_PROGRESS, PAUSED, COMPLETED, FAILED).to(IN_PROGRESS).on(SrmEventEnum.EXECUTION_ADJUSTMENT).perform(orderExecuteActionImpl);


        // 设置错误回调
        builder.setFailCallback(baseFailCallbackImpl);

        return builder.build(PURCHASE_ORDER_EXECUTION_STATE_MACHINE_NAME);
    }

    @Bean(PURCHASE_ORDER_PAYMENT_STATE_MACHINE_NAME)
    public StateMachine<SrmPaymentStatus, SrmEventEnum, SrmPurchaseOrderDO> getPurchaseOrderPaymentStateMachine() {
        StateMachineBuilder<SrmPaymentStatus, SrmEventEnum, SrmPurchaseOrderDO> builder = StateMachineBuilderFactory.create();

        // 初始化状态
        builder.internalTransition().within(NONE_PAYMENT).on(SrmEventEnum.PAYMENT_INIT).perform(orderPayActionImpl);
        // 付款异常
        builder.externalTransitions().fromAmong(NONE_PAYMENT, PARTIALLY_PAYMENT).to(PAYMENT_EXCEPTION).on(SrmEventEnum.PAYMENT_EXCEPTION).perform(orderPayActionImpl);
        // 付款调整
        builder.externalTransitions().fromAmong(NONE_PAYMENT, PARTIALLY_PAYMENT, ALL_PAYMENT, PAYMENT_EXCEPTION).to(PARTIALLY_PAYMENT).on(SrmEventEnum.PAYMENT_ADJUSTMENT).perform(orderPayActionImpl);
        // 设置错误回调
        builder.setFailCallback(baseFailCallbackImpl);

        return builder.build(PURCHASE_ORDER_PAYMENT_STATE_MACHINE_NAME);
    }

}
