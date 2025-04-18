package cn.iocoder.yudao.module.wms.config;

import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.cola.statemachine.builder.StateMachineBuilder;
import cn.iocoder.yudao.framework.cola.statemachine.builder.StateMachineBuilderFactory;
import cn.iocoder.yudao.framework.cola.statemachine.builder.TransitionContext;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryDO;
import cn.iocoder.yudao.module.wms.enums.inventory.WmsInventoryAuditStatus;
import cn.iocoder.yudao.module.wms.service.inventory.transition.InventoryAgreeTransitionHandler;
import cn.iocoder.yudao.module.wms.service.inventory.transition.InventoryRejectTransitionHandler;
import cn.iocoder.yudao.module.wms.service.inventory.transition.InventorySubmitTransitionHandler;
import cn.iocoder.yudao.module.wms.service.inventory.transition.InventoryTransitionFailCallback;
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
public class InventoryStateMachineConfigure {

    /**
     * 状态机名称
     **/
    public static final String STATE_MACHINE_NAME = "inventoryStateMachine";
    
    /**
     * 创建与配置状态机
     **/
    @Bean(InventoryStateMachineConfigure.STATE_MACHINE_NAME)
    public StateMachine<Integer, WmsInventoryAuditStatus.Event, TransitionContext<WmsInventoryDO>> inventoryActionStateMachine() {

        StateMachineBuilder<Integer, WmsInventoryAuditStatus.Event, TransitionContext<WmsInventoryDO>> builder = StateMachineBuilderFactory.create();

        // 提交
        builder.externalTransitions()
            .fromAmong(WmsInventoryAuditStatus.DRAFT.getValue(), WmsInventoryAuditStatus.REJECT.getValue())
            .to(WmsInventoryAuditStatus.AUDITING.getValue())
            .on(WmsInventoryAuditStatus.Event.SUBMIT)
            .handle(InventorySubmitTransitionHandler.class);

        // 同意
        builder.externalTransition()
            .from(WmsInventoryAuditStatus.AUDITING.getValue())
            .to(WmsInventoryAuditStatus.PASS.getValue())
            .on(WmsInventoryAuditStatus.Event.AGREE)
            .handle(InventoryAgreeTransitionHandler.class);

        // 拒绝
        builder.externalTransition()
            .from(WmsInventoryAuditStatus.AUDITING.getValue())
            .to(WmsInventoryAuditStatus.REJECT.getValue())
            .on(WmsInventoryAuditStatus.Event.REJECT)
            .handle(InventoryRejectTransitionHandler.class);



        // 失败处理
        builder.setFailCallback(InventoryTransitionFailCallback.class);

        return builder.build(OutboundStateMachineConfigure.STATE_MACHINE_NAME);


    }

//    /**
//     * 创建与配置状态机
//     **/
//    @Bean(InventoryStateMachineConfigure.STATE_MACHINE_NAME)
//    public StateMachineWrapper<Integer, WmsInventoryAuditStatus.Event, WmsInventoryDO> inboundActionStateMachine() {
//        StateMachineWrapper<Integer, WmsInventoryAuditStatus.Event, WmsInventoryDO> wrapper = new StateMachineWrapper<>(STATE_MACHINE_NAME, WmsInventoryDO::getAuditStatus);
//
//        // 提交
//        wrapper.bindExternals(
//                // from
//                new Integer[]{
//                    WmsInventoryAuditStatus.DRAFT.getValue(),
//                    WmsInventoryAuditStatus.REJECT.getValue()
//                },
//                // event
//                WmsInventoryAuditStatus.Event.SUBMIT,
//                // to
//                WmsInventoryAuditStatus.AUDITING.getValue(),
//                // handler
//                InventorySubmitTransitionHandler.class
//            )
//            // 同意
//            .bindExternal(
//                // from
//                WmsInventoryAuditStatus.AUDITING.getValue(),
//                // event
//                WmsInventoryAuditStatus.Event.AGREE,
//                // to
//                WmsInventoryAuditStatus.PASS.getValue(),
//                // handler
//                InventoryAgreeTransitionHandler.class
//            )
//            // 拒绝
//            .bindExternal(
//                // from
//                WmsInventoryAuditStatus.AUDITING.getValue(),
//                // event
//                WmsInventoryAuditStatus.Event.REJECT,
//                // to
//                WmsInventoryAuditStatus.REJECT.getValue(),
//                // handler
//                InventoryRejectTransitionHandler.class
//            )
//            // 失败处理
//            .setFailCallback(InventoryTransitionFailCallback.class)
//        ;
//
//
//        return wrapper;
//    }



}
