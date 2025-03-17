package cn.iocoder.yudao.module.erp.config.purchase.in;

import cn.iocoder.yudao.module.erp.config.BaseFailCallbackImpl;
import cn.iocoder.yudao.module.erp.config.purchase.in.impl.action.ActionInAuditImpl;
import cn.iocoder.yudao.module.erp.config.purchase.in.impl.action.ActionInPayImpl;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInAuditReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.ErpStateMachines;
import cn.iocoder.yudao.module.erp.enums.status.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.status.ErpPaymentStatus;
import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//采购入库单主表状态机
@Slf4j
@Configuration
@SuppressWarnings({"rawtypes", "unchecked"})
public class ErpPurchaseInStatusMachine {


    @Resource
    private BaseFailCallbackImpl baseFailCallbackImpl;

    @Resource
    ActionInAuditImpl actionInAuditImpl;
    @Bean(ErpStateMachines.PURCHASE_IN_AUDIT_STATE_MACHINE)
    public StateMachine<ErpAuditStatus, ErpEventEnum, ErpPurchaseInAuditReqVO> getPurchaseRequestStateMachine() {
        StateMachineBuilder<ErpAuditStatus, ErpEventEnum, ErpPurchaseInAuditReqVO> builder = StateMachineBuilderFactory.create();

        // 初始化状态
        builder.internalTransition()
            .within(ErpAuditStatus.DRAFT)
            .on(ErpEventEnum.AUDIT_INIT)
            .perform(actionInAuditImpl);

        // 提交审核
        builder.externalTransitions()
            .fromAmong(ErpAuditStatus.DRAFT, ErpAuditStatus.REVOKED, ErpAuditStatus.REJECTED)
            .to(ErpAuditStatus.PENDING_REVIEW)
            .on(ErpEventEnum.SUBMIT_FOR_REVIEW)
            .perform(actionInAuditImpl);

        // 审核通过
        builder.externalTransition()
            .from(ErpAuditStatus.PENDING_REVIEW)
            .to(ErpAuditStatus.APPROVED)
            .on(ErpEventEnum.AGREE)
            .perform(actionInAuditImpl);

        // 审核不通过
        builder.externalTransition()
            .from(ErpAuditStatus.PENDING_REVIEW)
            .to(ErpAuditStatus.REJECTED)
            .on(ErpEventEnum.REJECT)
            .perform(actionInAuditImpl);

        // 反审核
        builder.externalTransition()
            .from(ErpAuditStatus.APPROVED)
            .to(ErpAuditStatus.REVOKED)
            .on(ErpEventEnum.WITHDRAW_REVIEW)
            .perform(actionInAuditImpl);

        builder.setFailCallback(baseFailCallbackImpl);

        return builder.build(ErpStateMachines.PURCHASE_IN_AUDIT_STATE_MACHINE);
    }

    @Resource
    ActionInPayImpl actionInPayImpl;
    @Bean(ErpStateMachines.PURCHASE_IN_PAYMENT_STATE_MACHINE)
    public StateMachine<ErpPaymentStatus, ErpEventEnum, ErpPurchaseInDO> getPurchaseRequestPaymentStateMachine() {
        StateMachineBuilder<ErpPaymentStatus, ErpEventEnum, ErpPurchaseInDO> builder = StateMachineBuilderFactory.create();
        //初始化
        builder.internalTransition()
            .within(ErpPaymentStatus.NONE_PAYMENT)
            .on(ErpEventEnum.PAYMENT_INIT)
            .perform(actionInPayImpl);
        //付款金额调整
        builder.externalTransition()
            .from(ErpPaymentStatus.NONE_PAYMENT)
            .to(ErpPaymentStatus.PARTIALLY_PAYMENT)
            .on(ErpEventEnum.PAYMENT_ADJUSTMENT)
            .perform(actionInPayImpl);
        builder.externalTransition()
            .from(ErpPaymentStatus.ALL_PAYMENT)
            .to(ErpPaymentStatus.ALL_PAYMENT)
            .on(ErpEventEnum.PAYMENT_ADJUSTMENT)
            .perform(actionInPayImpl);
        builder.externalTransition()
            .from(ErpPaymentStatus.PARTIALLY_PAYMENT)
            .to(ErpPaymentStatus.ALL_PAYMENT)
            .on(ErpEventEnum.PAYMENT_ADJUSTMENT)
            .perform(actionInPayImpl);
        //付款失败事件
        builder.externalTransition()
            .from(ErpPaymentStatus.NONE_PAYMENT)
            .to(ErpPaymentStatus.PAYMENT_EXCEPTION)
            .on(ErpEventEnum.PAYMENT_EXCEPTION)
            .perform(actionInPayImpl);
        builder.externalTransition()
            .from(ErpPaymentStatus.PARTIALLY_PAYMENT)
            .to(ErpPaymentStatus.PAYMENT_EXCEPTION)
            .on(ErpEventEnum.PAYMENT_EXCEPTION)
            .perform(actionInPayImpl);
        //取消付款
        builder.externalTransition()
            .from(ErpPaymentStatus.ALL_PAYMENT)
            .to(ErpPaymentStatus.NONE_PAYMENT)
            .on(ErpEventEnum.CANCEL_PAYMENT)
            .perform(actionInPayImpl);


        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(ErpStateMachines.PURCHASE_IN_PAYMENT_STATE_MACHINE);
    }
}
