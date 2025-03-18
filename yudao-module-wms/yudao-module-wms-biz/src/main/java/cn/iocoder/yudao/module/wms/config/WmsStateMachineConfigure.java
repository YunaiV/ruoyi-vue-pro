package cn.iocoder.yudao.module.wms.config;


import cn.iocoder.yudao.module.wms.config.statemachine.ColaContext;
import cn.iocoder.yudao.module.wms.config.statemachine.StateMachineWrapper;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.enums.inbound.InboundStatus;
import cn.iocoder.yudao.module.wms.service.inbound.action.InboundSubmitAction;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import org.springframework.context.annotation.Bean;

/**
 * @author: LeeFJ
 * @date: 2025/3/18 18:59
 * @description:
 */
public class WmsStateMachineConfigure {

    @Bean
    public InboundSubmitAction inboundStatusAction() {
        return new InboundSubmitAction();
    }

    @Bean(InboundSubmitAction.STATE_MACHINE_NAME)
    public StateMachineWrapper<Integer, InboundStatus.Event, WmsInboundDO> purchaseRequestStateMachine(InboundSubmitAction inboundStatusAction) {

        StateMachineBuilder<Integer, InboundStatus.Event, ColaContext<WmsInboundDO>> builder = StateMachineBuilderFactory.create();
        // 初始化
        builder.externalTransition()
            // 审批状态从草稿到未审批
            .from(inboundStatusAction.getFrom()).to(inboundStatusAction.getTo())
            // 当 SUBMIT 时间发生时
            .on(inboundStatusAction.getEvent())
            .when(inboundStatusAction.getColaActionWhen())
            .perform(inboundStatusAction.getColaActionPerform());

        return new StateMachineWrapper<>(builder.build(InboundSubmitAction.STATE_MACHINE_NAME),inboundStatusAction.getGetter());

    }

}
