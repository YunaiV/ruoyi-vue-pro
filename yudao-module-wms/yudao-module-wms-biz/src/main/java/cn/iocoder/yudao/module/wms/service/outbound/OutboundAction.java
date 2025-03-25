package cn.iocoder.yudao.module.wms.service.outbound;

import cn.iocoder.yudao.module.wms.config.statemachine.ColaContext;
import cn.iocoder.yudao.module.wms.config.statemachine.StateMachineConfigure;
import cn.iocoder.yudao.module.wms.config.statemachine.StateMachineWrapper;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.module.wms.enums.outbound.OutboundAuditStatus;
import cn.iocoder.yudao.module.wms.service.approval.history.ApprovalHistoryAction;
import cn.iocoder.yudao.module.wms.service.quantity.OutboundAgreeExecutor;
import cn.iocoder.yudao.module.wms.service.quantity.OutboundRejectExecutor;
import cn.iocoder.yudao.module.wms.service.quantity.OutboundSubmitExecutor;
import cn.iocoder.yudao.module.wms.service.quantity.context.OutboundContext;
import cn.iocoder.yudao.module.wms.service.stock.warehouse.WmsStockWarehouseService;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.function.Function;

/**
 * @author: LeeFJ
 * @date: 2025/3/19 10:00
 * @description:
 */
@Slf4j
@Configuration
public class OutboundAction implements StateMachineConfigure<Integer, OutboundAuditStatus.Event, ColaContext<WmsOutboundDO>> {

    /**
     * 状态机名称
     **/
    public static final String STATE_MACHINE_NAME = "outboundActionStateMachine";


    /**
     * 提交
     **/
    @Component
    public static class Submit extends BaseOutboundAction {

        @Resource
        private OutboundSubmitExecutor outboundSubmitExecutor;

        public Submit() {
            // 指定事件以及前后的状态与状态提取器
            super(OutboundAuditStatus.DRAFT.getValue(), OutboundAuditStatus.AUDITING.getValue(), WmsOutboundDO::getAuditStatus, OutboundAuditStatus.Event.SUBMIT);
        }

        @Override
        public void perform(Integer from, Integer to, OutboundAuditStatus.Event event, ColaContext<WmsOutboundDO> context) {
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
    public static class Agree extends BaseOutboundAction {

        @Resource
        private OutboundAgreeExecutor outboundAgreeExecutor;

        public Agree() {
            // 指定事件以及前后的状态与状态提取器
            super(OutboundAuditStatus.AUDITING.getValue(), OutboundAuditStatus.PASS.getValue(), WmsOutboundDO::getAuditStatus, OutboundAuditStatus.Event.AGREE);
        }

        @Override
        public void perform(Integer from, Integer to, OutboundAuditStatus.Event event, ColaContext<WmsOutboundDO> context) {
            super.perform(from, to, event, context);
            // 调整库存
            OutboundContext outboundContext = new OutboundContext();
            outboundContext.setOutboundId(context.data().getId());
            outboundAgreeExecutor.execute(outboundContext);

        }
    }

    /**
     * 拒绝
     **/
    @Component
    public static class Reject extends BaseOutboundAction {



        @Resource
        private OutboundRejectExecutor outboundRejectExecutor;

        public Reject() {
            // 指定事件以及前后的状态与状态提取器
            super(OutboundAuditStatus.AUDITING.getValue(), OutboundAuditStatus.REJECT.getValue(), WmsOutboundDO::getAuditStatus, OutboundAuditStatus.Event.REJECT);
        }

        @Override
        public void perform(Integer from, Integer to, OutboundAuditStatus.Event event, ColaContext<WmsOutboundDO> context) {
            super.perform(from, to, event, context);
            // 调整库存
            OutboundContext outboundContext = new OutboundContext();
            outboundContext.setOutboundId(context.data().getId());
            outboundRejectExecutor.execute(outboundContext);
        }
    }



    /**
     * 状态机
     **/
    @Bean(OutboundAction.STATE_MACHINE_NAME)
    public StateMachineWrapper<Integer, OutboundAuditStatus.Event, WmsOutboundDO> inboundActionStateMachine() {
        //
        StateMachineBuilder<Integer, OutboundAuditStatus.Event, ColaContext<WmsOutboundDO>> builder = StateMachineBuilderFactory.create();
        this.initActions(builder, BaseOutboundAction.class);

        StateMachineWrapper<Integer, OutboundAuditStatus.Event, WmsOutboundDO> machine=new StateMachineWrapper<>(builder.build(OutboundAction.STATE_MACHINE_NAME), WmsOutboundDO::getAuditStatus);
        // 设置允许的基本操作
        machine.setInitStatus(OutboundAuditStatus.DRAFT.getValue());
        machine.setStatusCanEdit(Arrays.asList(OutboundAuditStatus.DRAFT.getValue(), OutboundAuditStatus.REJECT.getValue()));
        machine.setStatusCanDelete(Arrays.asList(OutboundAuditStatus.DRAFT.getValue(), OutboundAuditStatus.REJECT.getValue()));
        return machine;
    }

    /**
     * InboundAction 基类
     **/
    public static class BaseOutboundAction extends ApprovalHistoryAction<OutboundAuditStatus.Event, WmsOutboundDO> {

        @Resource
        @Lazy
        protected WmsOutboundService outboundService;

        public BaseOutboundAction(Integer from, Integer to, Function<WmsOutboundDO, Integer> getter, OutboundAuditStatus.Event event) {
            super(from, to, getter, event);
        }

        @Override
        public boolean when(ColaContext<WmsOutboundDO> context) {
            return context.success();
        }

        /**
         * 变更状态
         **/
        @Override
        public void perform(Integer from, Integer to, OutboundAuditStatus.Event event, ColaContext<WmsOutboundDO> context) {
            super.perform(from, to, event, context);
            WmsOutboundDO inboundDO = context.data();
            inboundDO.setAuditStatus(to);
            outboundService.updateOutboundAuditStatus(inboundDO.getId(),to);
        }

    }


}
