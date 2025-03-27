package cn.iocoder.yudao.module.srm.config.purchase.in;

import cn.iocoder.yudao.module.srm.config.BaseFailCallbackImpl;
import cn.iocoder.yudao.module.srm.config.purchase.in.impl.action.ActionInAuditImpl;
import cn.iocoder.yudao.module.srm.config.purchase.in.impl.action.ActionInPayImpl;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.ErpPurchaseInAuditReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.SrmStateMachines;
import cn.iocoder.yudao.module.srm.enums.status.ErpPaymentStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmAuditStatus;
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

    @Bean(SrmStateMachines.PURCHASE_IN_AUDIT_STATE_MACHINE)
    public StateMachine<SrmAuditStatus, SrmEventEnum, ErpPurchaseInAuditReqVO> getPurchaseRequestStateMachine() {
        StateMachineBuilder<SrmAuditStatus, SrmEventEnum, ErpPurchaseInAuditReqVO> builder = StateMachineBuilderFactory.create();

        // 初始化状态
        builder.internalTransition()
            .within(SrmAuditStatus.DRAFT)
            .on(SrmEventEnum.AUDIT_INIT)
            .perform(actionInAuditImpl);

        // 提交审核
        builder.externalTransitions()
            .fromAmong(SrmAuditStatus.DRAFT, SrmAuditStatus.REVOKED, SrmAuditStatus.REJECTED)
            .to(SrmAuditStatus.PENDING_REVIEW)
            .on(SrmEventEnum.SUBMIT_FOR_REVIEW)
            .perform(actionInAuditImpl);

        // 审核通过
        builder.externalTransition()
            .from(SrmAuditStatus.PENDING_REVIEW)
            .to(SrmAuditStatus.APPROVED)
            .on(SrmEventEnum.AGREE)
            .perform(actionInAuditImpl);

        // 审核不通过
        builder.externalTransition()
            .from(SrmAuditStatus.PENDING_REVIEW)
            .to(SrmAuditStatus.REJECTED)
            .on(SrmEventEnum.REJECT)
            .perform(actionInAuditImpl);

        // 反审核
        builder.externalTransition()
            .from(SrmAuditStatus.APPROVED)
            .to(SrmAuditStatus.REVOKED)
            .on(SrmEventEnum.WITHDRAW_REVIEW)
            .perform(actionInAuditImpl);

        builder.setFailCallback(baseFailCallbackImpl);

        return builder.build(SrmStateMachines.PURCHASE_IN_AUDIT_STATE_MACHINE);
    }

    @Resource
    ActionInPayImpl actionInPayImpl;

    @Bean(SrmStateMachines.PURCHASE_IN_PAYMENT_STATE_MACHINE)
    public StateMachine<ErpPaymentStatus, SrmEventEnum, ErpPurchaseInDO> getPurchaseRequestPaymentStateMachine() {
        StateMachineBuilder<ErpPaymentStatus, SrmEventEnum, ErpPurchaseInDO> builder = StateMachineBuilderFactory.create();
        //初始化
        builder.internalTransition()
            .within(ErpPaymentStatus.NONE_PAYMENT)
            .on(SrmEventEnum.PAYMENT_INIT)
            .perform(actionInPayImpl);
        //付款金额调整
        builder.externalTransition()
            .from(ErpPaymentStatus.NONE_PAYMENT)
            .to(ErpPaymentStatus.PARTIALLY_PAYMENT)
            .on(SrmEventEnum.PAYMENT_ADJUSTMENT)
            .perform(actionInPayImpl);
        builder.externalTransition()
            .from(ErpPaymentStatus.ALL_PAYMENT)
            .to(ErpPaymentStatus.ALL_PAYMENT)
            .on(SrmEventEnum.PAYMENT_ADJUSTMENT)
            .perform(actionInPayImpl);
        builder.externalTransition()
            .from(ErpPaymentStatus.PARTIALLY_PAYMENT)
            .to(ErpPaymentStatus.ALL_PAYMENT)
            .on(SrmEventEnum.PAYMENT_ADJUSTMENT)
            .perform(actionInPayImpl);
        //付款失败事件
        builder.externalTransition()
            .from(ErpPaymentStatus.NONE_PAYMENT)
            .to(ErpPaymentStatus.PAYMENT_EXCEPTION)
            .on(SrmEventEnum.PAYMENT_EXCEPTION)
            .perform(actionInPayImpl);
        builder.externalTransition()
            .from(ErpPaymentStatus.PARTIALLY_PAYMENT)
            .to(ErpPaymentStatus.PAYMENT_EXCEPTION)
            .on(SrmEventEnum.PAYMENT_EXCEPTION)
            .perform(actionInPayImpl);
        //取消付款
        builder.externalTransition()
            .from(ErpPaymentStatus.ALL_PAYMENT)
            .to(ErpPaymentStatus.NONE_PAYMENT)
            .on(SrmEventEnum.CANCEL_PAYMENT)
            .perform(actionInPayImpl);


        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(SrmStateMachines.PURCHASE_IN_PAYMENT_STATE_MACHINE);
    }
}
