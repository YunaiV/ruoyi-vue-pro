package cn.iocoder.yudao.module.srm.config.purchase.in;

import cn.iocoder.yudao.module.srm.config.purchase.in.impl.action.InAuditActionImpl;
import cn.iocoder.yudao.module.srm.config.purchase.in.impl.action.InPayActionImpl;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.req.SrmPurchaseInAuditReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInDO;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.SrmStateMachines;
import cn.iocoder.yudao.module.srm.enums.status.SrmAuditStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmPaymentStatus;
import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.builder.FailCallback;
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
public class SrmPurchaseInStatusMachine {

    @Resource
    InAuditActionImpl inAuditActionImpl;
    @Resource
    InPayActionImpl inPayActionImpl;
    @Resource
    private FailCallback baseFailCallbackImpl;

    @Bean(SrmStateMachines.PURCHASE_IN_AUDIT_STATE_MACHINE)
    public StateMachine<SrmAuditStatus, SrmEventEnum, SrmPurchaseInAuditReqVO> getPurchaseRequestStateMachine() {
        StateMachineBuilder<SrmAuditStatus, SrmEventEnum, SrmPurchaseInAuditReqVO> builder = StateMachineBuilderFactory.create();

        // 初始化状态
        builder.internalTransition().within(SrmAuditStatus.DRAFT).on(SrmEventEnum.AUDIT_INIT).perform(inAuditActionImpl);

        // 提交审核
        builder.externalTransitions().fromAmong(SrmAuditStatus.DRAFT, SrmAuditStatus.REVOKED, SrmAuditStatus.REJECTED).to(SrmAuditStatus.PENDING_REVIEW)
            .on(SrmEventEnum.SUBMIT_FOR_REVIEW).perform(inAuditActionImpl);

        // 审核通过
        builder.externalTransition().from(SrmAuditStatus.PENDING_REVIEW).to(SrmAuditStatus.APPROVED).on(SrmEventEnum.AGREE).perform(inAuditActionImpl);

        // 审核不通过
        builder.externalTransition().from(SrmAuditStatus.PENDING_REVIEW).to(SrmAuditStatus.REJECTED).on(SrmEventEnum.REJECT).perform(inAuditActionImpl);

        // 反审核
        builder.externalTransition().from(SrmAuditStatus.APPROVED).to(SrmAuditStatus.REVOKED).on(SrmEventEnum.WITHDRAW_REVIEW).perform(inAuditActionImpl);

        builder.setFailCallback(baseFailCallbackImpl);

        return builder.build(SrmStateMachines.PURCHASE_IN_AUDIT_STATE_MACHINE);
    }

    @Bean(SrmStateMachines.PURCHASE_IN_PAYMENT_STATE_MACHINE)
    public StateMachine<SrmPaymentStatus, SrmEventEnum, SrmPurchaseInDO> getPurchaseRequestPaymentStateMachine() {
        StateMachineBuilder<SrmPaymentStatus, SrmEventEnum, SrmPurchaseInDO> builder = StateMachineBuilderFactory.create();
        //初始化
        builder.internalTransition().within(SrmPaymentStatus.NONE_PAYMENT).on(SrmEventEnum.PAYMENT_INIT).perform(inPayActionImpl);
        //付款金额调整
        builder.externalTransition().from(SrmPaymentStatus.NONE_PAYMENT).to(SrmPaymentStatus.PARTIALLY_PAYMENT).on(SrmEventEnum.PAYMENT_ADJUSTMENT)
            .perform(inPayActionImpl);
        builder.externalTransition().from(SrmPaymentStatus.ALL_PAYMENT).to(SrmPaymentStatus.ALL_PAYMENT).on(SrmEventEnum.PAYMENT_ADJUSTMENT)
            .perform(inPayActionImpl);
        builder.externalTransition().from(SrmPaymentStatus.PARTIALLY_PAYMENT).to(SrmPaymentStatus.ALL_PAYMENT).on(SrmEventEnum.PAYMENT_ADJUSTMENT)
            .perform(inPayActionImpl);
        //付款失败事件
        builder.externalTransition().from(SrmPaymentStatus.NONE_PAYMENT).to(SrmPaymentStatus.PAYMENT_EXCEPTION).on(SrmEventEnum.PAYMENT_EXCEPTION)
            .perform(inPayActionImpl);
        builder.externalTransition().from(SrmPaymentStatus.PARTIALLY_PAYMENT).to(SrmPaymentStatus.PAYMENT_EXCEPTION).on(SrmEventEnum.PAYMENT_EXCEPTION)
            .perform(inPayActionImpl);
        //取消付款
        builder.externalTransition().from(SrmPaymentStatus.ALL_PAYMENT).to(SrmPaymentStatus.NONE_PAYMENT).on(SrmEventEnum.CANCEL_PAYMENT)
            .perform(inPayActionImpl);

        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(SrmStateMachines.PURCHASE_IN_PAYMENT_STATE_MACHINE);
    }
}
