package cn.iocoder.yudao.module.srm.config.purchase.request;

import cn.iocoder.yudao.module.srm.config.purchase.request.impl.action.StorageActionImpl;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.request.req.SrmPurchaseRequestAuditReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestDO;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmAuditStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmOffStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmOrderStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmStorageStatus;
import com.alibaba.cola.statemachine.Action;
import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.builder.FailCallback;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.*;

@Slf4j
@Configuration
@SuppressWarnings({"rawtypes", "unchecked"})
public class SrmPurchaseRequestStatusMachine {

    @Resource
    private Action<SrmAuditStatus, SrmEventEnum, SrmPurchaseRequestAuditReqVO> auditActionImpl;


    @Resource
    private FailCallback baseFailCallbackImpl;

    @Bean(PURCHASE_REQUEST_AUDIT_STATE_MACHINE_NAME)
    public StateMachine<SrmAuditStatus, SrmEventEnum, SrmPurchaseRequestAuditReqVO> getPurchaseRequestStateMachine() {
        StateMachineBuilder<SrmAuditStatus, SrmEventEnum, SrmPurchaseRequestAuditReqVO> builder = StateMachineBuilderFactory.create();

        // 初始化状态
        builder.internalTransition().within(SrmAuditStatus.DRAFT).on(SrmEventEnum.AUDIT_INIT).perform(auditActionImpl);

        // 提交审核
        builder.externalTransitions().fromAmong(SrmAuditStatus.DRAFT, SrmAuditStatus.REVOKED, SrmAuditStatus.REJECTED).to(SrmAuditStatus.PENDING_REVIEW).on(SrmEventEnum.SUBMIT_FOR_REVIEW).perform(auditActionImpl);

        // 审核通过
        builder.externalTransition().from(SrmAuditStatus.PENDING_REVIEW).to(SrmAuditStatus.APPROVED).on(SrmEventEnum.AGREE).perform(auditActionImpl);

        // 审核不通过
        builder.externalTransition().from(SrmAuditStatus.PENDING_REVIEW).to(SrmAuditStatus.REJECTED).on(SrmEventEnum.REJECT).perform(auditActionImpl);

        // 反审核
        builder.externalTransition().from(SrmAuditStatus.APPROVED).to(SrmAuditStatus.REVOKED).on(SrmEventEnum.WITHDRAW_REVIEW).perform(auditActionImpl);
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(PURCHASE_REQUEST_AUDIT_STATE_MACHINE_NAME);
    }

    @Resource
    private Action<SrmOffStatus, SrmEventEnum, SrmPurchaseRequestDO> offActionImpl;
    @Bean(PURCHASE_REQUEST_OFF_STATE_MACHINE_NAME)
    public StateMachine<SrmOffStatus, SrmEventEnum, SrmPurchaseRequestDO> getPurchaseRequestOffStateMachine() {
        StateMachineBuilder<SrmOffStatus, SrmEventEnum, SrmPurchaseRequestDO> builder = StateMachineBuilderFactory.create();
        // 初始化状态
        builder.internalTransition().within(SrmOffStatus.OPEN).on(SrmEventEnum.OFF_INIT).perform(offActionImpl);
        // 开启
        builder.externalTransitions().fromAmong(SrmOffStatus.CLOSED, SrmOffStatus.MANUAL_CLOSED).to(SrmOffStatus.OPEN).on(SrmEventEnum.ACTIVATE).perform(offActionImpl);
        // 手动关闭
        builder.externalTransition().from(SrmOffStatus.OPEN).to(SrmOffStatus.MANUAL_CLOSED).on(SrmEventEnum.MANUAL_CLOSE).perform(offActionImpl);
        //自动关闭
        builder.externalTransition().from(SrmOffStatus.OPEN).to(SrmOffStatus.CLOSED).on(SrmEventEnum.AUTO_CLOSE).perform(offActionImpl);
        //撤销关闭
        builder.externalTransitions().fromAmong(SrmOffStatus.MANUAL_CLOSED, SrmOffStatus.CLOSED, SrmOffStatus.OPEN).to(SrmOffStatus.OPEN).on(SrmEventEnum.CANCEL_DELETE).perform(offActionImpl);
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(PURCHASE_REQUEST_OFF_STATE_MACHINE_NAME);
    }

