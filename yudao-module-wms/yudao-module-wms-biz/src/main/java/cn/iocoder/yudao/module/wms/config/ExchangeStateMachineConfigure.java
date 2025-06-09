package cn.iocoder.yudao.module.wms.config;

import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.cola.statemachine.builder.StateMachineBuilder;
import cn.iocoder.yudao.framework.cola.statemachine.builder.StateMachineBuilderFactory;
import cn.iocoder.yudao.framework.cola.statemachine.builder.TransitionContext;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.WmsExchangeDO;
import cn.iocoder.yudao.module.wms.enums.exchange.WmsExchangeAuditStatus;
import cn.iocoder.yudao.module.wms.service.exchange.transition.ExchangeAgreeTransitionHandler;
import cn.iocoder.yudao.module.wms.service.exchange.transition.ExchangeRejectTransitionHandler;
import cn.iocoder.yudao.module.wms.service.exchange.transition.ExchangeSubmitTransitionHandler;
import cn.iocoder.yudao.module.wms.service.exchange.transition.ExchangeTransitionFailCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author: LeeFJ
 * @date: 2025/3/19 10:00
 * @description: 盘点单状态机与动作配置
 */
@Slf4j
@Configuration
public class ExchangeStateMachineConfigure {

    /**
     * 状态机名称
     **/
    public static final String STATE_MACHINE_NAME = "exchangeStateMachine";
    
    /**
     * 创建与配置状态机
     **/
    @Bean(ExchangeStateMachineConfigure.STATE_MACHINE_NAME)
    public StateMachine<Integer, WmsExchangeAuditStatus.Event, TransitionContext<WmsExchangeDO>> exchangeStateMachine() {

        StateMachineBuilder<Integer, WmsExchangeAuditStatus.Event, TransitionContext<WmsExchangeDO>> builder = StateMachineBuilderFactory.create();

        // 提交
        builder.externalTransitions()
            .fromAmong(WmsExchangeAuditStatus.DRAFT.getValue(), WmsExchangeAuditStatus.REJECT.getValue())
            .to(WmsExchangeAuditStatus.AUDITING.getValue())
            .on(WmsExchangeAuditStatus.Event.SUBMIT)
            .handle(ExchangeSubmitTransitionHandler.class);

        // 同意
        builder.externalTransition()
            .from(WmsExchangeAuditStatus.AUDITING.getValue())
            .to(WmsExchangeAuditStatus.PASS.getValue())
            .on(WmsExchangeAuditStatus.Event.AGREE)
            .handle(ExchangeAgreeTransitionHandler.class);

        // 拒绝
        builder.externalTransition()
            .from(WmsExchangeAuditStatus.AUDITING.getValue())
            .to(WmsExchangeAuditStatus.REJECT.getValue())
            .on(WmsExchangeAuditStatus.Event.REJECT)
            .handle(ExchangeRejectTransitionHandler.class);


        // 失败处理
        builder.setFailCallback(ExchangeTransitionFailCallback.class);

        return builder.build(ExchangeStateMachineConfigure.STATE_MACHINE_NAME, ctx -> ctx.data().getAuditStatus());


    }
 


}
