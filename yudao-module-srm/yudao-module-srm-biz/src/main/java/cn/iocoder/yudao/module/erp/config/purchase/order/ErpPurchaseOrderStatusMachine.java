package cn.iocoder.yudao.module.erp.config.purchase.order;

import cn.iocoder.yudao.module.erp.config.BaseFailCallbackImpl;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderAuditReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.enums.SrmEventEnum;
import cn.iocoder.yudao.module.erp.enums.SrmStateMachines;
import cn.iocoder.yudao.module.erp.enums.status.*;
import com.alibaba.cola.statemachine.Action;
import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@SuppressWarnings({"rawtypes", "unchecked"})
public class ErpPurchaseOrderStatusMachine {

    @Resource
    private Action<ErpOffStatus, SrmEventEnum, ErpPurchaseOrderDO> actionOrderOffImpl;
    @Resource
    private Action<SrmAuditStatus, SrmEventEnum, ErpPurchaseOrderAuditReqVO> actionOrderAuditImpl;
    @Resource
    private Action<ErpStorageStatus, SrmEventEnum, ErpPurchaseOrderDO> actionOrderInImpl;
    @Resource
    private BaseFailCallbackImpl baseFailCallbackImpl;
    @Resource
    private Action<ErpExecutionStatus, SrmEventEnum, ErpPurchaseOrderDO> actionOrderExecuteImpl;
    @Resource
    private Action<ErpPaymentStatus, SrmEventEnum, ErpPurchaseOrderDO> actionOrderPaymentImpl;

    //订单主项开关状态机
    @Bean(SrmStateMachines.PURCHASE_ORDER_OFF_STATE_MACHINE_NAME)
    public StateMachine<ErpOffStatus, SrmEventEnum, ErpPurchaseOrderDO> getPurchaseOrderStateMachine() {
        StateMachineBuilder<ErpOffStatus, SrmEventEnum, ErpPurchaseOrderDO> builder = StateMachineBuilderFactory.create();
        // 初始化状态
        builder.internalTransition()
            .within(ErpOffStatus.OPEN)
            .on(SrmEventEnum.OFF_INIT)
            .perform(actionOrderOffImpl);
        // 开启
        builder.externalTransitions()
            .fromAmong(ErpOffStatus.MANUAL_CLOSED)
            .to(ErpOffStatus.OPEN)
            .on(SrmEventEnum.ACTIVATE)
            .perform(actionOrderOffImpl);
        // 手动关闭
        builder.externalTransition()
            .from(ErpOffStatus.OPEN)
            .to(ErpOffStatus.MANUAL_CLOSED)
            .on(SrmEventEnum.MANUAL_CLOSE)
            .perform(actionOrderOffImpl);
        //自动关闭
        builder.externalTransition()
            .from(ErpOffStatus.OPEN)
            .to(ErpOffStatus.CLOSED)
            .on(SrmEventEnum.AUTO_CLOSE)
            .perform(actionOrderOffImpl);
        //错误回调函数
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(SrmStateMachines.PURCHASE_ORDER_OFF_STATE_MACHINE_NAME);
    }

    @Bean(SrmStateMachines.PURCHASE_ORDER_AUDIT_STATE_MACHINE_NAME)
    public StateMachine<SrmAuditStatus, SrmEventEnum, ErpPurchaseOrderAuditReqVO> getPurchaseRequestStateMachine() {
        StateMachineBuilder<SrmAuditStatus, SrmEventEnum, ErpPurchaseOrderAuditReqVO> builder = StateMachineBuilderFactory.create();

        // 初始化状态
        builder.internalTransition()
            .within(SrmAuditStatus.DRAFT)
            .on(SrmEventEnum.AUDIT_INIT)
            .perform(actionOrderAuditImpl);

        // 提交审核
        builder.externalTransitions()
            .fromAmong(SrmAuditStatus.DRAFT, SrmAuditStatus.REVOKED, SrmAuditStatus.REJECTED)
            .to(SrmAuditStatus.PENDING_REVIEW)
            .on(SrmEventEnum.SUBMIT_FOR_REVIEW)
            .perform(actionOrderAuditImpl);

        // 审核通过
        builder.externalTransition()
            .from(SrmAuditStatus.PENDING_REVIEW)
            .to(SrmAuditStatus.APPROVED)
            .on(SrmEventEnum.AGREE)
            .perform(actionOrderAuditImpl);

        // 审核不通过
        builder.externalTransition()
            .from(SrmAuditStatus.PENDING_REVIEW)
            .to(SrmAuditStatus.REJECTED)
            .on(SrmEventEnum.REJECT)
            .perform(actionOrderAuditImpl);

        // 反审核
        builder.externalTransition()
            .from(SrmAuditStatus.APPROVED)
            .to(SrmAuditStatus.REVOKED)
            .on(SrmEventEnum.WITHDRAW_REVIEW)
            .perform(actionOrderAuditImpl);
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(SrmStateMachines.PURCHASE_ORDER_AUDIT_STATE_MACHINE_NAME);
    }

