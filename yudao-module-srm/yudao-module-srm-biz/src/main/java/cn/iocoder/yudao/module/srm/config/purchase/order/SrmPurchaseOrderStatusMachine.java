package cn.iocoder.yudao.module.srm.config.purchase.order;

import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.req.SrmPurchaseOrderAuditReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.*;
import com.alibaba.cola.statemachine.Action;
import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.builder.FailCallback;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.*;

@Slf4j
@Configuration
@SuppressWarnings({"rawtypes", "unchecked"})
public class SrmPurchaseOrderStatusMachine {

    @Resource
    private Action<SrmOffStatus, SrmEventEnum, SrmPurchaseOrderDO> orderOffActionImpl;
    @Resource
    private Action<SrmAuditStatus, SrmEventEnum, SrmPurchaseOrderAuditReqVO> orderAuditActionImpl;
    @Resource
    private Action<SrmStorageStatus, SrmEventEnum, SrmPurchaseOrderDO> orderInActionImpl;
    @Resource
    private Action<SrmExecutionStatus, SrmEventEnum, SrmPurchaseOrderDO> orderExecuteActionImpl;
    @Resource
    private Action<SrmPaymentStatus, SrmEventEnum, SrmPurchaseOrderDO> orderPayActionImpl;
    @Resource
    private FailCallback baseFailCallbackImpl;

    //订单主项开关状态机
    @Bean(PURCHASE_ORDER_OFF_STATE_MACHINE_NAME)
    public StateMachine<SrmOffStatus, SrmEventEnum, SrmPurchaseOrderDO> getPurchaseOrderStateMachine() {
        StateMachineBuilder<SrmOffStatus, SrmEventEnum, SrmPurchaseOrderDO> builder =
            StateMachineBuilderFactory.create();
        // 初始化状态
        builder.internalTransition().within(SrmOffStatus.OPEN).on(SrmEventEnum.OFF_INIT).perform(orderOffActionImpl);
        // 开启
        builder.externalTransitions().fromAmong(SrmOffStatus.MANUAL_CLOSED).to(SrmOffStatus.OPEN)
            .on(SrmEventEnum.ACTIVATE).perform(orderOffActionImpl);
        // 手动关闭
        builder.externalTransition().from(SrmOffStatus.OPEN).to(SrmOffStatus.MANUAL_CLOSED)
            .on(SrmEventEnum.MANUAL_CLOSE).perform(orderOffActionImpl);
        //自动关闭
        builder.externalTransition().from(SrmOffStatus.OPEN).to(SrmOffStatus.CLOSED).on(SrmEventEnum.AUTO_CLOSE)
            .perform(orderOffActionImpl);
        //撤销关闭
        builder.externalTransitions().fromAmong(SrmOffStatus.MANUAL_CLOSED, SrmOffStatus.CLOSED, SrmOffStatus.OPEN)
            .to(SrmOffStatus.OPEN).on(SrmEventEnum.CANCEL_DELETE).perform(orderOffActionImpl);
        //错误回调函数
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(PURCHASE_ORDER_OFF_STATE_MACHINE_NAME);
    }

    @Bean(PURCHASE_ORDER_AUDIT_STATE_MACHINE_NAME)
    public StateMachine<SrmAuditStatus, SrmEventEnum, SrmPurchaseOrderAuditReqVO> getPurchaseRequestStateMachine() {
        StateMachineBuilder<SrmAuditStatus, SrmEventEnum, SrmPurchaseOrderAuditReqVO> builder =
            StateMachineBuilderFactory.create();

        // 初始化状态
        builder.internalTransition().within(SrmAuditStatus.DRAFT).on(SrmEventEnum.AUDIT_INIT)
            .perform(orderAuditActionImpl);

        // 提交审核
        builder.externalTransitions().fromAmong(SrmAuditStatus.DRAFT, SrmAuditStatus.REVOKED, SrmAuditStatus.REJECTED)
            .to(SrmAuditStatus.PENDING_REVIEW).on(SrmEventEnum.SUBMIT_FOR_REVIEW).perform(orderAuditActionImpl);

        // 审核通过
        builder.externalTransition().from(SrmAuditStatus.PENDING_REVIEW).to(SrmAuditStatus.APPROVED)
            .on(SrmEventEnum.AGREE).perform(orderAuditActionImpl);

        // 审核不通过
        builder.externalTransition().from(SrmAuditStatus.PENDING_REVIEW).to(SrmAuditStatus.REJECTED)
            .on(SrmEventEnum.REJECT).perform(orderAuditActionImpl);

        // 反审核
        builder.externalTransition().from(SrmAuditStatus.APPROVED).to(SrmAuditStatus.REVOKED)
            .on(SrmEventEnum.WITHDRAW_REVIEW).perform(orderAuditActionImpl);
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(PURCHASE_ORDER_AUDIT_STATE_MACHINE_NAME);
    }

