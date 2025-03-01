package cn.iocoder.yudao.module.erp.config.purchase;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.ErpStateMachines;
import cn.iocoder.yudao.module.erp.enums.status.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.status.ErpOffStatus;
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
public class ErpPurchaseStatusMachine {
    @Resource
    private Condition<ErpPurchaseRequestDO> baseConditionImpl;

    @Resource
    private Action<ErpAuditStatus, ErpEventEnum, ErpPurchaseRequestDO> actionAuditImpl;
    @Resource
    private Action<ErpOffStatus, ErpEventEnum, ErpPurchaseRequestDO> actionOffImpl;
    @Resource
    private ErpPurchaseRequestMapper mapper;

    @Bean(ErpStateMachines.PURCHASE_REQUEST_STATE_MACHINE_NAME)
    public StateMachine<ErpAuditStatus, ErpEventEnum, ErpPurchaseRequestDO> getPurchaseRequestStateMachine() {
        StateMachineBuilder<ErpAuditStatus, ErpEventEnum, ErpPurchaseRequestDO> builder = StateMachineBuilderFactory.create();

        // 初始化状态
        builder.internalTransition()
            .within(ErpAuditStatus.DRAFT)
            .on(ErpEventEnum.INIT)
            .when(baseConditionImpl)
            .perform(actionAuditImpl);

        // 提交审核
        builder.externalTransitions()
            .fromAmong(ErpAuditStatus.DRAFT, ErpAuditStatus.REVOKED, ErpAuditStatus.REJECTED)
            .to(ErpAuditStatus.PENDING_REVIEW)
            .on(ErpEventEnum.SUBMIT_FOR_REVIEW)
            .when(baseConditionImpl)
            .perform(actionAuditImpl);

        // 审核通过
        builder.externalTransition()
            .from(ErpAuditStatus.PENDING_REVIEW)
            .to(ErpAuditStatus.APPROVED)
            .on(ErpEventEnum.AGREE)
            .when(baseConditionImpl)
            .perform(actionAuditImpl);

        // 审核不通过
        builder.externalTransition()
            .from(ErpAuditStatus.PENDING_REVIEW)
            .to(ErpAuditStatus.REJECTED)
            .on(ErpEventEnum.REJECT)
            .when(baseConditionImpl)
            .perform(actionAuditImpl);

        // 反审核
        builder.externalTransition()
            .from(ErpAuditStatus.APPROVED)
            .to(ErpAuditStatus.REVOKED)
            .on(ErpEventEnum.WITHDRAW_REVIEW)
            .when(baseConditionImpl)
            .perform(actionAuditImpl);

        //错误回调函数
        builder.setFailCallback((f, e, o) -> {
            String msg = StrUtil.format("状态机执行失败,订单：{}，事件：{}，起始状态({})", JSONUtil.toJsonStr(o), e.getDesc(), f.getDesc());
            log.warn(msg);
            throw new RuntimeException(msg);
        });
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
        //错误回调函数
        builder.setFailCallback((f, e, o) -> {
            String msg = StrUtil.format("状态机执行失败,订单：{}，事件：{}，form状态({})", JSONUtil.toJsonStr(o), e.getDesc(), f.getDesc());
            log.warn(msg);
            throw new RuntimeException(msg);
        });
        return builder.build(ErpStateMachines.PURCHASE_REQUEST_OFF_STATE_MACHINE_NAME);
    }
}
