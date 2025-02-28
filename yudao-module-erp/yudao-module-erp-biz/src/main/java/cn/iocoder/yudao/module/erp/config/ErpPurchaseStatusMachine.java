package cn.iocoder.yudao.module.erp.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.ErpOffStatus;
import com.alibaba.cola.statemachine.Action;
import com.alibaba.cola.statemachine.Condition;
import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ErpPurchaseStatusMachine {
    @Autowired
    ErpPurchaseRequestMapper mapper;
    @Resource
    @Qualifier("baseConditionImpl")
    private Condition<ErpPurchaseRequestDO> conditionImpl;
    @Resource
    @Qualifier("actionImpl")
    private Action<ErpAuditStatus, ErpEventEnum, ErpPurchaseRequestDO> actionImpl;

    @Resource
    @Qualifier("ActionOffImpl")
    private Action<ErpOffStatus, ErpEventEnum, ErpPurchaseRequestDO> actionOffImpl;

    @Bean("purchaseRequestStateMachine")
    public StateMachine<ErpAuditStatus, ErpEventEnum, ErpPurchaseRequestDO> getPurchaseRequestStateMachine() {
        StateMachineBuilder<ErpAuditStatus, ErpEventEnum, ErpPurchaseRequestDO> builder = StateMachineBuilderFactory.create();

        // 初始化状态
        builder.internalTransition()
            .within(ErpAuditStatus.DRAFT)
            .on(ErpEventEnum.INIT)
            .perform(actionImpl);

        // 提交审核
        builder.externalTransitions()
            .fromAmong(ErpAuditStatus.DRAFT, ErpAuditStatus.REVERSED)
            .to(ErpAuditStatus.PROCESS)
            .on(ErpEventEnum.SUBMIT_FOR_REVIEW)
            .when(conditionImpl)
            .perform(actionImpl);

        // 审核通过
        builder.externalTransitions()
            .fromAmong(ErpAuditStatus.PROCESS, ErpAuditStatus.REJECTED)
            .to(ErpAuditStatus.APPROVE)
            .on(ErpEventEnum.AGREE)
            .when(conditionImpl)
            .perform(actionImpl);

        // 审核不通过
        builder.externalTransition()
            .from(ErpAuditStatus.PROCESS)
            .to(ErpAuditStatus.REJECTED)
            .on(ErpEventEnum.REJECT)
            .when(conditionImpl)
            .perform(actionImpl);

        // 反审核
        builder.externalTransition()
            .from(ErpAuditStatus.APPROVE)
            .to(ErpAuditStatus.REVERSED)
            .on(ErpEventEnum.WITHDRAW_REVIEW)
            .when(conditionImpl)
            .perform(actionImpl);

        //错误回调函数
        builder.setFailCallback((f, e, o) -> {
            String msg = StrUtil.format("状态机执行失败,订单：{}，事件：{}，form状态({})", JSONUtil.toJsonStr(o), e.getDesc(), f.getDesc());
            log.warn(msg);
            throw new RuntimeException(msg);
        });
        return builder.build("purchaseRequestStateMachine");
    }


    @Bean("purchaseRequestOffStateMachine")
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
            .when(conditionImpl)
            .perform(actionOffImpl);
        // 手动关闭
        builder.externalTransition()
            .from(ErpOffStatus.OPEN)
            .to(ErpOffStatus.MANUAL_CLOSED)
            .on(ErpEventEnum.MANUAL_CLOSE)
            .when(conditionImpl)
            .perform(actionOffImpl);
        //自动关闭
        builder.externalTransition()
            .from(ErpOffStatus.OPEN)
            .to(ErpOffStatus.CLOSED)
            .on(ErpEventEnum.AUTO_CLOSE)
            .when(conditionImpl)
            .perform(actionOffImpl);
        //错误回调函数
        builder.setFailCallback((f, e, o) -> {
            String msg = StrUtil.format("状态机执行失败,订单：{}，事件：{}，form状态({})", JSONUtil.toJsonStr(o), e.getDesc(), f.getDesc());
            log.warn(msg);
            throw new RuntimeException(msg);
        });
        return builder.build("purchaseRequestOffStateMachine");
    }
}
