package cn.iocoder.yudao.module.wms.config;

import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.cola.statemachine.builder.StateMachineBuilder;
import cn.iocoder.yudao.framework.cola.statemachine.builder.StateMachineBuilderFactory;
import cn.iocoder.yudao.framework.cola.statemachine.builder.TransitionContext;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import cn.iocoder.yudao.module.wms.service.outbound.transition.OutboundAgreeTransitionHandler;
import cn.iocoder.yudao.module.wms.service.outbound.transition.OutboundExecuteTransitionHandler;
import cn.iocoder.yudao.module.wms.service.outbound.transition.OutboundRejectTransitionHandler;
import cn.iocoder.yudao.module.wms.service.outbound.transition.OutboundSubmitTransitionHandler;
import cn.iocoder.yudao.module.wms.service.outbound.transition.OutboundTransitionFailCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author: LeeFJ
 * @date: 2025/3/19 10:00
 * @description: 出库单状态机与动作配置
 */
@Slf4j
@Configuration
public class OutboundStateMachineConfigure {

    /**
     * 状态机名称
     **/
    public static final String STATE_MACHINE_NAME = "outboundStateMachine";


    /**
     * 创建与配置状态机
     **/
    @Bean(OutboundStateMachineConfigure.STATE_MACHINE_NAME)
    public StateMachine<Integer, WmsOutboundAuditStatus.Event, TransitionContext<WmsOutboundDO>> inboundActionStateMachine() {

        StateMachineBuilder<Integer, WmsOutboundAuditStatus.Event, TransitionContext<WmsOutboundDO>> builder = StateMachineBuilderFactory.create();

        // 提交
        builder.externalTransitions()
            .fromAmong(WmsOutboundAuditStatus.DRAFT.getValue(), WmsOutboundAuditStatus.REJECT.getValue())
            .to(WmsOutboundAuditStatus.AUDITING.getValue())
            .on(WmsOutboundAuditStatus.Event.SUBMIT)
            .handle(OutboundSubmitTransitionHandler.class);

        // 同意
        builder.externalTransition()
            .from(WmsOutboundAuditStatus.AUDITING.getValue())
            .to(WmsOutboundAuditStatus.PASS.getValue())
            .on(WmsOutboundAuditStatus.Event.AGREE)
            .handle(OutboundAgreeTransitionHandler.class);

        // 拒绝
        builder.externalTransition()
            .from(WmsOutboundAuditStatus.AUDITING.getValue())
            .to(WmsOutboundAuditStatus.REJECT.getValue())
            .on(WmsOutboundAuditStatus.Event.REJECT)
            .handle(OutboundRejectTransitionHandler.class);

        // 执行
        builder.externalTransition()
            .from(WmsOutboundAuditStatus.PASS.getValue())
            .to(WmsOutboundAuditStatus.FINISHED.getValue())
            .on(WmsOutboundAuditStatus.Event.FINISH)
            .handle(OutboundExecuteTransitionHandler.class);


        // 失败处理
        builder.setFailCallback(OutboundTransitionFailCallback.class);

        return builder.build(OutboundStateMachineConfigure.STATE_MACHINE_NAME);


    }


//
//    /**
//     * 创建与配置状态机
//     **/
//    @Bean(InboundStateMachineConfigure.STATE_MACHINE_NAME)
//    public StateMachine<Integer, WmsInboundAuditStatus.Event, TransitionContext<WmsInboundDO>> inboundActionStateMachine() {
//
//        StateMachineBuilder<Integer, WmsInboundAuditStatus.Event, TransitionContext<WmsInboundDO>> builder = StateMachineBuilderFactory.create();
//
//        // 提交
//        builder.externalTransitions()
//            .fromAmong(WmsInboundAuditStatus.DRAFT.getValue(),WmsInboundAuditStatus.REJECT.getValue())
//            .to( WmsInboundAuditStatus.AUDITING.getValue())
//            .on(WmsInboundAuditStatus.Event.SUBMIT)
//            .handle(InboundSubmitTransitionHandler.class);
//
//        // 废弃
//        builder.externalTransitions()
//            .fromAmong(WmsInboundAuditStatus.DRAFT.getValue(),WmsInboundAuditStatus.REJECT.getValue(),WmsInboundAuditStatus.AUDITING.getValue())
//            .to( WmsInboundAuditStatus.ABANDONED.getValue())
//            .on(WmsInboundAuditStatus.Event.REJECT)
//            .handle(InboundAbandonTransitionHandler.class);
//
//        // 同意
//        builder.externalTransitions()
//            .fromAmong(WmsInboundAuditStatus.AUDITING.getValue())
//            .to(WmsInboundAuditStatus.PASS.getValue())
//            .on(WmsInboundAuditStatus.Event.AGREE)
//            .handle(InboundAgreeTransitionHandler.class);
//
//        // 强制结束
//        builder.externalTransitions()
//            .fromAmong(WmsInboundAuditStatus.AUDITING.getValue())
//            .to(WmsInboundAuditStatus.FORCE_FINISHED.getValue())
//            .on(WmsInboundAuditStatus.Event.FORCE_FINISH)
//            .handle(InboundForceFinishTransitionHandler.class);
//
//        // 拒绝
//        builder.externalTransitions()
//            .fromAmong(WmsInboundAuditStatus.AUDITING.getValue())
//            .to(WmsInboundAuditStatus.REJECT.getValue())
//            .on(WmsInboundAuditStatus.Event.REJECT)
//            .handle(InboundRejectTransitionHandler.class);
//
//        // 错误处理
//        builder.setFailCallback(InboundTransitionFailCallback.class);
//
//        // 返回
//        return builder.build(InboundStateMachineConfigure.STATE_MACHINE_NAME);
//
//    }

//
//    /**
//     * 创建与配置状态机
//     **/
//    @Bean(OutboundStateMachineConfigure.STATE_MACHINE_NAME)
//    public StateMachineWrapper<Integer, WmsOutboundAuditStatus.Event, WmsOutboundDO> inboundActionStateMachine() {
//        StateMachineWrapper<Integer, WmsOutboundAuditStatus.Event, WmsOutboundDO> wrapper = new StateMachineWrapper<>(STATE_MACHINE_NAME, WmsOutboundDO::getAuditStatus);
//
//        // 提交
//        wrapper.bindExternals(
//                // from
//                new Integer[]{
//                    WmsOutboundAuditStatus.DRAFT.getValue(),
//                    WmsOutboundAuditStatus.REJECT.getValue()
//                },
//                // event
//                WmsOutboundAuditStatus.Event.SUBMIT,
//                // to
//                WmsOutboundAuditStatus.AUDITING.getValue(),
//                // handler
//                OutboundSubmitTransitionHandler.class
//            )
//            // 同意
//            .bindExternal(
//                // from
//                WmsOutboundAuditStatus.AUDITING.getValue(),
//                // event
//                WmsOutboundAuditStatus.Event.AGREE,
//                // to
//                WmsOutboundAuditStatus.PASS.getValue(),
//                // handler
//                OutboundAgreeTransitionHandler.class
//            )
//            // 拒绝
//            .bindExternal(
//                // from
//                WmsOutboundAuditStatus.AUDITING.getValue(),
//                // event
//                WmsOutboundAuditStatus.Event.REJECT,
//                // to
//                WmsOutboundAuditStatus.REJECT.getValue(),
//                // handler
//                OutboundRejectTransitionHandler.class
//            )
//            // 执行
//            .bindExternal(
//                // from
//                WmsOutboundAuditStatus.PASS.getValue(),
//                // event
//                WmsOutboundAuditStatus.Event.FINISH,
//                // to
//                WmsOutboundAuditStatus.FINISHED.getValue(),
//                // handler
//                OutboundExecuteTransitionHandler.class
//            )
//            // 失败处理
//            .setFailCallback(OutboundTransitionFailCallback.class)
//        ;
//
//
//        return wrapper;
//    }



}
