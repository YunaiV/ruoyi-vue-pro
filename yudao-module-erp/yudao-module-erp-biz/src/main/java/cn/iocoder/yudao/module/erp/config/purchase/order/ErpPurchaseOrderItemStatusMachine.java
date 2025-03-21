package cn.iocoder.yudao.module.erp.config.purchase.order;

import cn.iocoder.yudao.module.erp.api.purchase.SrmInCountDTO;
import cn.iocoder.yudao.module.erp.config.BaseFailCallbackImpl;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.ErpStateMachines;
import cn.iocoder.yudao.module.erp.enums.status.ErpExecutionStatus;
import cn.iocoder.yudao.module.erp.enums.status.ErpOffStatus;
import cn.iocoder.yudao.module.erp.enums.status.ErpPaymentStatus;
import cn.iocoder.yudao.module.erp.enums.status.ErpStorageStatus;
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
public class ErpPurchaseOrderItemStatusMachine {


    @Resource
    private BaseFailCallbackImpl baseFailCallbackImpl;

    



    @Resource
    private Action actionOrderItemOffImpl;
    //采购订单子项状态机
    @Bean(ErpStateMachines.PURCHASE_ORDER_ITEM_OFF_STATE_MACHINE_NAME)
    public StateMachine<ErpOffStatus, ErpEventEnum, ErpPurchaseOrderItemDO> getPurchaseOrderItemStateMachine() {
        StateMachineBuilder<ErpOffStatus, ErpEventEnum, ErpPurchaseOrderItemDO> builder = StateMachineBuilderFactory.create();
        // 初始化状态
        builder.internalTransition()
            .within(ErpOffStatus.OPEN)
            .on(ErpEventEnum.OFF_INIT)
            .perform(actionOrderItemOffImpl);
        // 开启
        builder.externalTransitions()
            .fromAmong(ErpOffStatus.MANUAL_CLOSED)
            .to(ErpOffStatus.OPEN)
            .on(ErpEventEnum.ACTIVATE)
            .perform(actionOrderItemOffImpl);
        // 手动关闭
        builder.externalTransition()
            .from(ErpOffStatus.OPEN)
            .to(ErpOffStatus.MANUAL_CLOSED)
            .on(ErpEventEnum.MANUAL_CLOSE)
            .perform(actionOrderItemOffImpl);
        //自动关闭
        builder.externalTransition()
            .from(ErpOffStatus.OPEN)
            .to(ErpOffStatus.CLOSED)
            .on(ErpEventEnum.AUTO_CLOSE)
            .perform(actionOrderItemOffImpl);
        //错误回调函数
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(ErpStateMachines.PURCHASE_ORDER_ITEM_OFF_STATE_MACHINE_NAME);
    }


    @Resource
    private Action actionOrderItemExecuteImpl;
    // 采购订单子项执行状态机
    @Bean(ErpStateMachines.PURCHASE_ORDER_ITEM_EXECUTION_STATE_MACHINE_NAME)
    public StateMachine<ErpExecutionStatus, ErpEventEnum, ErpPurchaseOrderItemDO> getPurchaseOrderItemExecutionStateMachine() {
        StateMachineBuilder<ErpExecutionStatus, ErpEventEnum, ErpPurchaseOrderItemDO> builder = StateMachineBuilderFactory.create();

        // 初始化待执行状态
        builder.internalTransition()
            .within(ErpExecutionStatus.PENDING)
            .on(ErpEventEnum.EXECUTION_INIT)
            .perform(actionOrderItemExecuteImpl);

        // 开始执行
        builder.externalTransition()
            .from(ErpExecutionStatus.PENDING)
            .to(ErpExecutionStatus.IN_PROGRESS)
            .on(ErpEventEnum.START_EXECUTION)
            .perform(actionOrderItemExecuteImpl);

        // 执行完成
        builder.externalTransition()
            .from(ErpExecutionStatus.IN_PROGRESS)
            .to(ErpExecutionStatus.COMPLETED)
            .on(ErpEventEnum.COMPLETE_EXECUTION)
            .perform(actionOrderItemExecuteImpl);

        // 暂停执行
        builder.externalTransition()
            .from(ErpExecutionStatus.IN_PROGRESS)
            .to(ErpExecutionStatus.PAUSED)
            .on(ErpEventEnum.PAUSE_EXECUTION)
            .perform(actionOrderItemExecuteImpl);

        // 恢复执行
        builder.externalTransition()
            .from(ErpExecutionStatus.PAUSED)
            .to(ErpExecutionStatus.IN_PROGRESS)
            .on(ErpEventEnum.RESUME_EXECUTION)
            .perform(actionOrderItemExecuteImpl);

        // 取消执行
        builder.externalTransitions()
            .fromAmong(ErpExecutionStatus.PENDING, ErpExecutionStatus.IN_PROGRESS, ErpExecutionStatus.PAUSED)
            .to(ErpExecutionStatus.CANCELLED)
            .on(ErpEventEnum.CANCEL_EXECUTION)
            .perform(actionOrderItemExecuteImpl);

        // 执行失败
        builder.externalTransition()
            .from(ErpExecutionStatus.IN_PROGRESS)
            .to(ErpExecutionStatus.FAILED)
            .on(ErpEventEnum.EXECUTION_FAILED)
            .perform(actionOrderItemExecuteImpl);

        // 设置错误回调
        builder.setFailCallback(baseFailCallbackImpl);

        return builder.build(ErpStateMachines.PURCHASE_ORDER_ITEM_EXECUTION_STATE_MACHINE_NAME);
    }

    @Resource
    private Action actionOrderItemInImpl;
    @Resource
    private Action actionOrderItemPayImpl;

