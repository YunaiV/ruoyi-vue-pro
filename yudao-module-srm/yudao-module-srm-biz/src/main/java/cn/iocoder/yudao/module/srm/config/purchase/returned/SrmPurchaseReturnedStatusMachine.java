package cn.iocoder.yudao.module.srm.config.purchase.returned;

import cn.iocoder.yudao.framework.cola.statemachine.Action;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.cola.statemachine.builder.StateMachineBuilder;
import cn.iocoder.yudao.framework.cola.statemachine.builder.StateMachineBuilderFactory;
import cn.iocoder.yudao.module.srm.config.BaseFailCallbackImpl;
import cn.iocoder.yudao.module.srm.config.machine.outItem.SrmPurchaseOutMachineContext;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.returns.SrmPurchaseReturnAuditReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnDO;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmAuditStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmOutboundStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmReturnStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.*;

@Slf4j
@Configuration
@SuppressWarnings({"rawtypes", "unchecked"})
public class SrmPurchaseReturnedStatusMachine {

    @Autowired
    private BaseFailCallbackImpl baseFailCallbackImpl;

    //审核状态
    @Autowired
    private Action<SrmAuditStatus, SrmEventEnum, SrmPurchaseReturnAuditReqVO> refundAuditActionImpl;
    @Bean(PURCHASE_RETURN_AUDIT_STATE_MACHINE_NAME)
    public StateMachine getAuditMachine() {
        StateMachineBuilder<SrmAuditStatus, SrmEventEnum, SrmPurchaseReturnAuditReqVO> builder = StateMachineBuilderFactory.create();

        // 初始化状态
        builder.internalTransition().within(SrmAuditStatus.DRAFT).on(SrmEventEnum.AUDIT_INIT).perform(refundAuditActionImpl);

        // 提交审核
        builder.externalTransitions().fromAmong(SrmAuditStatus.DRAFT, SrmAuditStatus.REVOKED, SrmAuditStatus.REJECTED).to(SrmAuditStatus.PENDING_REVIEW).on(SrmEventEnum.SUBMIT_FOR_REVIEW).perform(refundAuditActionImpl);

        // 审核通过
        builder.externalTransition().from(SrmAuditStatus.PENDING_REVIEW).to(SrmAuditStatus.APPROVED).on(SrmEventEnum.AGREE).perform(refundAuditActionImpl);

        // 审核不通过
        builder.externalTransition().from(SrmAuditStatus.PENDING_REVIEW).to(SrmAuditStatus.REJECTED).on(SrmEventEnum.REJECT).perform(refundAuditActionImpl);

        // 反审核
        builder.externalTransition().from(SrmAuditStatus.APPROVED).to(SrmAuditStatus.REVOKED).on(SrmEventEnum.WITHDRAW_REVIEW).perform(refundAuditActionImpl);
        builder.setFailCallback(baseFailCallbackImpl);

        return builder.build(PURCHASE_RETURN_AUDIT_STATE_MACHINE_NAME);
    }

    //退款状态
    @Autowired
    private Action<SrmReturnStatus, SrmEventEnum, SrmPurchaseReturnDO> refundActionImpl;
    @Bean(PURCHASE_RETURN_REFUND_STATE_MACHINE_NAME)
    public StateMachine getRefundMachine() {
        StateMachineBuilder<SrmReturnStatus, SrmEventEnum, SrmPurchaseReturnDO> builder = StateMachineBuilderFactory.create();
        // 初始化状态
        builder.internalTransition().within(SrmReturnStatus.NOT_RETURN).on(SrmEventEnum.RETURN_INIT).perform(refundActionImpl);
        //RETURN_COMPLETE
        builder.externalTransition().from(SrmReturnStatus.NOT_RETURN).to(SrmReturnStatus.RETURNED).on(SrmEventEnum.RETURN_COMPLETE).perform(refundActionImpl);
        //取消退款
        builder.externalTransition().from(SrmReturnStatus.RETURNED).to(SrmReturnStatus.NOT_RETURN).on(SrmEventEnum.RETURN_COMPLETE).perform(refundActionImpl);
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(PURCHASE_RETURN_REFUND_STATE_MACHINE_NAME);
    }

    //出库
    @Autowired
    private Action<SrmOutboundStatus, SrmEventEnum, SrmPurchaseOutMachineContext> outboundActionImpl;

    @Bean(PURCHASE_RETURN_OUT_STORAGE_STATE_MACHINE_NAME)
    public StateMachine getOutboundMachine() {
        StateMachineBuilder<SrmOutboundStatus, SrmEventEnum, SrmPurchaseOutMachineContext> builder = StateMachineBuilderFactory.create();
        //初始化
        builder.internalTransition().within(SrmOutboundStatus.NONE_OUTBOUND).on(SrmEventEnum.OUT_STORAGE_INIT).perform(outboundActionImpl);
        //出库数量调整
        builder.externalTransitions().fromAmong(SrmOutboundStatus.NONE_OUTBOUND, SrmOutboundStatus.PARTIALLY_OUTBOUND, SrmOutboundStatus.ALL_OUTBOUND).to(SrmOutboundStatus.PARTIALLY_OUTBOUND)
            .on(SrmEventEnum.OUT_STORAGE_ADJUSTMENT).perform(outboundActionImpl);

        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(PURCHASE_RETURN_OUT_STORAGE_STATE_MACHINE_NAME);
    }
}
