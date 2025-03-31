package cn.iocoder.yudao.module.wms.config;

import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import cn.iocoder.yudao.module.wms.service.outbound.transition.BaseOutboundTransition;
import cn.iocoder.yudao.module.wms.statemachine.ColaTransition;
import cn.iocoder.yudao.module.wms.statemachine.ColaContext;
import cn.iocoder.yudao.module.wms.statemachine.StateMachineConfigure;
import cn.iocoder.yudao.module.wms.statemachine.StateMachineWrapper;
import com.alibaba.cola.statemachine.builder.FailCallback;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.OUTBOUND_AUDIT_FAIL;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.OUTBOUND_STATUS_PARSE_ERROR;

/**
 * @author: LeeFJ
 * @date: 2025/3/19 10:00
 * @description: 出库单状态机与动作配置
 */
@Slf4j
@Configuration
public class OutboundStateMachineConfigure implements StateMachineConfigure<Integer, WmsOutboundAuditStatus.Event, ColaContext<WmsOutboundDO>> , FailCallback<Integer, WmsOutboundAuditStatus.Event, ColaContext<WmsOutboundDO>> {

    /**
     * 状态机名称
     **/
    public static final String STATE_MACHINE_NAME = "outboundActionStateMachine";

    /**
     * 创建与配置状态机
     **/
    @Bean(OutboundStateMachineConfigure.STATE_MACHINE_NAME)
    public StateMachineWrapper<Integer, WmsOutboundAuditStatus.Event, WmsOutboundDO> inboundActionStateMachine() {
        //  创建状态机构建器
        StateMachineBuilder<Integer, WmsOutboundAuditStatus.Event, ColaContext<WmsOutboundDO>> builder = StateMachineBuilderFactory.create();
        // 初始化状态机状态
        List<ColaTransition<Integer, WmsOutboundAuditStatus.Event, ColaContext<WmsOutboundDO>>> colaTransitions = this.initActions(builder, BaseOutboundTransition.class, this);
        // 创建状态机
        StateMachineWrapper<Integer, WmsOutboundAuditStatus.Event, WmsOutboundDO> machine = new StateMachineWrapper<>(builder.build(OutboundStateMachineConfigure.STATE_MACHINE_NAME), colaTransitions, WmsOutboundDO::getAuditStatus);
        // 设置允许的基本操作
        // machine.setInitStatus(WmsOutboundAuditStatus.DRAFT.getValue());
        // machine.setStatusCanEdit(Arrays.asList(WmsOutboundAuditStatus.DRAFT.getValue(), WmsOutboundAuditStatus.REJECT.getValue()));
        // machine.setStatusCanDelete(Arrays.asList(WmsOutboundAuditStatus.DRAFT.getValue(), WmsOutboundAuditStatus.REJECT.getValue()));
        return machine;
    }


    /**
     * 处理失败的情况
     **/
    @Override
    public void onFail(Integer from, WmsOutboundAuditStatus.Event event, ColaContext<WmsOutboundDO> context) {
        // 当前状态
        WmsOutboundAuditStatus currStatus= WmsOutboundAuditStatus.parse(context.data().getAuditStatus());
        if (currStatus == null) {
            throw exception(OUTBOUND_STATUS_PARSE_ERROR);
        }
        // 目标状态
        Integer to = context.getTo(from, event);
        WmsOutboundAuditStatus toAuditStatus = WmsOutboundAuditStatus.parse(to);
        if (toAuditStatus == null) {
            throw exception(OUTBOUND_STATUS_PARSE_ERROR);
        }
        // 组装消息
        throw exception(OUTBOUND_AUDIT_FAIL, currStatus.getLabel(), toAuditStatus.getLabel(), event.getLabel());
    }




}
