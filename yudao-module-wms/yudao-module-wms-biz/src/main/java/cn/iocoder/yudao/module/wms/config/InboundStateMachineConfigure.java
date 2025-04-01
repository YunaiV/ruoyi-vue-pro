package cn.iocoder.yudao.module.wms.config;

import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundAuditStatus;
import cn.iocoder.yudao.module.wms.service.inbound.transition.*;
import cn.iocoder.yudao.module.wms.service.outbound.transition.BaseOutboundTransition;
import cn.iocoder.yudao.module.wms.statemachine.ColaContext;
import cn.iocoder.yudao.module.wms.statemachine.ColaTransition;
import cn.iocoder.yudao.module.wms.statemachine.StateMachineWrapper;
import com.alibaba.cola.statemachine.builder.FailCallback;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * @author: LeeFJ
 * @date: 2025/3/19 10:00
 * @description: 入库单状态机与动作配置
 */
@Slf4j
@Configuration
public class InboundStateMachineConfigure implements FailCallback<Integer, WmsInboundAuditStatus.Event, ColaContext<WmsInboundDO>> {

    /**
     * 状态机名称
     **/
    public static final String STATE_MACHINE_NAME = "inboundActionStateMachine";

    /**
     * 状态机 Transition 基类
     **/
    private static final Class BASE_TRANSITION_CLASS = BaseInboundTransition.class;

    /**
     * 创建与配置状态机
     **/
    @Bean(InboundStateMachineConfigure.STATE_MACHINE_NAME)
    public StateMachineWrapper<Integer, WmsInboundAuditStatus.Event, WmsInboundDO> inboundActionStateMachine() {
        return new StateMachineWrapper<>(STATE_MACHINE_NAME, BASE_TRANSITION_CLASS, WmsInboundDO::getAuditStatus,this);
    }


    /**
     * 状态机失败情况的处理
     **/
    @Override
    public void onFail(Integer from, WmsInboundAuditStatus.Event event, ColaContext<WmsInboundDO> context) {
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
