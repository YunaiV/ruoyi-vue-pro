package cn.iocoder.yudao.module.wms.service.outbound;

import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import cn.iocoder.yudao.module.wms.service.approval.history.ApprovalHistoryTransition;
import cn.iocoder.yudao.module.wms.service.quantity.OutboundFinishExecutor;
import cn.iocoder.yudao.module.wms.service.quantity.OutboundRejectExecutor;
import cn.iocoder.yudao.module.wms.service.quantity.OutboundSubmitExecutor;
import cn.iocoder.yudao.module.wms.service.quantity.context.OutboundContext;
import cn.iocoder.yudao.module.wms.statemachine.ColaTransition;
import cn.iocoder.yudao.module.wms.statemachine.ColaContext;
import cn.iocoder.yudao.module.wms.statemachine.StateMachineConfigure;
import cn.iocoder.yudao.module.wms.statemachine.StateMachineWrapper;
import com.alibaba.cola.statemachine.builder.FailCallback;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
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
public class OutboundTransitions implements StateMachineConfigure<Integer, WmsOutboundAuditStatus.Event, ColaContext<WmsOutboundDO>> , FailCallback<Integer, WmsOutboundAuditStatus.Event, ColaContext<WmsOutboundDO>> {

    /**
     * 状态机名称
     **/
    public static final String STATE_MACHINE_NAME = "outboundActionStateMachine";

    /**
     * 提交
     **/
    @Component
    public static class SubmitTransition extends BaseOutboundTransition {

        @Resource
        private OutboundSubmitExecutor outboundSubmitExecutor;

        public SubmitTransition() {
            // 指定事件以及前后的状态
            super(
                // from
                new WmsOutboundAuditStatus[]{
                    WmsOutboundAuditStatus.DRAFT,
                    WmsOutboundAuditStatus.REJECT
                },
                // to
                WmsOutboundAuditStatus.AUDITING,
                // event
                WmsOutboundAuditStatus.Event.SUBMIT
            );
        }

        @Override
        public void perform(Integer from, Integer to, WmsOutboundAuditStatus.Event event, ColaContext<WmsOutboundDO> context) {
            super.perform(from, to, event, context);
            // 调整库存
            OutboundContext outboundContext = new OutboundContext();
            outboundContext.setOutboundId(context.data().getId());
            outboundSubmitExecutor.execute(outboundContext);
        }
    }

    /**
     * 同意
     **/
    @Component
    public static class AgreeTransition extends BaseOutboundTransition {

        public AgreeTransition() {
            // 指定事件以及前后的状态
            super(
                // from
                WmsOutboundAuditStatus.AUDITING,
                // to
                WmsOutboundAuditStatus.PASS,
                // event
                WmsOutboundAuditStatus.Event.AGREE
            );
        }

    }


    /**
     * 同意
     **/
    @Component
    public static class OutboundTransition extends BaseOutboundTransition {

        @Resource
        private OutboundFinishExecutor outboundFinishExecutor;

        public OutboundTransition() {
            // 指定事件以及前后的状态
            super(
                // from
                WmsOutboundAuditStatus.PASS,
                // to
                WmsOutboundAuditStatus.FINISHED,
                // event
                WmsOutboundAuditStatus.Event.FINISH
            );
        }

        @Override
        public void perform(Integer from, Integer to, WmsOutboundAuditStatus.Event event, ColaContext<WmsOutboundDO> context) {
            super.perform(from, to, event, context);
            // 调整库存
            OutboundContext outboundContext = new OutboundContext();
            outboundContext.setOutboundId(context.data().getId());
            outboundFinishExecutor.execute(outboundContext);

        }
    }


    /**
     * 拒绝
     **/
    @Component
    public static class RejectTransition extends BaseOutboundTransition {

        @Resource
        private OutboundRejectExecutor outboundRejectExecutor;

        public RejectTransition() {
            // 指定事件以及前后的状态
            super(
                // from
                WmsOutboundAuditStatus.AUDITING,
                // to
                WmsOutboundAuditStatus.REJECT,
                // event
                WmsOutboundAuditStatus.Event.REJECT
            );
        }

        @Override
        public void perform(Integer from, Integer to, WmsOutboundAuditStatus.Event event, ColaContext<WmsOutboundDO> context) {
            super.perform(from, to, event, context);
            // 调整库存
            OutboundContext outboundContext = new OutboundContext();
            outboundContext.setOutboundId(context.data().getId());
            outboundRejectExecutor.execute(outboundContext);
        }


    }


    /**
     * 创建与配置状态机
     **/
    @Bean(OutboundTransitions.STATE_MACHINE_NAME)
    public StateMachineWrapper<Integer, WmsOutboundAuditStatus.Event, WmsOutboundDO> inboundActionStateMachine() {
        //  创建状态机构建器
        StateMachineBuilder<Integer, WmsOutboundAuditStatus.Event, ColaContext<WmsOutboundDO>> builder = StateMachineBuilderFactory.create();
        // 初始化状态机状态
        List<ColaTransition<Integer, WmsOutboundAuditStatus.Event, ColaContext<WmsOutboundDO>>> colaTransitions = this.initActions(builder, BaseOutboundTransition.class, this);
        // 创建状态机
        StateMachineWrapper<Integer, WmsOutboundAuditStatus.Event, WmsOutboundDO> machine = new StateMachineWrapper<>(builder.build(OutboundTransitions.STATE_MACHINE_NAME), colaTransitions, WmsOutboundDO::getAuditStatus);
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

    /**
     * OutboundAction 基类
     **/
    public static class BaseOutboundTransition extends ApprovalHistoryTransition<WmsOutboundAuditStatus.Event, WmsOutboundDO> {

        @Resource
        @Lazy
        protected WmsOutboundService outboundService;

        /**
         * 多个 from 的构造函数
         **/
        public BaseOutboundTransition(WmsOutboundAuditStatus[] from, WmsOutboundAuditStatus to, WmsOutboundAuditStatus.Event event) {
            super(Arrays.stream(from).map(WmsOutboundAuditStatus::getValue).toArray(Integer[]::new), to.getValue(), WmsOutboundDO::getAuditStatus, event);
        }

        /**
         * 单个 from 的构造函数
         **/
        public BaseOutboundTransition(WmsOutboundAuditStatus from, WmsOutboundAuditStatus to, WmsOutboundAuditStatus.Event event) {
            super(new Integer[]{from.getValue()}, to.getValue(), WmsOutboundDO::getAuditStatus, event);
        }

        /**
         * 变更状态
         **/
        @Override
        public void perform(Integer from, Integer to, WmsOutboundAuditStatus.Event event, ColaContext<WmsOutboundDO> context) {
            super.perform(from, to, event, context);
            WmsOutboundDO inboundDO = context.data();
            inboundDO.setAuditStatus(to);
            outboundService.updateOutboundAuditStatus(inboundDO.getId(),to);
        }

    }


}
