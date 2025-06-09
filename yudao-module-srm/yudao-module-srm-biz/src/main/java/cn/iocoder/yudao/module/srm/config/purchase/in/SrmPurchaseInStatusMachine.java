package cn.iocoder.yudao.module.srm.config.purchase.in;

import cn.iocoder.yudao.framework.cola.statemachine.Action;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.cola.statemachine.builder.StateMachineBuilder;
import cn.iocoder.yudao.framework.cola.statemachine.builder.StateMachineBuilderFactory;
import cn.iocoder.yudao.module.srm.config.BaseFailCallbackImpl;
import cn.iocoder.yudao.module.srm.config.machine.in.SrmPurchaseInCountContext;
import cn.iocoder.yudao.module.srm.config.purchase.in.impl.action.InAuditActionImpl;
import cn.iocoder.yudao.module.srm.config.purchase.in.impl.action.InPayActionImpl;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.req.SrmPurchaseInAuditReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInDO;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.SrmStateMachines;
import cn.iocoder.yudao.module.srm.enums.status.SrmAuditStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmPaymentStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmStorageStatus;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static cn.iocoder.yudao.module.srm.enums.status.SrmAuditStatus.*;
import static cn.iocoder.yudao.module.srm.enums.status.SrmPaymentStatus.*;

//采购入库单主表状态机
@Slf4j
@Configuration
@SuppressWarnings({"rawtypes", "unchecked"})
public class SrmPurchaseInStatusMachine {

    @Resource
    InAuditActionImpl inAuditActionImpl;
    @Resource
    InPayActionImpl inPayActionImpl;
    @Autowired
    private BaseFailCallbackImpl baseFailCallbackImpl;

    @Bean(SrmStateMachines.PURCHASE_IN_AUDIT_STATE_MACHINE)
    public StateMachine<SrmAuditStatus, SrmEventEnum, SrmPurchaseInAuditReqVO> getPurchaseRequestStateMachine() {
        StateMachineBuilder<SrmAuditStatus, SrmEventEnum, SrmPurchaseInAuditReqVO> builder = StateMachineBuilderFactory.create();

        // 初始化状态
        builder.internalTransition().within(DRAFT).on(SrmEventEnum.AUDIT_INIT).perform(inAuditActionImpl);

        // 提交审核
        builder.externalTransitions().fromAmong(DRAFT, REVOKED, REJECTED).to(PENDING_REVIEW).on(SrmEventEnum.SUBMIT_FOR_REVIEW).perform(inAuditActionImpl);

        // 审核通过
        builder.externalTransition().from(PENDING_REVIEW).to(APPROVED).on(SrmEventEnum.AGREE).perform(inAuditActionImpl);

        // 审核不通过
        builder.externalTransition().from(PENDING_REVIEW).to(REJECTED).on(SrmEventEnum.REJECT).perform(inAuditActionImpl);

        // 反审核
        builder.externalTransition().from(APPROVED).to(REVOKED).on(SrmEventEnum.WITHDRAW_REVIEW).perform(inAuditActionImpl);

        builder.setFailCallback(baseFailCallbackImpl);

        return builder.build(SrmStateMachines.PURCHASE_IN_AUDIT_STATE_MACHINE);
    }

    @Bean(SrmStateMachines.PURCHASE_IN_PAYMENT_STATE_MACHINE)
    public StateMachine<SrmPaymentStatus, SrmEventEnum, SrmPurchaseInDO> getPurchaseRequestPaymentStateMachine() {
        StateMachineBuilder<SrmPaymentStatus, SrmEventEnum, SrmPurchaseInDO> builder = StateMachineBuilderFactory.create();
        //初始化
        builder.internalTransition().within(NONE_PAYMENT).on(SrmEventEnum.PAYMENT_INIT).perform(inPayActionImpl);
        //付款金额调整
        builder.externalTransitions().fromAmong(NONE_PAYMENT, PARTIALLY_PAYMENT, ALL_PAYMENT, PAYMENT_EXCEPTION).to(PARTIALLY_PAYMENT).on(SrmEventEnum.PAYMENT_ADJUSTMENT).perform(inPayActionImpl);

        //付款失败事件
        builder.externalTransitions().fromAmong(NONE_PAYMENT, PARTIALLY_PAYMENT).to(PAYMENT_EXCEPTION).on(SrmEventEnum.PAYMENT_EXCEPTION)
            .perform(inPayActionImpl);
        //取消付款
        builder.externalTransition().from(ALL_PAYMENT).to(NONE_PAYMENT).on(SrmEventEnum.CANCEL_PAYMENT)
            .perform(inPayActionImpl);

        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(SrmStateMachines.PURCHASE_IN_PAYMENT_STATE_MACHINE);
    }

    @Autowired
    Action<SrmStorageStatus, SrmEventEnum, SrmPurchaseInCountContext> inStorageActionImpl;

    @Bean(SrmStateMachines.PURCHASE_IN_STORAGE_STATE_MACHINE)
    public StateMachine<SrmStorageStatus, SrmEventEnum, SrmPurchaseInCountContext> getPurchaseRequestStorageStateMachine() {
        StateMachineBuilder<SrmStorageStatus, SrmEventEnum, SrmPurchaseInCountContext> builder = StateMachineBuilderFactory.create();
        //初始化
        builder.internalTransition().within(SrmStorageStatus.NONE_IN_STORAGE).on(SrmEventEnum.STORAGE_INIT).perform(inStorageActionImpl);
        //入库调整
        builder.externalTransitions().fromAmong(SrmStorageStatus.NONE_IN_STORAGE, SrmStorageStatus.PARTIALLY_IN_STORAGE, SrmStorageStatus.ALL_IN_STORAGE)
                .to(SrmStorageStatus.PARTIALLY_IN_STORAGE).on(SrmEventEnum.STOCK_ADJUSTMENT).perform(inStorageActionImpl);

        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(SrmStateMachines.PURCHASE_IN_STORAGE_STATE_MACHINE);
    }
}
