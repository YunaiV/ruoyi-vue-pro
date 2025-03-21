package cn.iocoder.yudao.module.erp.config.purchase.returned;

import cn.iocoder.yudao.module.erp.config.BaseFailCallbackImpl;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnAuditReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.iocoder.yudao.module.erp.enums.SrmEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpReturnStatus;
import cn.iocoder.yudao.module.erp.enums.status.SrmAuditStatus;
import com.alibaba.cola.statemachine.Action;
import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static cn.iocoder.yudao.module.erp.enums.SrmStateMachines.PURCHASE_RETURN_AUDIT_STATE_MACHINE_NAME;
import static cn.iocoder.yudao.module.erp.enums.SrmStateMachines.PURCHASE_RETURN_REFUND_STATE_MACHINE_NAME;

@Slf4j
@Configuration
@SuppressWarnings({"rawtypes", "unchecked"})
public class ErpPurchaseReturnedStatusMachine {
    @Resource
    private BaseFailCallbackImpl baseFailCallbackImpl;
    @Resource
    private Action actionRefundAuditImpl;

    //审核状态
    @Bean(PURCHASE_RETURN_AUDIT_STATE_MACHINE_NAME)
    public StateMachine getAuditMachine() {
        StateMachineBuilder<SrmAuditStatus, SrmEventEnum, ErpPurchaseReturnAuditReqVO> builder = StateMachineBuilderFactory.create();

        // 初始化状态
        builder.internalTransition()
            .within(SrmAuditStatus.DRAFT)
            .on(SrmEventEnum.AUDIT_INIT)
            .perform(actionRefundAuditImpl);

        // 提交审核
        builder.externalTransitions()
            .fromAmong(SrmAuditStatus.DRAFT, SrmAuditStatus.REVOKED, SrmAuditStatus.REJECTED)
            .to(SrmAuditStatus.PENDING_REVIEW)
            .on(SrmEventEnum.SUBMIT_FOR_REVIEW)
            .perform(actionRefundAuditImpl);

        // 审核通过
        builder.externalTransition()
            .from(SrmAuditStatus.PENDING_REVIEW)
            .to(SrmAuditStatus.APPROVED)
            .on(SrmEventEnum.AGREE)
            .perform(actionRefundAuditImpl);

        // 审核不通过
        builder.externalTransition()
            .from(SrmAuditStatus.PENDING_REVIEW)
            .to(SrmAuditStatus.REJECTED)
            .on(SrmEventEnum.REJECT)
            .perform(actionRefundAuditImpl);

        // 反审核
        builder.externalTransition()
            .from(SrmAuditStatus.APPROVED)
            .to(SrmAuditStatus.REVOKED)
            .on(SrmEventEnum.WITHDRAW_REVIEW)
            .perform(actionRefundAuditImpl);
        builder.setFailCallback(baseFailCallbackImpl);

        return builder.build(PURCHASE_RETURN_AUDIT_STATE_MACHINE_NAME);
    }

    //退款状态
    @Resource
    private Action actionRefundImpl;

    @Bean(PURCHASE_RETURN_REFUND_STATE_MACHINE_NAME)
    public StateMachine getRefundMachine() {
        StateMachineBuilder<ErpReturnStatus, SrmEventEnum, ErpPurchaseReturnDO> builder = StateMachineBuilderFactory.create();
        // 初始化状态
        builder.internalTransition()
            .within(ErpReturnStatus.NOT_RETURN)
            .on(SrmEventEnum.RETURN_INIT)
            .perform(actionRefundImpl);
        //RETURN_COMPLETE
        builder.externalTransition()
            .from(ErpReturnStatus.NOT_RETURN)
            .to(ErpReturnStatus.RETURNED)
            .on(SrmEventEnum.RETURN_COMPLETE)
            .perform(actionRefundImpl);
        //取消退款
        builder.externalTransition()
            .from(ErpReturnStatus.RETURNED)
            .to(ErpReturnStatus.NOT_RETURN)
            .on(SrmEventEnum.RETURN_COMPLETE)
            .perform(actionRefundImpl);
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(PURCHASE_RETURN_REFUND_STATE_MACHINE_NAME);
    }
}
