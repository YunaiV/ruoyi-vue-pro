package cn.iocoder.yudao.module.wms.config;

import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.cola.statemachine.builder.StateMachineBuilder;
import cn.iocoder.yudao.framework.cola.statemachine.builder.StateMachineBuilderFactory;
import cn.iocoder.yudao.framework.cola.statemachine.builder.TransitionContext;
import cn.iocoder.yudao.module.wms.dal.dataobject.stockcheck.WmsStockCheckDO;
import cn.iocoder.yudao.module.wms.enums.stock.check.WmsStockCheckAuditStatus;
import cn.iocoder.yudao.module.wms.service.stockcheck.transition.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author: LeeFJ
 * @date: 2025/3/19 10:00
 * @description: 盘点单状态机与动作配置
 */
@Slf4j
@Configuration
public class StockCheckStateMachineConfigure {

    /**
     * 状态机名称
     **/
    public static final String STATE_MACHINE_NAME = "stockCheckStateMachine";

    /**
     * 创建与配置状态机
     **/
    @Bean(StockCheckStateMachineConfigure.STATE_MACHINE_NAME)
    public StateMachine<Integer, WmsStockCheckAuditStatus.Event, TransitionContext<WmsStockCheckDO>> stockCheckStateMachine() {

        StateMachineBuilder<Integer, WmsStockCheckAuditStatus.Event, TransitionContext<WmsStockCheckDO>> builder = StateMachineBuilderFactory.create();

        // 提交
        builder.externalTransitions()
            .fromAmong(WmsStockCheckAuditStatus.DRAFT.getValue(), WmsStockCheckAuditStatus.REJECT.getValue())
            .to(WmsStockCheckAuditStatus.AUDITING.getValue())
            .on(WmsStockCheckAuditStatus.Event.SUBMIT)
            .handle(StockCheckSubmitTransitionHandler.class);

        // 同意
        builder.externalTransition()
            .from(WmsStockCheckAuditStatus.AUDITING.getValue())
            .to(WmsStockCheckAuditStatus.PASS.getValue())
            .on(WmsStockCheckAuditStatus.Event.AGREE)
            .handle(StockCheckAgreeTransitionHandler.class);

        // 拒绝
        builder.externalTransition()
            .from(WmsStockCheckAuditStatus.AUDITING.getValue())
            .to(WmsStockCheckAuditStatus.REJECT.getValue())
            .on(WmsStockCheckAuditStatus.Event.REJECT)
            .handle(StockCheckRejectTransitionHandler.class);

        // 废弃
        builder.externalTransitions()
            .fromAmong(WmsStockCheckAuditStatus.DRAFT.getValue(), WmsStockCheckAuditStatus.REJECT.getValue(), WmsStockCheckAuditStatus.AUDITING.getValue())
            .to(WmsStockCheckAuditStatus.ABANDONED.getValue())
            .on(WmsStockCheckAuditStatus.Event.ABANDON)
            .handle(StockCheckAbandonTransitionHandler.class);

        // 失败处理
        builder.setFailCallback(StockCheckTransitionFailCallback.class);

        return builder.build(StockCheckStateMachineConfigure.STATE_MACHINE_NAME, ctx -> ctx.data().getAuditStatus());


    }


}