    //    入库状态机
    @Bean(SrmStateMachines.PURCHASE_ORDER_STORAGE_STATE_MACHINE_NAME)
    public StateMachine<ErpStorageStatus, SrmEventEnum, ErpPurchaseOrderDO> getPurchaseStorageStateMachine() {
        StateMachineBuilder<ErpStorageStatus, SrmEventEnum, ErpPurchaseOrderDO> builder = StateMachineBuilderFactory.create();
        // 初始化状态
        builder.internalTransition()
            .within(ErpStorageStatus.NONE_IN_STORAGE)
            .on(SrmEventEnum.STORAGE_INIT)
            .perform(actionOrderInImpl);
        //
        builder.externalTransitions()
            .fromAmong(ErpStorageStatus.PARTIALLY_IN_STORAGE, ErpStorageStatus.ALL_IN_STORAGE)
            .to(ErpStorageStatus.NONE_IN_STORAGE)
            .on(SrmEventEnum.STOCK_ADJUSTMENT)
            .perform(actionOrderInImpl);
        builder.externalTransitions()
            .fromAmong(ErpStorageStatus.NONE_IN_STORAGE, ErpStorageStatus.ALL_IN_STORAGE)
            .to(ErpStorageStatus.PARTIALLY_IN_STORAGE)
            .on(SrmEventEnum.STOCK_ADJUSTMENT)
            .perform(actionOrderInImpl);
        //内部流转,未入库<->部分入库,完全入库
        builder.externalTransitions()
            .fromAmong(ErpStorageStatus.NONE_IN_STORAGE, ErpStorageStatus.PARTIALLY_IN_STORAGE)
            .to(ErpStorageStatus.ALL_IN_STORAGE)
            .on(SrmEventEnum.STOCK_ADJUSTMENT)
            .perform(actionOrderInImpl);
        //结束事件
//        builder.internalTransition()
//            .within(ErpStorageStatus.ALL_IN_STORAGE)
//            .on(SrmEventEnum.COMPLETE_STORAGE)
//            .perform(actionOrderInImpl);


        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(SrmStateMachines.PURCHASE_ORDER_STORAGE_STATE_MACHINE_NAME);
    }

