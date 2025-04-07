package cn.iocoder.yudao.module.wms.config;

import cn.iocoder.yudao.framework.cola.statemachine.StateMachineWrapper;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundAuditStatus;
import cn.iocoder.yudao.module.wms.service.inbound.transition.InboundAbandonTransitionHandler;
import cn.iocoder.yudao.module.wms.service.inbound.transition.InboundAgreeTransitionHandler;
import cn.iocoder.yudao.module.wms.service.inbound.transition.InboundForceFinishTransitionHandler;
import cn.iocoder.yudao.module.wms.service.inbound.transition.InboundRejectTransitionHandler;
import cn.iocoder.yudao.module.wms.service.inbound.transition.InboundSubmitTransitionHandler;
import cn.iocoder.yudao.module.wms.service.inbound.transition.InboundTransitionFailCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: LeeFJ
 * @date: 2025/3/19 10:00
 * @description: 入库单状态机与动作配置
 */
@Slf4j
@Configuration
public class InboundStateMachineConfigure {

    /**
     * 状态机名称
     **/
    public static final String STATE_MACHINE_NAME = "inboundActionStateMachine";


    /**
     * 创建与配置状态机
     **/
    @Bean(InboundStateMachineConfigure.STATE_MACHINE_NAME)
    public StateMachineWrapper<Integer, WmsInboundAuditStatus.Event, WmsInboundDO> inboundActionStateMachine() {

        StateMachineWrapper<Integer, WmsInboundAuditStatus.Event, WmsInboundDO> wrapper = new StateMachineWrapper<>(STATE_MACHINE_NAME, WmsInboundDO::getAuditStatus);

        // 提交
        wrapper.bindExternals(
                // from
                new Integer[]{
                    WmsInboundAuditStatus.DRAFT.getValue(),
                    WmsInboundAuditStatus.REJECT.getValue()
                },
                // event
                WmsInboundAuditStatus.Event.SUBMIT,
                // to
                WmsInboundAuditStatus.AUDITING.getValue(),
                // handler
                InboundSubmitTransitionHandler.class
            )
            // 废弃
            .bindExternals(
                // from
                new Integer[]{
                    WmsInboundAuditStatus.DRAFT.getValue(),
                    WmsInboundAuditStatus.REJECT.getValue(),
                    WmsInboundAuditStatus.AUDITING.getValue()
                },
                // event
                WmsInboundAuditStatus.Event.REJECT,
                // to
                WmsInboundAuditStatus.ABANDONED.getValue(),
                // handler
                InboundAbandonTransitionHandler.class
            )
            // 同意
            .bindExternal(
                // from
                WmsInboundAuditStatus.AUDITING.getValue(),
                // event
                WmsInboundAuditStatus.Event.AGREE,
                // to
                WmsInboundAuditStatus.PASS.getValue(),
                // handler
                InboundAgreeTransitionHandler.class
            )
            // 强制结束
            .bindExternal(
                // from
                WmsInboundAuditStatus.AUDITING.getValue(),
                // event
                WmsInboundAuditStatus.Event.FORCE_FINISH,
                // to
                WmsInboundAuditStatus.FORCE_FINISHED.getValue(),
                // handler
                InboundForceFinishTransitionHandler.class
            )
            // 拒绝
            .bindExternal(
                // from
                WmsInboundAuditStatus.AUDITING.getValue(),
                // event
                WmsInboundAuditStatus.Event.REJECT,
                // to
                WmsInboundAuditStatus.REJECT.getValue(),
                // handler
                InboundRejectTransitionHandler.class
            )
            // 失败处理
            .setFailCallback(InboundTransitionFailCallback.class)
        ;


        return wrapper;
    }


}