    //    入库状态机
    @Bean(PURCHASE_ORDER_STORAGE_STATE_MACHINE_NAME)
    public StateMachine<SrmStorageStatus, SrmEventEnum, SrmPurchaseOrderDO> getPurchaseStorageStateMachine() {
        StateMachineBuilder<SrmStorageStatus, SrmEventEnum, SrmPurchaseOrderDO> builder =
            StateMachineBuilderFactory.create();
        // 初始化状态
        builder.internalTransition().within(SrmStorageStatus.NONE_IN_STORAGE).on(SrmEventEnum.STORAGE_INIT)
            .perform(orderInActionImpl);
        //
        builder.externalTransitions().fromAmong(SrmStorageStatus.PARTIALLY_IN_STORAGE, SrmStorageStatus.ALL_IN_STORAGE)
            .to(SrmStorageStatus.NONE_IN_STORAGE).on(SrmEventEnum.STOCK_ADJUSTMENT).perform(orderInActionImpl);
        builder.externalTransitions().fromAmong(SrmStorageStatus.NONE_IN_STORAGE, SrmStorageStatus.ALL_IN_STORAGE)
            .to(SrmStorageStatus.PARTIALLY_IN_STORAGE).on(SrmEventEnum.STOCK_ADJUSTMENT).perform(orderInActionImpl);
        //内部流转,未入库<->部分入库,完全入库
        builder.externalTransitions().fromAmong(SrmStorageStatus.NONE_IN_STORAGE, SrmStorageStatus.PARTIALLY_IN_STORAGE)
            .to(SrmStorageStatus.ALL_IN_STORAGE).on(SrmEventEnum.STOCK_ADJUSTMENT).perform(orderInActionImpl);
        //结束事件
        //        builder.internalTransition()
        //            .within(SrmStorageStatus.ALL_IN_STORAGE)
        //            .on(SrmEventEnum.COMPLETE_STORAGE)
        //            .perform(actionOrderInImpl);

        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(PURCHASE_ORDER_STORAGE_STATE_MACHINE_NAME);
    }

