package cn.iocoder.yudao.module.erp.config.purchase.request;

import cn.iocoder.yudao.module.erp.config.purchase.request.impl.BaseFailCallbackImpl;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.req.ErpPurchaseRequestAuditReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.ErpStateMachines;
import cn.iocoder.yudao.module.erp.enums.status.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.status.ErpOffStatus;
import cn.iocoder.yudao.module.erp.enums.status.ErpOrderStatus;
import com.alibaba.cola.statemachine.Action;
import com.alibaba.cola.statemachine.Condition;
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
public class ErpPurchaseRequestStatusMachine {
    @Resource
    private Condition<ErpPurchaseRequestDO> baseConditionImpl;

    @Resource
    private Action<ErpAuditStatus, ErpEventEnum, ErpPurchaseRequestAuditReqVO> actionAuditImpl;
    @Resource
    private Action<ErpOffStatus, ErpEventEnum, ErpPurchaseRequestDO> actionOffImpl;
    @Resource
    private Action<ErpOrderStatus, ErpEventEnum, ErpPurchaseRequestDO> actionOrderImpl;
    @Resource
    private BaseFailCallbackImpl baseFailCallbackImpl;

    @Bean(ErpStateMachines.PURCHASE_REQUEST_STATE_MACHINE_NAME)
    public StateMachine<ErpAuditStatus, ErpEventEnum, ErpPurchaseRequestAuditReqVO> getPurchaseRequestStateMachine() {
        StateMachineBuilder<ErpAuditStatus, ErpEventEnum, ErpPurchaseRequestAuditReqVO> builder = StateMachineBuilderFactory.create();

        // 初始化状态
        builder.internalTransition()
            .within(ErpAuditStatus.DRAFT)
            .on(ErpEventEnum.AUDIT_INIT)
            .perform(actionAuditImpl);

        // 提交审核
        builder.externalTransitions()
            .fromAmong(ErpAuditStatus.DRAFT, ErpAuditStatus.REVOKED, ErpAuditStatus.REJECTED)
            .to(ErpAuditStatus.PENDING_REVIEW)
            .on(ErpEventEnum.SUBMIT_FOR_REVIEW)
            .perform(actionAuditImpl);

        // 审核通过
        builder.externalTransition()
            .from(ErpAuditStatus.PENDING_REVIEW)
            .to(ErpAuditStatus.APPROVED)
            .on(ErpEventEnum.AGREE)
            .perform(actionAuditImpl);

        // 审核不通过
        builder.externalTransition()
            .from(ErpAuditStatus.PENDING_REVIEW)
            .to(ErpAuditStatus.REJECTED)
            .on(ErpEventEnum.REJECT)
            .perform(actionAuditImpl);

        // 反审核
        builder.externalTransition()
            .from(ErpAuditStatus.APPROVED)
            .to(ErpAuditStatus.REVOKED)
            .on(ErpEventEnum.WITHDRAW_REVIEW)
            .perform(actionAuditImpl);
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(ErpStateMachines.PURCHASE_REQUEST_STATE_MACHINE_NAME);
    }

    @Bean(ErpStateMachines.PURCHASE_REQUEST_OFF_STATE_MACHINE_NAME)
    public StateMachine<ErpOffStatus, ErpEventEnum, ErpPurchaseRequestDO> getPurchaseRequestOffStateMachine() {
        StateMachineBuilder<ErpOffStatus, ErpEventEnum, ErpPurchaseRequestDO> builder = StateMachineBuilderFactory.create();
        // 初始化状态
        builder.internalTransition()
            .within(ErpOffStatus.OPEN)
            .on(ErpEventEnum.OFF_INIT)
            .perform(actionOffImpl);
        // 开启
        builder.externalTransitions()
            .fromAmong(ErpOffStatus.CLOSED, ErpOffStatus.MANUAL_CLOSED)
            .to(ErpOffStatus.OPEN)
            .on(ErpEventEnum.ACTIVATE)
            .when(baseConditionImpl)
            .perform(actionOffImpl);
        // 手动关闭
        builder.externalTransition()
            .from(ErpOffStatus.OPEN)
            .to(ErpOffStatus.MANUAL_CLOSED)
            .on(ErpEventEnum.MANUAL_CLOSE)
            .when(baseConditionImpl)
            .perform(actionOffImpl);
        //自动关闭
        builder.externalTransition()
            .from(ErpOffStatus.OPEN)
            .to(ErpOffStatus.CLOSED)
            .on(ErpEventEnum.AUTO_CLOSE)
            .when(baseConditionImpl)
            .perform(actionOffImpl);
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(ErpStateMachines.PURCHASE_REQUEST_OFF_STATE_MACHINE_NAME);
    }

    @Bean(ErpStateMachines.PURCHASE_ORDER_STATE_MACHINE_NAME)
    public StateMachine<ErpOrderStatus, ErpEventEnum, ErpPurchaseRequestDO> getPurchaseOrderStateMachine() {
        StateMachineBuilder<ErpOrderStatus, ErpEventEnum, ErpPurchaseRequestDO> builder = StateMachineBuilderFactory.create();
        //初始化事件
        builder.internalTransition()
            .within(ErpOrderStatus.OT_ORDERED)
            .on(ErpEventEnum.ORDER_INIT)
            .perform(actionOrderImpl);
        //部分采购
        builder.externalTransitions()
            .fromAmong(ErpOrderStatus.OT_ORDERED)
            .to(ErpOrderStatus.PARTIALLY_ORDERED)
            .on(ErpEventEnum.PART_PURCHASE)
            .perform(actionOrderImpl);
        builder.internalTransition()
            .within(ErpOrderStatus.PARTIALLY_ORDERED)
            .on(ErpEventEnum.PART_PURCHASE)
            .perform(actionOrderImpl);
        //完全采购
        builder.externalTransitions()
            .fromAmong(ErpOrderStatus.PARTIALLY_ORDERED)
            .to(ErpOrderStatus.ORDERED)
            .on(ErpEventEnum.FULL_PURCHASE)
            .perform(actionOrderImpl);
        //放弃订购
        builder.externalTransitions()
            .fromAmong(ErpOrderStatus.PARTIALLY_ORDERED, ErpOrderStatus.OT_ORDERED)
            .to(ErpOrderStatus.ORDER_FAILED)
            .on(ErpEventEnum.ORDER_CANCEL)
            .perform(actionOrderImpl);
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(ErpStateMachines.PURCHASE_ORDER_STATE_MACHINE_NAME);
    }
}