    @Bean(ErpStateMachines.PURCHASE_ORDER_ITEM_STORAGE_STATE_MACHINE_NAME)
    public StateMachine<ErpStorageStatus, ErpEventEnum, SrmInCountDTO> buildPurchaseOrderItemStorageStateMachine() {
        StateMachineBuilder<ErpStorageStatus, ErpEventEnum, SrmInCountDTO> builder = StateMachineBuilderFactory.create();

        // 初始化入库
        builder.externalTransition()
            .from(ErpStorageStatus.NONE_IN_STORAGE)
            .to(ErpStorageStatus.NONE_IN_STORAGE)
            .on(ErpEventEnum.STORAGE_INIT)
            .perform(actionOrderItemInImpl);

        // 部分入库
//        builder.externalTransition()
//            .from(ErpStorageStatus.NONE_IN_STORAGE)
//            .to(ErpStorageStatus.PARTIALLY_IN_STORAGE)
//            .on(ErpEventEnum.PARTIAL_STORAGE)
//            .perform(actionOrderItemInImpl);
//
//        // 完成入库
//        builder.externalTransitions()
//            .fromAmong(ErpStorageStatus.NONE_IN_STORAGE, ErpStorageStatus.PARTIALLY_IN_STORAGE)
//            .to(ErpStorageStatus.ALL_IN_STORAGE)
//            .on(ErpEventEnum.COMPLETE_STORAGE)
//            .perform(actionOrderItemInImpl);

        // 取消入库
        builder.externalTransitions()
            .fromAmong(ErpStorageStatus.NONE_IN_STORAGE, ErpStorageStatus.PARTIALLY_IN_STORAGE)
            .to(ErpStorageStatus.NONE_IN_STORAGE)
            .on(ErpEventEnum.CANCEL_STORAGE)
            .perform(actionOrderItemInImpl);

//        // 入库异常
//        builder.externalTransitions()
//            .fromAmong(ErpStorageStatus.NONE_IN_STORAGE, ErpStorageStatus.PARTIALLY_IN_STORAGE, ErpStorageStatus.ALL_IN_STORAGE)
//            .to(ErpStorageStatus.NONE_IN_STORAGE)
//            .on(ErpEventEnum.STORAGE_EXCEPTION)
//            .perform(actionOrderItemInImpl);

        // 库存调整
        builder.externalTransitions()
            .fromAmong(ErpStorageStatus.NONE_IN_STORAGE, ErpStorageStatus.PARTIALLY_IN_STORAGE, ErpStorageStatus.ALL_IN_STORAGE)
            .to(ErpStorageStatus.NONE_IN_STORAGE)
            .on(ErpEventEnum.STOCK_ADJUSTMENT)
            .perform(actionOrderItemInImpl);

        // 设置错误回调
        builder.setFailCallback(baseFailCallbackImpl);

        return builder.build(ErpStateMachines.PURCHASE_ORDER_ITEM_STORAGE_STATE_MACHINE_NAME);
    }

    @Bean(ErpStateMachines.PURCHASE_ORDER_ITEM_PAYMENT_STATE_MACHINE_NAME)
    public StateMachine<ErpPaymentStatus, ErpEventEnum, ErpPurchaseOrderItemDO> getPurchaseOrderItemPaymentStateMachine() {
        StateMachineBuilder<ErpPaymentStatus, ErpEventEnum, ErpPurchaseOrderItemDO> builder = StateMachineBuilderFactory.create();

        // 初始化付款状态
        builder.internalTransition()
            .within(ErpPaymentStatus.NONE_PAYMENT)
            .on(ErpEventEnum.PAYMENT_INIT)
            .perform(actionOrderItemPayImpl);

//        // 部分付款
//        builder.externalTransition()
//            .from(ErpPaymentStatus.NONE_PAYMENT)
//            .to(ErpPaymentStatus.PARTIALLY_PAYMENT)
//            .on(ErpEventEnum.PARTIAL_PAYMENT)
//            .perform(actionOrderItemPayImpl);
//
//        // 完成付款
//        builder.externalTransitions()
//            .fromAmong(ErpPaymentStatus.NONE_PAYMENT, ErpPaymentStatus.PARTIALLY_PAYMENT)
//            .to(ErpPaymentStatus.ALL_PAYMENT)
//            .on(ErpEventEnum.COMPLETE_PAYMENT)
//            .perform(actionOrderItemPayImpl);

        // 取消付款
        builder.externalTransitions()
            .fromAmong(ErpPaymentStatus.NONE_PAYMENT, ErpPaymentStatus.PARTIALLY_PAYMENT)
            .to(ErpPaymentStatus.NONE_PAYMENT)
            .on(ErpEventEnum.CANCEL_PAYMENT)
            .perform(actionOrderItemPayImpl);

        // 付款异常
        builder.externalTransitions()
            .fromAmong(ErpPaymentStatus.NONE_PAYMENT, ErpPaymentStatus.PARTIALLY_PAYMENT, ErpPaymentStatus.ALL_PAYMENT)
            .to(ErpPaymentStatus.NONE_PAYMENT)
            .on(ErpEventEnum.PAYMENT_EXCEPTION)
            .perform(actionOrderItemPayImpl);

        // 付款调整
        builder.externalTransitions()
            .fromAmong(ErpPaymentStatus.NONE_PAYMENT, ErpPaymentStatus.PARTIALLY_PAYMENT, ErpPaymentStatus.ALL_PAYMENT)
            .to(ErpPaymentStatus.NONE_PAYMENT)
            .on(ErpEventEnum.PAYMENT_ADJUSTMENT)
            .perform(actionOrderItemPayImpl);

        // 设置错误回调
        builder.setFailCallback(baseFailCallbackImpl);

        return builder.build(ErpStateMachines.PURCHASE_ORDER_ITEM_PAYMENT_STATE_MACHINE_NAME);
    }

}
