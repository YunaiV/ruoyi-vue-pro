package cn.iocoder.yudao.module.tms.config.transfer;


import cn.iocoder.yudao.framework.cola.statemachine.Action;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.cola.statemachine.builder.StateMachineBuilder;
import cn.iocoder.yudao.framework.cola.statemachine.builder.StateMachineBuilderFactory;
import cn.iocoder.yudao.module.tms.config.TmsFailCallbackImpl;
import cn.iocoder.yudao.module.tms.controller.admin.transfer.vo.TmsTransferAuditReqVO;
import cn.iocoder.yudao.module.tms.enums.TmsEventEnum;
import cn.iocoder.yudao.module.tms.enums.status.TmsAuditStatus;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static cn.iocoder.yudao.module.tms.enums.TmsStateMachines.TRANSFER_AUDIT_STATE_MACHINE;

@Slf4j
@Configuration
@SuppressWarnings({"rawtypes", "unchecked"})
public class TmsTransferStatusMachine {
    @Resource
    TmsFailCallbackImpl tmsFailCallbackImpl;

    @Autowired
    Action<TmsAuditStatus, TmsEventEnum, TmsTransferAuditReqVO> TmsTransferImpl;

    @Bean(TRANSFER_AUDIT_STATE_MACHINE)
    public StateMachine<TmsAuditStatus, TmsEventEnum, TmsTransferAuditReqVO> getFirstMileRequestStateMachine() {
        StateMachineBuilder<TmsAuditStatus, TmsEventEnum, TmsTransferAuditReqVO> builder = StateMachineBuilderFactory.create();

        // 初始化状态
        builder.internalTransition().within(TmsAuditStatus.DRAFT).on(TmsEventEnum.AUDIT_INIT).perform(TmsTransferImpl);

        // 提交审核
        builder.externalTransitions().fromAmong(TmsAuditStatus.DRAFT, TmsAuditStatus.REVOKED, TmsAuditStatus.REJECTED).to(TmsAuditStatus.PENDING_REVIEW)
            .on(TmsEventEnum.SUBMIT_FOR_REVIEW).perform(TmsTransferImpl);

        // 审核通过
        builder.externalTransition().from(TmsAuditStatus.PENDING_REVIEW).to(TmsAuditStatus.APPROVED).on(TmsEventEnum.AGREE).perform(TmsTransferImpl);

        // 审核不通过
        builder.externalTransition().from(TmsAuditStatus.PENDING_REVIEW).to(TmsAuditStatus.REJECTED).on(TmsEventEnum.REJECT).perform(TmsTransferImpl);

        // 反审核
        builder.externalTransition().from(TmsAuditStatus.APPROVED).to(TmsAuditStatus.REVOKED).on(TmsEventEnum.WITHDRAW_REVIEW).perform(TmsTransferImpl);

        builder.setFailCallback(tmsFailCallbackImpl);

        return builder.build(TRANSFER_AUDIT_STATE_MACHINE);
    }
}
