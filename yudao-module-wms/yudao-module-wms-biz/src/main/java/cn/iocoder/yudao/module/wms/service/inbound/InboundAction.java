package cn.iocoder.yudao.module.wms.service.inbound;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import cn.iocoder.yudao.module.wms.config.statemachine.ColaAction;
import cn.iocoder.yudao.module.wms.config.statemachine.ColaContext;
import cn.iocoder.yudao.module.wms.config.statemachine.StateMachineConfigure;
import cn.iocoder.yudao.module.wms.config.statemachine.StateMachineWrapper;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalHistorySaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.enums.inbound.InboundStatus;
import cn.iocoder.yudao.module.wms.service.approval.history.ApprovalHistoryAction;
import cn.iocoder.yudao.module.wms.service.approval.history.WmsApprovalHistoryService;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Arrays;
import java.util.function.Function;

/**
 * @author: LeeFJ
 * @date: 2025/3/19 10:00
 * @description:
 */
@Slf4j
@Configuration
public class InboundAction implements StateMachineConfigure<Integer, InboundStatus.Event, ColaContext<WmsInboundDO>> {

    /**
     * 状态机名称
     **/
    public static final String STATE_MACHINE_NAME = "inboundActionStateMachine";


    /**
     * 提交
     **/
    @Component
    public static class Submit extends BaseInboundAction {
        public Submit() {
            // 指定事件以及前后的状态与状态提取器
            super(InboundStatus.DRAFT.getValue(), InboundStatus.AUDIT.getValue(), WmsInboundDO::getStatus, InboundStatus.Event.SUBMIT);
        }
    }

    /**
     * 同意
     **/
    @Component
    public static class Agree extends BaseInboundAction {
        public Agree() {
            // 指定事件以及前后的状态与状态提取器
            super(InboundStatus.AUDIT.getValue(), InboundStatus.PASS.getValue(), WmsInboundDO::getStatus, InboundStatus.Event.AGREE);
        }

        @Override
        public void perform(Integer from, Integer to, InboundStatus.Event event, ColaContext<WmsInboundDO> context) {
            super.perform(from, to, event, context);
            // 调整库存
        }
    }

    /**
     * 拒绝
     **/
    @Component
    public static class Reject extends BaseInboundAction {
        public Reject() {
            // 指定事件以及前后的状态与状态提取器
            super(InboundStatus.AUDIT.getValue(), InboundStatus.REJECT.getValue(), WmsInboundDO::getStatus, InboundStatus.Event.REJECT);
        }
    }



    /**
     * 状态机
     **/
    @Bean(InboundAction.STATE_MACHINE_NAME)
    public StateMachineWrapper<Integer, InboundStatus.Event, WmsInboundDO> inboundActionStateMachine() {
        //
        StateMachineBuilder<Integer, InboundStatus.Event, ColaContext<WmsInboundDO>> builder = StateMachineBuilderFactory.create();
        this.initActions(builder, BaseInboundAction.class);

        StateMachineWrapper<Integer, InboundStatus.Event, WmsInboundDO> machine=new StateMachineWrapper<>(builder.build(InboundAction.STATE_MACHINE_NAME), WmsInboundDO::getStatus);
        // 设置允许的基本操作
        machine.setInitStatus(InboundStatus.DRAFT.getValue());
        machine.setStatusCanEdit(Arrays.asList(InboundStatus.DRAFT.getValue(),InboundStatus.REJECT.getValue()));
        machine.setStatusCanDelete(Arrays.asList(InboundStatus.DRAFT.getValue(),InboundStatus.REJECT.getValue()));
        return machine;
    }

    /**
     * InboundAction 基类
     **/
    public static class BaseInboundAction extends ApprovalHistoryAction<InboundStatus.Event, WmsInboundDO> {

        @Resource
        @Lazy
        protected WmsInboundService inboundService;

        public BaseInboundAction(Integer from, Integer to, Function<WmsInboundDO, Integer> getter, InboundStatus.Event event) {
            super(from, to, getter, event);
        }

        @Override
        public boolean when(ColaContext<WmsInboundDO> context) {
            return context.success();
        }

        /**
         * 变更状态
         **/
        @Override
        public void perform(Integer from, Integer to, InboundStatus.Event event, ColaContext<WmsInboundDO> context) {
            super.perform(from, to, event, context);
            WmsInboundDO inboundDO = context.data();
            inboundDO.setStatus(to);
            inboundService.updateInboundStatus(inboundDO.getId(),to);
        }

    }


}