    // 采购订单执行状态机
    @Bean(PURCHASE_ORDER_EXECUTION_STATE_MACHINE_NAME)
    public StateMachine<SrmExecutionStatus, SrmEventEnum, SrmPurchaseOrderDO> getPurchaseOrderExecutionStateMachine() {
        StateMachineBuilder<SrmExecutionStatus, SrmEventEnum, SrmPurchaseOrderDO> builder =
            StateMachineBuilderFactory.create();

        // 初始化待执行状态
        builder.internalTransition().within(SrmExecutionStatus.PENDING).on(SrmEventEnum.EXECUTION_INIT)
            .perform(orderExecuteActionImpl);

        // 开始执行
        builder.externalTransition().from(SrmExecutionStatus.PENDING).to(SrmExecutionStatus.IN_PROGRESS)
            .on(SrmEventEnum.START_EXECUTION).perform(orderExecuteActionImpl);

        // 执行完成
        builder.externalTransition().from(SrmExecutionStatus.IN_PROGRESS).to(SrmExecutionStatus.COMPLETED)
            .on(SrmEventEnum.COMPLETE_EXECUTION).perform(orderExecuteActionImpl);

        // 暂停执行
        builder.externalTransition().from(SrmExecutionStatus.IN_PROGRESS).to(SrmExecutionStatus.PAUSED)
            .on(SrmEventEnum.PAUSE_EXECUTION).perform(orderExecuteActionImpl);

        // 恢复执行
        builder.externalTransition().from(SrmExecutionStatus.PAUSED).to(SrmExecutionStatus.IN_PROGRESS)
            .on(SrmEventEnum.RESUME_EXECUTION).perform(orderExecuteActionImpl);

        // 取消执行
        builder.externalTransitions()
            .fromAmong(SrmExecutionStatus.PENDING, SrmExecutionStatus.IN_PROGRESS, SrmExecutionStatus.PAUSED)
            .to(SrmExecutionStatus.CANCELLED).on(SrmEventEnum.CANCEL_EXECUTION).perform(orderExecuteActionImpl);

        // 执行失败
        builder.externalTransition().from(SrmExecutionStatus.IN_PROGRESS).to(SrmExecutionStatus.FAILED)
            .on(SrmEventEnum.EXECUTION_FAILED).perform(orderExecuteActionImpl);

        // 设置错误回调
        builder.setFailCallback(baseFailCallbackImpl);

        return builder.build(PURCHASE_ORDER_EXECUTION_STATE_MACHINE_NAME);
    }

    @Bean(PURCHASE_ORDER_PAYMENT_STATE_MACHINE_NAME)
    public StateMachine<SrmPaymentStatus, SrmEventEnum, SrmPurchaseOrderDO> getPurchaseOrderPaymentStateMachine() {
        StateMachineBuilder<SrmPaymentStatus, SrmEventEnum, SrmPurchaseOrderDO> builder =
            StateMachineBuilderFactory.create();

        // 初始化状态
        builder.internalTransition().within(SrmPaymentStatus.NONE_PAYMENT).on(SrmEventEnum.PAYMENT_INIT)
            .perform(orderPayActionImpl);

        //        // 部分付款
        //        builder.externalTransition()
        //            .from(SrmPaymentStatus.NONE_PAYMENT)
        //            .to(SrmPaymentStatus.PARTIALLY_PAYMENT)
        //            .on(SrmEventEnum.PARTIAL_PAYMENT)
        //            .perform(actionOrderPaymentImpl);
        //
        //        // 完成付款
        //        builder.externalTransitions()
        //            .fromAmong(SrmPaymentStatus.NONE_PAYMENT, SrmPaymentStatus.PARTIALLY_PAYMENT)
        //            .to(SrmPaymentStatus.ALL_PAYMENT)
        //            .on(SrmEventEnum.COMPLETE_PAYMENT)
        //            .perform(actionOrderPaymentImpl);

        // 付款异常
        builder.externalTransitions().fromAmong(SrmPaymentStatus.NONE_PAYMENT, SrmPaymentStatus.PARTIALLY_PAYMENT)
            .to(SrmPaymentStatus.PAYMENT_EXCEPTION).on(SrmEventEnum.PAYMENT_EXCEPTION).perform(orderPayActionImpl);

        // 付款调整
        builder.externalTransitions().fromAmong(SrmPaymentStatus.PARTIALLY_PAYMENT, SrmPaymentStatus.ALL_PAYMENT,
                SrmPaymentStatus.PAYMENT_EXCEPTION).to(SrmPaymentStatus.PARTIALLY_PAYMENT)
            .on(SrmEventEnum.PAYMENT_ADJUSTMENT).perform(orderPayActionImpl);

        //        // 取消付款
        //        builder.externalTransitions()
        //            .fromAmong(SrmPaymentStatus.ALL_PAYMENT, SrmPaymentStatus.ALL_PAYMENT)
        //            .to(SrmPaymentStatus.NONE_PAYMENT)
        //            .on(SrmEventEnum.CANCEL_PAYMENT)
        //            .perform(actionOrderPaymentImpl);

        // 设置错误回调
        builder.setFailCallback(baseFailCallbackImpl);

        return builder.build(PURCHASE_ORDER_PAYMENT_STATE_MACHINE_NAME);
    }

}
