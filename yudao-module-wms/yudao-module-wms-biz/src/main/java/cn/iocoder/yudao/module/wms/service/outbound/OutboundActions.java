package cn.iocoder.yudao.module.wms.service.outbound;

import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import cn.iocoder.yudao.module.wms.service.approval.history.ApprovalHistoryAction;
import cn.iocoder.yudao.module.wms.service.quantity.OutboundFinishExecutor;
import cn.iocoder.yudao.module.wms.service.quantity.OutboundRejectExecutor;
import cn.iocoder.yudao.module.wms.service.quantity.OutboundSubmitExecutor;
import cn.iocoder.yudao.module.wms.service.quantity.context.OutboundContext;
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
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.OUTBOUND_AUDIT_ERROR;

/**
 * @author: LeeFJ
 * @date: 2025/3/19 10:00
 * @description: 出库单状态机与动作配置
 */
@Slf4j
@Configuration
public class OutboundActions implements StateMachineConfigure<Integer, WmsOutboundAuditStatus.Event, ColaContext<WmsOutboundDO>> , FailCallback<Integer, WmsOutboundAuditStatus.Event, ColaContext<WmsOutboundDO>> {

    /**
     * 状态机名称
     **/
    public static final String STATE_MACHINE_NAME = "outboundActionStateMachine";
    /**
     * 错误消息
     **/
    public static final String AUDIT_ERROR_MESSAGE = "审核错误，当前出库单状态为%s，在%s状态时才允许%s";

    /**
     * 提交
     **/
    @Component
    public static class SubmitAction extends BaseOutboundAction {

        @Resource
        private OutboundSubmitExecutor outboundSubmitExecutor;

        public SubmitAction() {
            // 指定事件以及前后的状态与状态值提取器
            super(new Integer[]{WmsOutboundAuditStatus.DRAFT.getValue(), WmsOutboundAuditStatus.REJECT.getValue()}, WmsOutboundAuditStatus.AUDITING.getValue(), WmsOutboundAuditStatus.Event.SUBMIT);
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
    public static class AgreeAction extends BaseOutboundAction {

        public AgreeAction() {
            // 指定事件以及前后的状态与状态值提取器
            super(WmsOutboundAuditStatus.AUDITING.getValue(), WmsOutboundAuditStatus.PASS.getValue(), WmsOutboundAuditStatus.Event.AGREE);
        }

    }


    /**
     * 同意
     **/
    @Component
    public static class OutboundAction extends BaseOutboundAction {

        @Resource
        private OutboundFinishExecutor outboundFinishExecutor;

        public OutboundAction() {
            // 指定事件以及前后的状态与状态值提取器
            super(WmsOutboundAuditStatus.PASS.getValue(), WmsOutboundAuditStatus.FINISHED.getValue(), WmsOutboundAuditStatus.Event.FINISH);
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
    public static class RejectAction extends BaseOutboundAction {

        @Resource
        private OutboundRejectExecutor outboundRejectExecutor;

        public RejectAction() {
            // 指定事件以及前后的状态与状态值提取器
            super(WmsOutboundAuditStatus.AUDITING.getValue(), WmsOutboundAuditStatus.REJECT.getValue(), WmsOutboundAuditStatus.Event.REJECT);
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
    @Bean(OutboundActions.STATE_MACHINE_NAME)
    public StateMachineWrapper<Integer, WmsOutboundAuditStatus.Event, WmsOutboundDO> inboundActionStateMachine() {
        //  创建状态机构建器
        StateMachineBuilder<Integer, WmsOutboundAuditStatus.Event, ColaContext<WmsOutboundDO>> builder = StateMachineBuilderFactory.create();
        // 初始化状态机状态
        Map<Integer, List<Integer>> conditionMap = this.initActions(builder, BaseOutboundAction.class,this);
        // 创建状态机
        StateMachineWrapper<Integer, WmsOutboundAuditStatus.Event, WmsOutboundDO> machine=new StateMachineWrapper<>(builder.build(OutboundActions.STATE_MACHINE_NAME), WmsOutboundDO::getAuditStatus);
        // 设置允许的基本操作
        machine.setInitStatus(WmsOutboundAuditStatus.DRAFT.getValue());
        machine.setStatusCanEdit(Arrays.asList(WmsOutboundAuditStatus.DRAFT.getValue(), WmsOutboundAuditStatus.REJECT.getValue()));
        machine.setStatusCanDelete(Arrays.asList(WmsOutboundAuditStatus.DRAFT.getValue(), WmsOutboundAuditStatus.REJECT.getValue()));
        // 设置状态地图
        machine.setConditionMap(conditionMap);
        return machine;
    }


    /**
     * 处理失败的情况
     **/
    @Override
    public void onFail(Integer to, WmsOutboundAuditStatus.Event event, ColaContext<WmsOutboundDO> context) {

        // 当前状态
        WmsOutboundAuditStatus currStatus= WmsOutboundAuditStatus.parse(context.data().getAuditStatus());
        // 允许的可审批状态
        List<Integer> fromList = context.getStateMachineWrapper().getFroms(to);
        List<WmsOutboundAuditStatus> fromAuditStatusList = WmsOutboundAuditStatus.parse(fromList);
        String fromAuditStatusNames = StrUtils.join(StreamX.from(fromAuditStatusList).toSet(WmsOutboundAuditStatus::getLabel));
        // 组装消息
        String message=String.format(AUDIT_ERROR_MESSAGE,currStatus.getLabel(),fromAuditStatusNames,event.getLabel());
        throw exception(OUTBOUND_AUDIT_ERROR,message);

    }

    /**
     * OutboundAction 基类
     **/
    public static class BaseOutboundAction extends ApprovalHistoryAction<WmsOutboundAuditStatus.Event, WmsOutboundDO> {

        @Resource
        @Lazy
        protected WmsOutboundService outboundService;

        public BaseOutboundAction(Integer[] from, Integer to, WmsOutboundAuditStatus.Event event) {
            super(from, to, WmsOutboundDO::getAuditStatus, event);
        }

        public BaseOutboundAction(Integer from, Integer to, WmsOutboundAuditStatus.Event event) {
            super(new Integer[]{from}, to, WmsOutboundDO::getAuditStatus, event);
        }

        @Override
        public boolean when(ColaContext<WmsOutboundDO> context) {
            return context.success();
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
