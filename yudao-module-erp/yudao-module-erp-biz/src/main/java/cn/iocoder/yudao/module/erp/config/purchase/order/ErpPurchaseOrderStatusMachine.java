package cn.iocoder.yudao.module.erp.config.purchase.order;

import cn.iocoder.yudao.module.erp.config.purchase.request.impl.BaseFailCallbackImpl;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.ErpStateMachines;
import cn.iocoder.yudao.module.erp.enums.status.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.status.ErpOffStatus;
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
public class ErpPurchaseOrderStatusMachine {

    @Resource
    private Action<ErpOffStatus, ErpEventEnum, ErpPurchaseOrderDO> actionOrderOffImpl;
    @Resource
    private Action<ErpAuditStatus, ErpEventEnum, ErpPurchaseOrderDO> actionOrderAuditImpl;
    @Resource
    private Action<ErpStorageStatus, ErpEventEnum, ErpPurchaseOrderDO> actionOrderInImpl;
    @Resource
    private BaseFailCallbackImpl baseFailCallbackImpl;

    //订单主项开关状态机
    @Bean(ErpStateMachines.PURCHASE_ORDER_OFF_STATE_MACHINE_NAME)
    public StateMachine<ErpOffStatus, ErpEventEnum, ErpPurchaseOrderDO> getPurchaseOrderStateMachine() {
        StateMachineBuilder<ErpOffStatus, ErpEventEnum, ErpPurchaseOrderDO> builder = StateMachineBuilderFactory.create();
        // 初始化状态
        builder.internalTransition()
            .within(ErpOffStatus.OPEN)
            .on(ErpEventEnum.OFF_INIT)
            .perform(actionOrderOffImpl);
        // 开启
        builder.externalTransitions()
            .fromAmong(ErpOffStatus.MANUAL_CLOSED)
            .to(ErpOffStatus.OPEN)
            .on(ErpEventEnum.ACTIVATE)
            .perform(actionOrderOffImpl);
        // 手动关闭
        builder.externalTransition()
            .from(ErpOffStatus.OPEN)
            .to(ErpOffStatus.MANUAL_CLOSED)
            .on(ErpEventEnum.MANUAL_CLOSE)
            .perform(actionOrderOffImpl);
        //自动关闭
        builder.externalTransition()
            .from(ErpOffStatus.OPEN)
            .to(ErpOffStatus.CLOSED)
            .on(ErpEventEnum.AUTO_CLOSE)
            .perform(actionOrderOffImpl);
        //错误回调函数
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(ErpStateMachines.PURCHASE_ORDER_OFF_STATE_MACHINE_NAME);
    }

    @Bean(ErpStateMachines.PURCHASE_ORDER_AUDIT_STATE_MACHINE_NAME)
    public StateMachine<ErpAuditStatus, ErpEventEnum, ErpPurchaseOrderDO> getPurchaseRequestStateMachine() {
        StateMachineBuilder<ErpAuditStatus, ErpEventEnum, ErpPurchaseOrderDO> builder = StateMachineBuilderFactory.create();

        // 初始化状态
        builder.internalTransition()
            .within(ErpAuditStatus.DRAFT)
            .on(ErpEventEnum.AUDIT_INIT)
            .perform(actionOrderAuditImpl);

        // 提交审核
        builder.externalTransitions()
            .fromAmong(ErpAuditStatus.DRAFT, ErpAuditStatus.REVOKED, ErpAuditStatus.REJECTED)
            .to(ErpAuditStatus.PENDING_REVIEW)
            .on(ErpEventEnum.SUBMIT_FOR_REVIEW)
            .perform(actionOrderAuditImpl);

        // 审核通过
        builder.externalTransition()
            .from(ErpAuditStatus.PENDING_REVIEW)
            .to(ErpAuditStatus.APPROVED)
            .on(ErpEventEnum.AGREE)
            .perform(actionOrderAuditImpl);

        // 审核不通过
        builder.externalTransition()
            .from(ErpAuditStatus.PENDING_REVIEW)
            .to(ErpAuditStatus.REJECTED)
            .on(ErpEventEnum.REJECT)
            .perform(actionOrderAuditImpl);

        // 反审核
        builder.externalTransition()
            .from(ErpAuditStatus.APPROVED)
            .to(ErpAuditStatus.REVOKED)
            .on(ErpEventEnum.WITHDRAW_REVIEW)
            .perform(actionOrderAuditImpl);
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(ErpStateMachines.PURCHASE_ORDER_AUDIT_STATE_MACHINE_NAME);
    }

    //    入库状态机
    @Bean(ErpStateMachines.PURCHASE_ORDER_STORAGE_STATE_MACHINE_NAME)
    public StateMachine<ErpStorageStatus, ErpEventEnum, ErpPurchaseOrderDO> getPurchaseStorageStateMachine() {
        StateMachineBuilder<ErpStorageStatus, ErpEventEnum, ErpPurchaseOrderDO> builder = StateMachineBuilderFactory.create();
        // 初始化状态
        builder.internalTransition()
            .within(ErpStorageStatus.NONE_IN_STORAGE)
            .on(ErpEventEnum.STORAGE_INIT)
            .perform(actionOrderInImpl);
        //
        builder.externalTransitions()
            .fromAmong(ErpStorageStatus.PARTIALLY_IN_STORAGE, ErpStorageStatus.ALL_IN_STORAGE)
            .to(ErpStorageStatus.NONE_IN_STORAGE)
            .on(ErpEventEnum.STOCK_ADJUSTMENT)
            .perform(actionOrderInImpl);
        builder.externalTransitions()
            .fromAmong(ErpStorageStatus.NONE_IN_STORAGE, ErpStorageStatus.ALL_IN_STORAGE)
            .to(ErpStorageStatus.PARTIALLY_IN_STORAGE)
            .on(ErpEventEnum.STOCK_ADJUSTMENT)
            .perform(actionOrderInImpl);
        //内部流转,未入库<->部分入库,完全入库
        builder.externalTransitions()
            .fromAmong(ErpStorageStatus.NONE_IN_STORAGE, ErpStorageStatus.PARTIALLY_IN_STORAGE)
            .to(ErpStorageStatus.ALL_IN_STORAGE)
            .on(ErpEventEnum.STOCK_ADJUSTMENT)
            .perform(actionOrderInImpl);
        //结束事件
        builder.internalTransition()
            .within(ErpStorageStatus.ALL_IN_STORAGE)
            .on(ErpEventEnum.STORAGE_COMPLETE)
            .perform(actionOrderInImpl);


        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(ErpStateMachines.PURCHASE_ORDER_STORAGE_STATE_MACHINE_NAME);
    }

//    //执行状态机
//    @Bean(ErpStateMachines.PURCHASE_ORDER_EXECUTION_STATE_MACHINE_NAME)
//    public StateMachine<ErpExecStatus, ErpEventEnum, ErpPurchaseOrderExecReqVO> getPurchaseExecStateMachine() {
//
//    }
}
