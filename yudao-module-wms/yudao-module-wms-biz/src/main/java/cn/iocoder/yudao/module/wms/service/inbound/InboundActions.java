package cn.iocoder.yudao.module.wms.service.inbound;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundAuditStatus;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundStatus;
import cn.iocoder.yudao.module.wms.service.approval.history.ApprovalHistoryAction;
import cn.iocoder.yudao.module.wms.service.quantity.InboundExecutor;
import cn.iocoder.yudao.module.wms.service.quantity.context.InboundContext;
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
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * @author: LeeFJ
 * @date: 2025/3/19 10:00
 * @description: 入库单状态机与动作配置
 */
@Slf4j
@Configuration
public class InboundActions implements StateMachineConfigure<Integer, WmsInboundAuditStatus.Event, ColaContext<WmsInboundDO>>,FailCallback<Integer, WmsInboundAuditStatus.Event, ColaContext<WmsInboundDO>> {

    /**
     * 状态机名称
     **/
    public static final String STATE_MACHINE_NAME = "inboundActionStateMachine";

    /**
     * 提交
     **/
    @Component
    public static class SubmitAction extends BaseInboundAction {

        public SubmitAction() {
            // 指定事件以及前后的状态与状态值提取器
            super(new Integer[]{WmsInboundAuditStatus.DRAFT.getValue(), WmsInboundAuditStatus.REJECT.getValue()}, WmsInboundAuditStatus.AUDITING.getValue(), WmsInboundAuditStatus.Event.SUBMIT);
        }

        @Override
        public boolean when(ColaContext<WmsInboundDO> context) {
            // 检查是否有入库单明细条数
            WmsInboundRespVO inbound = inboundService.getInboundWithItemList(context.data().getId());
            if(CollectionUtils.isEmpty(inbound.getItemList())) {
                throw exception(INBOUND_ITEM_NOT_EXISTS);
            }
            return super.when(context);
        }
    }

    /**
     * 同意
     **/
    @Component
    public static class AgreeAction extends BaseInboundAction {

        @Resource
        private InboundExecutor inboundExecutor;

        public AgreeAction() {
            // 指定事件以及前后的状态与状态值提取器
            super(WmsInboundAuditStatus.AUDITING.getValue(), WmsInboundAuditStatus.PASS.getValue(), WmsInboundAuditStatus.Event.AGREE);
        }

        @Override
        public void perform(Integer from, Integer to, WmsInboundAuditStatus.Event event, ColaContext<WmsInboundDO> context) {
            super.perform(from, to, event, context);
            // 调整库存
            InboundContext inboundContext=new InboundContext();
            inboundContext.setInboundId(context.data().getId());
            inboundExecutor.execute(inboundContext);

        }
    }


    /**
     * 强制完成
     **/
    @Component
    public static class ForceFinishAction extends BaseInboundAction {
        public ForceFinishAction() {
            // 指定事件以及前后的状态与状态值提取器
            super(WmsInboundAuditStatus.AUDITING.getValue(), WmsInboundAuditStatus.REJECT.getValue(), WmsInboundAuditStatus.Event.FORCE_FINISH);
        }

        @Override
        public boolean when(ColaContext<WmsInboundDO> context) {
            WmsInboundStatus inboundStatus = WmsInboundStatus.parse(context.data().getInboundStatus());
            if(inboundStatus.matchAny(WmsInboundStatus.PART)) {
                throw exception(INBOUND_FORCE_FINISH_NOT_ALLOWED);
            }
            return super.when(context);
        }
    }


    /**
     * 拒绝
     **/
    @Component
    public static class RejectAction extends BaseInboundAction {
        public RejectAction() {
            // 指定事件以及前后的状态与状态值提取器
            super(WmsInboundAuditStatus.AUDITING.getValue(), WmsInboundAuditStatus.REJECT.getValue(), WmsInboundAuditStatus.Event.REJECT);
        }
    }


    /**
     * 作废
     **/
    @Component
    public static class AbandonAction extends BaseInboundAction {
        public AbandonAction() {
            // 指定事件以及前后的状态与状态值提取器
            super(new Integer[] {
                WmsInboundAuditStatus.DRAFT.getValue(),
                WmsInboundAuditStatus.REJECT.getValue(),
                WmsInboundAuditStatus.AUDITING.getValue()
            }, WmsInboundAuditStatus.ABANDONED.getValue(), WmsInboundAuditStatus.Event.REJECT);
        }