    // 采购订单执行状态机
    @Bean(SrmStateMachines.PURCHASE_ORDER_EXECUTION_STATE_MACHINE_NAME)
    public StateMachine<ErpExecutionStatus, SrmEventEnum, ErpPurchaseOrderDO> getPurchaseOrderExecutionStateMachine() {
        StateMachineBuilder<ErpExecutionStatus, SrmEventEnum, ErpPurchaseOrderDO> builder = StateMachineBuilderFactory.create();

        // 初始化待执行状态
        builder.internalTransition()
            .within(ErpExecutionStatus.PENDING)
            .on(SrmEventEnum.EXECUTION_INIT)
            .perform(actionOrderExecuteImpl);

        // 开始执行
        builder.externalTransition()
            .from(ErpExecutionStatus.PENDING)
            .to(ErpExecutionStatus.IN_PROGRESS)
            .on(SrmEventEnum.START_EXECUTION)
            .perform(actionOrderExecuteImpl);

        // 执行完成
        builder.externalTransition()
            .from(ErpExecutionStatus.IN_PROGRESS)
            .to(ErpExecutionStatus.COMPLETED)
            .on(SrmEventEnum.COMPLETE_EXECUTION)
            .perform(actionOrderExecuteImpl);

        // 暂停执行
        builder.externalTransition()
            .from(ErpExecutionStatus.IN_PROGRESS)
            .to(ErpExecutionStatus.PAUSED)
            .on(SrmEventEnum.PAUSE_EXECUTION)
            .perform(actionOrderExecuteImpl);

        // 恢复执行
        builder.externalTransition()
            .from(ErpExecutionStatus.PAUSED)
            .to(ErpExecutionStatus.IN_PROGRESS)
            .on(SrmEventEnum.RESUME_EXECUTION)
            .perform(actionOrderExecuteImpl);

        // 取消执行
        builder.externalTransitions()
            .fromAmong(ErpExecutionStatus.PENDING, ErpExecutionStatus.IN_PROGRESS, ErpExecutionStatus.PAUSED)
            .to(ErpExecutionStatus.CANCELLED)
            .on(SrmEventEnum.CANCEL_EXECUTION)
            .perform(actionOrderExecuteImpl);

        // 执行失败
        builder.externalTransition()
            .from(ErpExecutionStatus.IN_PROGRESS)
            .to(ErpExecutionStatus.FAILED)
            .on(SrmEventEnum.EXECUTION_FAILED)
            .perform(actionOrderExecuteImpl);

        // 设置错误回调
        builder.setFailCallback(baseFailCallbackImpl);

        return builder.build(SrmStateMachines.PURCHASE_ORDER_EXECUTION_STATE_MACHINE_NAME);
    }

    @Bean(SrmStateMachines.PURCHASE_ORDER_PAYMENT_STATE_MACHINE_NAME)
    public StateMachine<ErpPaymentStatus, SrmEventEnum, ErpPurchaseOrderDO> getPurchaseOrderPaymentStateMachine() {
        StateMachineBuilder<ErpPaymentStatus, SrmEventEnum, ErpPurchaseOrderDO> builder = StateMachineBuilderFactory.create();

        // 初始化状态
        builder.internalTransition()
            .within(ErpPaymentStatus.NONE_PAYMENT)
            .on(SrmEventEnum.PAYMENT_INIT)
            .perform(actionOrderPaymentImpl);

//        // 部分付款
//        builder.externalTransition()
//            .from(ErpPaymentStatus.NONE_PAYMENT)
//            .to(ErpPaymentStatus.PARTIALLY_PAYMENT)
//            .on(SrmEventEnum.PARTIAL_PAYMENT)
//            .perform(actionOrderPaymentImpl);
//
//        // 完成付款
//        builder.externalTransitions()
//            .fromAmong(ErpPaymentStatus.NONE_PAYMENT, ErpPaymentStatus.PARTIALLY_PAYMENT)
//            .to(ErpPaymentStatus.ALL_PAYMENT)
//            .on(SrmEventEnum.COMPLETE_PAYMENT)
//            .perform(actionOrderPaymentImpl);

        // 付款异常
        builder.externalTransitions()
            .fromAmong(ErpPaymentStatus.NONE_PAYMENT, ErpPaymentStatus.PARTIALLY_PAYMENT)
            .to(ErpPaymentStatus.PAYMENT_EXCEPTION)
            .on(SrmEventEnum.PAYMENT_EXCEPTION)
            .perform(actionOrderPaymentImpl);

        // 付款调整
        builder.externalTransitions()
            .fromAmong(ErpPaymentStatus.PARTIALLY_PAYMENT, ErpPaymentStatus.ALL_PAYMENT, ErpPaymentStatus.PAYMENT_EXCEPTION)
            .to(ErpPaymentStatus.PARTIALLY_PAYMENT)
            .on(SrmEventEnum.PAYMENT_ADJUSTMENT)
            .perform(actionOrderPaymentImpl);

//        // 取消付款
//        builder.externalTransitions()
//            .fromAmong(ErpPaymentStatus.ALL_PAYMENT, ErpPaymentStatus.ALL_PAYMENT)
//            .to(ErpPaymentStatus.NONE_PAYMENT)
//            .on(SrmEventEnum.CANCEL_PAYMENT)
//            .perform(actionOrderPaymentImpl);

        // 设置错误回调
        builder.setFailCallback(baseFailCallbackImpl);

        return builder.build(SrmStateMachines.PURCHASE_ORDER_PAYMENT_STATE_MACHINE_NAME);
    }

}
