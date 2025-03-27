package cn.iocoder.yudao.module.srm.config.purchase.request;

import cn.iocoder.yudao.module.srm.config.BaseFailCallbackImpl;
import cn.iocoder.yudao.module.srm.config.purchase.request.impl.action.ActionStorageImpl;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.request.req.ErpPurchaseRequestAuditReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.SrmStateMachines;
import cn.iocoder.yudao.module.srm.enums.status.ErpOffStatus;
import cn.iocoder.yudao.module.srm.enums.status.ErpOrderStatus;
import cn.iocoder.yudao.module.srm.enums.status.ErpStorageStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmAuditStatus;
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
    private Action<SrmAuditStatus, SrmEventEnum, ErpPurchaseRequestAuditReqVO> actionAuditImpl;
    @Resource
    private Action<ErpOffStatus, SrmEventEnum, ErpPurchaseRequestDO> actionOffImpl;
    @Resource
    private Action<ErpOrderStatus, SrmEventEnum, ErpPurchaseRequestDO> actionOrderImpl;
    @Resource
    private BaseFailCallbackImpl baseFailCallbackImpl;

    @Bean(SrmStateMachines.PURCHASE_REQUEST_AUDIT_STATE_MACHINE_NAME)
    public StateMachine<SrmAuditStatus, SrmEventEnum, ErpPurchaseRequestAuditReqVO> getPurchaseRequestStateMachine() {
        StateMachineBuilder<SrmAuditStatus, SrmEventEnum, ErpPurchaseRequestAuditReqVO> builder = StateMachineBuilderFactory.create();

        // 初始化状态
        builder.internalTransition()
            .within(SrmAuditStatus.DRAFT)
            .on(SrmEventEnum.AUDIT_INIT)
            .perform(actionAuditImpl);

        // 提交审核
        builder.externalTransitions()
            .fromAmong(SrmAuditStatus.DRAFT, SrmAuditStatus.REVOKED, SrmAuditStatus.REJECTED)
            .to(SrmAuditStatus.PENDING_REVIEW)
            .on(SrmEventEnum.SUBMIT_FOR_REVIEW)
            .perform(actionAuditImpl);

        // 审核通过
        builder.externalTransition()
            .from(SrmAuditStatus.PENDING_REVIEW)
            .to(SrmAuditStatus.APPROVED)
            .on(SrmEventEnum.AGREE)
            .perform(actionAuditImpl);

        // 审核不通过
        builder.externalTransition()
            .from(SrmAuditStatus.PENDING_REVIEW)
            .to(SrmAuditStatus.REJECTED)
            .on(SrmEventEnum.REJECT)
            .perform(actionAuditImpl);

        // 反审核
        builder.externalTransition()
            .from(SrmAuditStatus.APPROVED)
            .to(SrmAuditStatus.REVOKED)
            .on(SrmEventEnum.WITHDRAW_REVIEW)
            .perform(actionAuditImpl);
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(SrmStateMachines.PURCHASE_REQUEST_AUDIT_STATE_MACHINE_NAME);
    }

    @Bean(SrmStateMachines.PURCHASE_REQUEST_OFF_STATE_MACHINE_NAME)
    public StateMachine<ErpOffStatus, SrmEventEnum, ErpPurchaseRequestDO> getPurchaseRequestOffStateMachine() {
        StateMachineBuilder<ErpOffStatus, SrmEventEnum, ErpPurchaseRequestDO> builder = StateMachineBuilderFactory.create();
        // 初始化状态
        builder.internalTransition()
            .within(ErpOffStatus.OPEN)
            .on(SrmEventEnum.OFF_INIT)
            .perform(actionOffImpl);
        // 开启
        builder.externalTransitions()
            .fromAmong(ErpOffStatus.CLOSED, ErpOffStatus.MANUAL_CLOSED)
            .to(ErpOffStatus.OPEN)
            .on(SrmEventEnum.ACTIVATE)
            .when(baseConditionImpl)
            .perform(actionOffImpl);
        // 手动关闭
        builder.externalTransition()
            .from(ErpOffStatus.OPEN)
            .to(ErpOffStatus.MANUAL_CLOSED)
            .on(SrmEventEnum.MANUAL_CLOSE)
            .when(baseConditionImpl)
            .perform(actionOffImpl);
        //自动关闭
        builder.externalTransition()
            .from(ErpOffStatus.OPEN)
            .to(ErpOffStatus.CLOSED)
            .on(SrmEventEnum.AUTO_CLOSE)
            .when(baseConditionImpl)
            .perform(actionOffImpl);
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(SrmStateMachines.PURCHASE_REQUEST_OFF_STATE_MACHINE_NAME);
    }

    @Bean(SrmStateMachines.PURCHASE_REQUEST_ORDER_STATE_MACHINE_NAME)
    public StateMachine<ErpOrderStatus, SrmEventEnum, ErpPurchaseRequestDO> getPurchaseOrderStateMachine() {
        StateMachineBuilder<ErpOrderStatus, SrmEventEnum, ErpPurchaseRequestDO> builder = StateMachineBuilderFactory.create();
        //初始化事件
        builder.internalTransition()
            .within(ErpOrderStatus.OT_ORDERED)
            .on(SrmEventEnum.ORDER_INIT)
            .perform(actionOrderImpl);
        // 订购数量调整
        builder.externalTransitions()
            .fromAmong(ErpOrderStatus.OT_ORDERED)
            .to(ErpOrderStatus.PARTIALLY_ORDERED)
            .on(SrmEventEnum.ORDER_ADJUSTMENT)
            .perform(actionOrderImpl);

        builder.externalTransitions()
            .fromAmong(ErpOrderStatus.PARTIALLY_ORDERED)
            .to(ErpOrderStatus.ORDERED)
            .on(SrmEventEnum.ORDER_ADJUSTMENT)
            .perform(actionOrderImpl);

        builder.externalTransitions()
            .fromAmong(ErpOrderStatus.ORDERED)
            .to(ErpOrderStatus.PARTIALLY_ORDERED)
            .on(SrmEventEnum.ORDER_ADJUSTMENT)
            .perform(actionOrderImpl);

        builder.externalTransitions()
            .fromAmong(ErpOrderStatus.PARTIALLY_ORDERED)
            .to(ErpOrderStatus.OT_ORDERED)
            .on(SrmEventEnum.ORDER_ADJUSTMENT)
            .perform(actionOrderImpl);

        //放弃订购
        builder.externalTransitions()
            .fromAmong(ErpOrderStatus.PARTIALLY_ORDERED, ErpOrderStatus.OT_ORDERED)
            .to(ErpOrderStatus.ORDER_FAILED)
            .on(SrmEventEnum.ORDER_CANCEL)
            .perform(actionOrderImpl);
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(SrmStateMachines.PURCHASE_REQUEST_ORDER_STATE_MACHINE_NAME);
    }


    @Resource
    ActionStorageImpl actionStorageImpl;

    @Bean(SrmStateMachines.PURCHASE_REQUEST_STORAGE_STATE_MACHINE_NAME)
    public StateMachine<ErpStorageStatus, SrmEventEnum, ErpPurchaseRequestDO> buildPurchaseOrderItemStorageStateMachine() {
        StateMachineBuilder<ErpStorageStatus, SrmEventEnum, ErpPurchaseRequestDO> builder = StateMachineBuilderFactory.create();
        // 初始化入库
        builder.externalTransition()
            .from(ErpStorageStatus.NONE_IN_STORAGE)
            .to(ErpStorageStatus.NONE_IN_STORAGE)
            .on(SrmEventEnum.STORAGE_INIT)
            .perform(actionStorageImpl);
        // 取消入库
        builder.externalTransitions()
            .fromAmong(ErpStorageStatus.NONE_IN_STORAGE, ErpStorageStatus.PARTIALLY_IN_STORAGE)
            .to(ErpStorageStatus.NONE_IN_STORAGE)
            .on(SrmEventEnum.CANCEL_STORAGE)
            .perform(actionStorageImpl);
        // 库存调整
        builder.externalTransitions()
            .fromAmong(ErpStorageStatus.NONE_IN_STORAGE, ErpStorageStatus.PARTIALLY_IN_STORAGE, ErpStorageStatus.ALL_IN_STORAGE)
            .to(ErpStorageStatus.NONE_IN_STORAGE)
            .on(SrmEventEnum.STOCK_ADJUSTMENT)
            .perform(actionStorageImpl);
        // 设置错误回调
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(SrmStateMachines.PURCHASE_REQUEST_STORAGE_STATE_MACHINE_NAME);
    }
}