        @Override
        public boolean when(ColaContext<WmsInboundDO> context) {
            WmsInboundStatus inboundStatus = WmsInboundStatus.parse(context.data().getInboundStatus());
            if(inboundStatus.matchAny(WmsInboundStatus.PART, WmsInboundStatus.ALL)) {
                throw exception(INBOUND_ABANDON_NOT_ALLOWED);
            }
            return super.when(context);
        }
    }


    /**
     * 创建与配置状态机
     **/
    @Bean(InboundActions.STATE_MACHINE_NAME)
    public StateMachineWrapper<Integer, WmsInboundAuditStatus.Event, WmsInboundDO> inboundActionStateMachine() {
        // 创建状态机构建器
        StateMachineBuilder<Integer, WmsInboundAuditStatus.Event, ColaContext<WmsInboundDO>> builder = StateMachineBuilderFactory.create();
        // 初始化状态机状态
        Map<Integer, List<Integer>> conditionMap = this.initActions(builder, BaseInboundAction.class,this);
        // 创建状态机
        StateMachineWrapper<Integer, WmsInboundAuditStatus.Event, WmsInboundDO> machine=new StateMachineWrapper<>(builder.build(InboundActions.STATE_MACHINE_NAME), WmsInboundDO::getAuditStatus);
        // 设置允许的基本操作
        machine.setInitStatus(WmsInboundAuditStatus.DRAFT.getValue());
        machine.setStatusCanEdit(Arrays.asList(WmsInboundAuditStatus.DRAFT.getValue(), WmsInboundAuditStatus.REJECT.getValue()));
        machine.setStatusCanDelete(Arrays.asList(WmsInboundAuditStatus.DRAFT.getValue(), WmsInboundAuditStatus.REJECT.getValue()));
        // 设置状态地图
        machine.setConditionMap(conditionMap);
        return machine;
    }


    /**
     * 状态机失败情况的处理
     **/
    @Override
    public void onFail(Integer to, WmsInboundAuditStatus.Event event, ColaContext<WmsInboundDO> context) {
        // 当前状态
        WmsInboundAuditStatus currStatus= WmsInboundAuditStatus.parse(context.data().getAuditStatus());
        // 允许的可审批状态
        List<Integer> fromList = context.getStateMachineWrapper().getFroms(to);
        List<WmsInboundAuditStatus> fromAuditStatusList = WmsInboundAuditStatus.parse(fromList);
        String fromAuditStatusNames = StrUtils.join(StreamX.from(fromAuditStatusList).toSet(WmsInboundAuditStatus::getLabel));
        // 组装消息
        throw exception(INBOUND_AUDIT_FAIL,currStatus.getLabel(),fromAuditStatusNames,event.getLabel());
    }

    /**
     * InboundAction 基类
     **/
    public static class BaseInboundAction extends ApprovalHistoryAction<WmsInboundAuditStatus.Event, WmsInboundDO> {

        @Resource
        @Lazy
        protected WmsInboundService inboundService;

        public BaseInboundAction(Integer[] from, Integer to, WmsInboundAuditStatus.Event event) {
            super(from, to, WmsInboundDO::getAuditStatus, event);
        }

        public BaseInboundAction(Integer from, Integer to, WmsInboundAuditStatus.Event event) {
            super(new Integer[]{from}, to, WmsInboundDO::getAuditStatus, event);
        }

        /**
         * 默认的条件判断都为通过
         **/
        @Override
        public boolean when(ColaContext<WmsInboundDO> context) {
            return context.success();
        }

        /**
         * 变更状态
         **/
        @Override
        public void perform(Integer from, Integer to, WmsInboundAuditStatus.Event event, ColaContext<WmsInboundDO> context) {
            super.perform(from, to, event, context);
            // 变更状态值
            WmsInboundDO inboundDO = context.data();
            inboundDO.setAuditStatus(to);
            inboundService.updateInboundAuditStatus(inboundDO.getId(),to);
        }

    }


}
