package cn.iocoder.yudao.module.wms.service.inbound;

import cn.iocoder.yudao.module.wms.config.statemachine.ColaContext;
import cn.iocoder.yudao.module.wms.config.statemachine.StateMachineConfigure;
import cn.iocoder.yudao.module.wms.config.statemachine.StateMachineWrapper;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.enums.inbound.InboundAuditStatus;
import cn.iocoder.yudao.module.wms.service.approval.history.ApprovalHistoryAction;
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
public class InboundAction implements StateMachineConfigure<Integer, InboundAuditStatus.Event, ColaContext<WmsInboundDO>> {

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
            super(InboundAuditStatus.DRAFT.getValue(), InboundAuditStatus.AUDITING.getValue(), WmsInboundDO::getAuditStatus, InboundAuditStatus.Event.SUBMIT);
        }
    }

    /**
     * 同意
     **/
    @Component
    public static class Agree extends BaseInboundAction {

        @Resource
        private WmsStockWarehouseService stockWarehouseService;

        public Agree() {
            // 指定事件以及前后的状态与状态提取器
            super(InboundAuditStatus.AUDITING.getValue(), InboundAuditStatus.PASS.getValue(), WmsInboundDO::getAuditStatus, InboundAuditStatus.Event.AGREE);
        }

        @Override
        public void perform(Integer from, Integer to, InboundAuditStatus.Event event, ColaContext<WmsInboundDO> context) {
            super.perform(from, to, event, context);
            // 调整库存
            WmsInboundRespVO inboundRespVO=inboundService.getInboundWithItemList(context.data().getId());
            stockWarehouseService.inbound(inboundRespVO);

        }
    }

    /**
     * 拒绝
     **/
    @Component
    public static class Reject extends BaseInboundAction {
        public Reject() {
            // 指定事件以及前后的状态与状态提取器
            super(InboundAuditStatus.AUDITING.getValue(), InboundAuditStatus.REJECT.getValue(), WmsInboundDO::getAuditStatus, InboundAuditStatus.Event.REJECT);
        }
    }



    /**
     * 状态机
     **/
    @Bean(InboundAction.STATE_MACHINE_NAME)
    public StateMachineWrapper<Integer, InboundAuditStatus.Event, WmsInboundDO> inboundActionStateMachine() {
        //
        StateMachineBuilder<Integer, InboundAuditStatus.Event, ColaContext<WmsInboundDO>> builder = StateMachineBuilderFactory.create();
        this.initActions(builder, BaseInboundAction.class);

        StateMachineWrapper<Integer, InboundAuditStatus.Event, WmsInboundDO> machine=new StateMachineWrapper<>(builder.build(InboundAction.STATE_MACHINE_NAME), WmsInboundDO::getAuditStatus);
        // 设置允许的基本操作
        machine.setInitStatus(InboundAuditStatus.DRAFT.getValue());
        machine.setStatusCanEdit(Arrays.asList(InboundAuditStatus.DRAFT.getValue(), InboundAuditStatus.REJECT.getValue()));
        machine.setStatusCanDelete(Arrays.asList(InboundAuditStatus.DRAFT.getValue(), InboundAuditStatus.REJECT.getValue()));
        return machine;
    }

    /**
     * InboundAction 基类
     **/
    public static class BaseInboundAction extends ApprovalHistoryAction<InboundAuditStatus.Event, WmsInboundDO> {

        @Resource
        @Lazy
        protected WmsInboundService inboundService;

        public BaseInboundAction(Integer from, Integer to, Function<WmsInboundDO, Integer> getter, InboundAuditStatus.Event event) {
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
        public void perform(Integer from, Integer to, InboundAuditStatus.Event event, ColaContext<WmsInboundDO> context) {
            super.perform(from, to, event, context);
            WmsInboundDO inboundDO = context.data();
            inboundDO.setAuditStatus(to);
            inboundService.updateInboundAuditStatus(inboundDO.getId(),to);
        }

    }


}
