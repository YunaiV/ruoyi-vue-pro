package cn.iocoder.yudao.module.srm.config.purchase.returned;

import cn.iocoder.yudao.framework.cola.statemachine.Action;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.cola.statemachine.builder.StateMachineBuilder;
import cn.iocoder.yudao.framework.cola.statemachine.builder.StateMachineBuilderFactory;
import cn.iocoder.yudao.module.srm.config.BaseFailCallbackImpl;
import cn.iocoder.yudao.module.srm.config.machine.outItem.SrmPurchaseOutItemCountContext;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmOutboundStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.PURCHASE_RETURN_ITEM_OUT_STORAGE_STATE_MACHINE_NAME;

@Slf4j
@Configuration
@SuppressWarnings({"rawtypes", "unchecked"})
public class SrmPurchaseReturnedItemStatusMachine {


    @Autowired
    private BaseFailCallbackImpl baseFailCallbackImpl;

    @Autowired
    private Action<SrmOutboundStatus, SrmEventEnum, SrmPurchaseOutItemCountContext> outItemActionImpl;

    @Bean(PURCHASE_RETURN_ITEM_OUT_STORAGE_STATE_MACHINE_NAME)
    public StateMachine getOutStorageMachine() {
        StateMachineBuilder<SrmOutboundStatus, SrmEventEnum, SrmPurchaseOutItemCountContext> builder = StateMachineBuilderFactory.create();
        //初始化
        builder.internalTransition().within(SrmOutboundStatus.NONE_OUTBOUND).on(SrmEventEnum.OUT_STORAGE_INIT).perform(outItemActionImpl);
        //出库数量调整
        builder.externalTransitions().fromAmong(SrmOutboundStatus.NONE_OUTBOUND, SrmOutboundStatus.PARTIALLY_OUTBOUND, SrmOutboundStatus.ALL_OUTBOUND)
            .to(SrmOutboundStatus.PARTIALLY_OUTBOUND)
            .on(SrmEventEnum.OUT_STORAGE_ADJUSTMENT)
            .perform(outItemActionImpl);
        //出库作废
        builder.externalTransitions()
            .fromAmong(SrmOutboundStatus.NONE_OUTBOUND, SrmOutboundStatus.PARTIALLY_OUTBOUND)
            .to(SrmOutboundStatus.NONE_OUTBOUND)
            .on(SrmEventEnum.OUT_STORAGE_CANCEL)
            .perform(outItemActionImpl);
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(PURCHASE_RETURN_ITEM_OUT_STORAGE_STATE_MACHINE_NAME);
    }
}
