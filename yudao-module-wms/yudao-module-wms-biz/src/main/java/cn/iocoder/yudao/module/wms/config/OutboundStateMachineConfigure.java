package cn.iocoder.yudao.module.wms.config;

import cn.iocoder.yudao.framework.cola.statemachine.StateMachineWrapper;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import cn.iocoder.yudao.module.wms.service.outbound.transition.*;
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
    public static final String STATE_MACHINE_NAME = "outboundActionStateMachine";
    /**
     * 创建与配置状态机
     **/
    @Bean(OutboundStateMachineConfigure.STATE_MACHINE_NAME)
    public StateMachineWrapper<Integer, WmsOutboundAuditStatus.Event, WmsOutboundDO> inboundActionStateMachine() {
        StateMachineWrapper<Integer, WmsOutboundAuditStatus.Event, WmsOutboundDO> wrapper = new StateMachineWrapper<>(STATE_MACHINE_NAME, WmsOutboundDO::getAuditStatus);

        // 提交
        wrapper.bindExternals(
                // from
                new Integer[]{
                    WmsOutboundAuditStatus.DRAFT.getValue(),
                    WmsOutboundAuditStatus.REJECT.getValue()
                },
                // event
                WmsOutboundAuditStatus.Event.SUBMIT,
                // to
                WmsOutboundAuditStatus.AUDITING.getValue(),
                // handler
                OutboundSubmitTransitionHandler.class
            )
            // 同意
            .bindExternal(
                // from
                WmsOutboundAuditStatus.AUDITING.getValue(),
                // event
                WmsOutboundAuditStatus.Event.AGREE,
                // to
                WmsOutboundAuditStatus.PASS.getValue(),
                // handler
                OutboundAgreeTransitionHandler.class
            )
            // 拒绝
            .bindExternal(
                // from
                WmsOutboundAuditStatus.AUDITING.getValue(),
                // event
                WmsOutboundAuditStatus.Event.REJECT,
                // to
                WmsOutboundAuditStatus.REJECT.getValue(),
                // handler
                OutboundRejectTransitionHandler.class
            )
            // 执行
            .bindExternal(
                // from
                WmsOutboundAuditStatus.PASS.getValue(),
                // event
                WmsOutboundAuditStatus.Event.FINISH,
                // to
                WmsOutboundAuditStatus.FINISHED.getValue(),
                // handler
                OutboundExecuteTransitionHandler.class
            )
            // 失败处理
            .setFailCallback(OutboundTransitionFailCallback.class)
        ;


        return wrapper;
    }



}
