package cn.iocoder.yudao.module.wms.config;

import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundAuditStatus;
import cn.iocoder.yudao.module.wms.service.inbound.transition.BaseInboundTransitionHandler;
import cn.iocoder.yudao.module.wms.service.inbound.transition.InboundAbandonTransitionHandler;
import cn.iocoder.yudao.module.wms.service.inbound.transition.InboundAgreeTransitionHandler;
import cn.iocoder.yudao.module.wms.statemachine.TransitionContext;
import cn.iocoder.yudao.module.wms.statemachine.StateMachineWrapper;
import com.alibaba.cola.statemachine.builder.FailCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INBOUND_AUDIT_FAIL;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INBOUND_STATUS_PARSE_ERROR;

/**
 * @author: LeeFJ
 * @date: 2025/3/19 10:00
 * @description: 入库单状态机与动作配置
 */
@Slf4j
@Configuration
public class InboundStateMachineConfigure implements FailCallback<Integer, WmsInboundAuditStatus.Event, TransitionContext<WmsInboundDO>> {

    /**
     * 状态机名称
     **/
    public static final String STATE_MACHINE_NAME = "inboundActionStateMachine";

    /**
     * 状态机 Transition 基类
     **/
    private static final Class BASE_TRANSITION_CLASS = BaseInboundTransitionHandler.class;

    /**
     * 创建与配置状态机
     **/
    @Bean(InboundStateMachineConfigure.STATE_MACHINE_NAME)
    public StateMachineWrapper<Integer, WmsInboundAuditStatus.Event, WmsInboundDO> inboundActionStateMachine() {

        StateMachineWrapper<Integer, WmsInboundAuditStatus.Event, WmsInboundDO> wrapper = new StateMachineWrapper<>(STATE_MACHINE_NAME, BASE_TRANSITION_CLASS, WmsInboundDO::getAuditStatus, this);

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
                InboundAbandonTransitionHandler.class
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
                InboundAgreeTransitionHandler.class
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
                InboundAgreeTransitionHandler.class
            )
        ;


        return wrapper;
    }


    /**
     * 状态机失败情况的处理
     **/
    @Override
    public void onFail(Integer from, WmsInboundAuditStatus.Event event, TransitionContext<WmsInboundDO> context) {
        // 当前状态
        WmsInboundAuditStatus currStatus= WmsInboundAuditStatus.parse(context.data().getAuditStatus());
        if (currStatus == null) {
            throw exception(INBOUND_STATUS_PARSE_ERROR);
        }
        // 目标状态
        Integer to = context.getTo(from, event);
        WmsInboundAuditStatus toStatus = WmsInboundAuditStatus.parse(to);
        if (toStatus == null) {
            throw exception(INBOUND_STATUS_PARSE_ERROR);
        }
        // 组装消息
        throw exception(INBOUND_AUDIT_FAIL, currStatus.getLabel(), toStatus.getLabel(), event.getLabel());
    }




}
