package cn.iocoder.yudao.module.srm.config.purchase.request;

import cn.iocoder.yudao.module.srm.api.purchase.SrmInCountDTO;
import cn.iocoder.yudao.module.srm.api.purchase.SrmOrderCountDTO;
import cn.iocoder.yudao.module.srm.config.BaseFailCallbackImpl;
import cn.iocoder.yudao.module.srm.config.purchase.request.impl.action.item.ItemStorageActionImpl;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.SrmStateMachines;
import cn.iocoder.yudao.module.srm.enums.status.SrmOffStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmOrderStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmStorageStatus;
import com.alibaba.cola.statemachine.Action;
import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@SuppressWarnings({"rawtypes", "unchecked"})
public class SrmPurchaseRequestItemStatusMachine {
    @Resource
    private Action<SrmOffStatus, SrmEventEnum, SrmPurchaseRequestItemsDO> actionItemOffImpl;
    @Resource
    private Action<SrmOrderStatus, SrmEventEnum, SrmOrderCountDTO> actionItemOrderImpl;
    @Resource
    private BaseFailCallbackImpl baseFailCallbackImpl;

    @Bean(SrmStateMachines.PURCHASE_REQUEST_ITEM_OFF_STATE_MACHINE_NAME)
    public StateMachine<SrmOffStatus, SrmEventEnum, SrmPurchaseRequestItemsDO> getPurchaseRequestStateMachine() {
        StateMachineBuilder<SrmOffStatus, SrmEventEnum, SrmPurchaseRequestItemsDO> builder = StateMachineBuilderFactory.create();
        // 初始化状态
        builder.internalTransition()
            .within(SrmOffStatus.OPEN)
            .on(SrmEventEnum.OFF_INIT)
            .perform(actionItemOffImpl);
        // 开启
        builder.externalTransitions()
            .fromAmong(SrmOffStatus.MANUAL_CLOSED)
            .to(SrmOffStatus.OPEN)
            .on(SrmEventEnum.ACTIVATE)
            .perform(actionItemOffImpl);
        // 手动关闭
        builder.externalTransition()
            .from(SrmOffStatus.OPEN)
            .to(SrmOffStatus.MANUAL_CLOSED)
            .on(SrmEventEnum.MANUAL_CLOSE)
            .perform(actionItemOffImpl);
        //自动关闭
        builder.externalTransition()
            .from(SrmOffStatus.OPEN)
            .to(SrmOffStatus.CLOSED)
            .on(SrmEventEnum.AUTO_CLOSE)
            .perform(actionItemOffImpl);
        //错误回调函数
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(SrmStateMachines.PURCHASE_REQUEST_ITEM_OFF_STATE_MACHINE_NAME);
    }

    //    子项采购状态
    @Bean(SrmStateMachines.PURCHASE_REQUEST_ITEM_ORDER_STATE_MACHINE_NAME)
    public StateMachine<SrmOrderStatus, SrmEventEnum, SrmOrderCountDTO> getPurchaseOrderStateMachine() {
        StateMachineBuilder<SrmOrderStatus, SrmEventEnum, SrmOrderCountDTO> builder = StateMachineBuilderFactory.create();
        //初始化事件
        builder.internalTransition()
            .within(SrmOrderStatus.OT_ORDERED)
            .on(SrmEventEnum.ORDER_INIT)
            .perform(actionItemOrderImpl);
        // 订购数量调整
        builder.externalTransitions()
            .fromAmong(SrmOrderStatus.OT_ORDERED)
            .to(SrmOrderStatus.PARTIALLY_ORDERED)
            .on(SrmEventEnum.ORDER_ADJUSTMENT)
            .perform(actionItemOrderImpl);

        builder.externalTransitions()
            .fromAmong(SrmOrderStatus.PARTIALLY_ORDERED)
            .to(SrmOrderStatus.ORDERED)
            .on(SrmEventEnum.ORDER_ADJUSTMENT)
            .perform(actionItemOrderImpl);

        builder.externalTransitions()
            .fromAmong(SrmOrderStatus.ORDERED)
            .to(SrmOrderStatus.PARTIALLY_ORDERED)
            .on(SrmEventEnum.ORDER_ADJUSTMENT)
            .perform(actionItemOrderImpl);

        builder.externalTransitions()
            .fromAmong(SrmOrderStatus.PARTIALLY_ORDERED)
            .to(SrmOrderStatus.OT_ORDERED)
            .on(SrmEventEnum.ORDER_ADJUSTMENT)
            .perform(actionItemOrderImpl);

        //放弃订购
        builder.externalTransitions()
            .fromAmong(SrmOrderStatus.PARTIALLY_ORDERED, SrmOrderStatus.OT_ORDERED)
            .to(SrmOrderStatus.ORDER_FAILED)
            .on(SrmEventEnum.ORDER_CANCEL)
            .perform(actionItemOrderImpl);

        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(SrmStateMachines.PURCHASE_REQUEST_ITEM_ORDER_STATE_MACHINE_NAME);
    }

    @Resource
    ItemStorageActionImpl itemStorageActionImpl;

    @Bean(SrmStateMachines.PURCHASE_REQUEST_ITEM_STORAGE_STATE_MACHINE_NAME)
    public StateMachine<SrmStorageStatus, SrmEventEnum, SrmInCountDTO> buildPurchaseOrderItemStorageStateMachine() {
        StateMachineBuilder<SrmStorageStatus, SrmEventEnum, SrmInCountDTO> builder = StateMachineBuilderFactory.create();
        // 初始化入库
        builder.externalTransition()
            .from(SrmStorageStatus.NONE_IN_STORAGE)
            .to(SrmStorageStatus.NONE_IN_STORAGE)
            .on(SrmEventEnum.STORAGE_INIT)
            .perform(itemStorageActionImpl);
        // 取消入库
        builder.externalTransitions()
            .fromAmong(SrmStorageStatus.NONE_IN_STORAGE, SrmStorageStatus.PARTIALLY_IN_STORAGE)
            .to(SrmStorageStatus.NONE_IN_STORAGE)
            .on(SrmEventEnum.CANCEL_STORAGE)
            .perform(itemStorageActionImpl);
        // 库存调整
        builder.externalTransitions()
            .fromAmong(SrmStorageStatus.NONE_IN_STORAGE, SrmStorageStatus.PARTIALLY_IN_STORAGE, SrmStorageStatus.ALL_IN_STORAGE)
            .to(SrmStorageStatus.NONE_IN_STORAGE)
            .on(SrmEventEnum.STOCK_ADJUSTMENT)
            .perform(itemStorageActionImpl);
        // 设置错误回调
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(SrmStateMachines.PURCHASE_REQUEST_ITEM_STORAGE_STATE_MACHINE_NAME);
    }


}