    @Resource
    private Action<SrmOrderStatus, SrmEventEnum, SrmPurchaseRequestDO> orderActionImpl;
    @Bean(PURCHASE_REQUEST_ORDER_STATE_MACHINE_NAME)
    public StateMachine<SrmOrderStatus, SrmEventEnum, SrmPurchaseRequestDO> getPurchaseOrderStateMachine() {
        StateMachineBuilder<SrmOrderStatus, SrmEventEnum, SrmPurchaseRequestDO> builder = StateMachineBuilderFactory.create();
        //初始化事件
        builder.internalTransition().within(SrmOrderStatus.OT_ORDERED).on(SrmEventEnum.ORDER_INIT).perform(orderActionImpl);
        // 订购数量调整
        builder.externalTransitions().fromAmong(SrmOrderStatus.OT_ORDERED).to(SrmOrderStatus.PARTIALLY_ORDERED).on(SrmEventEnum.ORDER_ADJUSTMENT).perform(orderActionImpl);

        builder.externalTransitions().fromAmong(SrmOrderStatus.PARTIALLY_ORDERED).to(SrmOrderStatus.ORDERED).on(SrmEventEnum.ORDER_ADJUSTMENT).perform(orderActionImpl);

        builder.externalTransitions().fromAmong(SrmOrderStatus.ORDERED).to(SrmOrderStatus.PARTIALLY_ORDERED).on(SrmEventEnum.ORDER_ADJUSTMENT).perform(orderActionImpl);

        builder.externalTransitions().fromAmong(SrmOrderStatus.PARTIALLY_ORDERED).to(SrmOrderStatus.OT_ORDERED).on(SrmEventEnum.ORDER_ADJUSTMENT).perform(orderActionImpl);

        //放弃订购
        builder.externalTransitions().fromAmong(SrmOrderStatus.PARTIALLY_ORDERED, SrmOrderStatus.OT_ORDERED).to(SrmOrderStatus.ORDER_FAILED).on(SrmEventEnum.ORDER_CANCEL).perform(orderActionImpl);
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(PURCHASE_REQUEST_ORDER_STATE_MACHINE_NAME);
    }


    @Resource
    StorageActionImpl storageActionImpl;

    @Bean(PURCHASE_REQUEST_STORAGE_STATE_MACHINE_NAME)
    public StateMachine<SrmStorageStatus, SrmEventEnum, SrmPurchaseRequestDO> buildPurchaseOrderItemStorageStateMachine() {
        StateMachineBuilder<SrmStorageStatus, SrmEventEnum, SrmPurchaseRequestDO> builder = StateMachineBuilderFactory.create();
        // 初始化入库
        builder.externalTransition().from(SrmStorageStatus.NONE_IN_STORAGE).to(SrmStorageStatus.NONE_IN_STORAGE).on(SrmEventEnum.STORAGE_INIT).perform(storageActionImpl);
        // 取消入库
        builder.externalTransitions().fromAmong(SrmStorageStatus.NONE_IN_STORAGE, SrmStorageStatus.PARTIALLY_IN_STORAGE).to(SrmStorageStatus.NONE_IN_STORAGE).on(SrmEventEnum.CANCEL_STORAGE).perform(storageActionImpl);
        // 库存调整
        builder.externalTransitions().fromAmong(SrmStorageStatus.NONE_IN_STORAGE, SrmStorageStatus.PARTIALLY_IN_STORAGE, SrmStorageStatus.ALL_IN_STORAGE).to(SrmStorageStatus.NONE_IN_STORAGE).on(SrmEventEnum.STOCK_ADJUSTMENT).perform(storageActionImpl);
        // 设置错误回调
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(PURCHASE_REQUEST_STORAGE_STATE_MACHINE_NAME);
    }
}
