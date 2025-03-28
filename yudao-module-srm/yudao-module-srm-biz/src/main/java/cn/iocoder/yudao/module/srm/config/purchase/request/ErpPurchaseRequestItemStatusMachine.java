package cn.iocoder.yudao.module.srm.config.purchase.request;

import cn.iocoder.yudao.module.srm.api.purchase.SrmInCountDTO;
import cn.iocoder.yudao.module.srm.api.purchase.TmsOrderCountDTO;
import cn.iocoder.yudao.module.srm.config.BaseFailCallbackImpl;
import cn.iocoder.yudao.module.srm.config.purchase.request.impl.action.item.ActionItemStorageImpl;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.SrmStateMachines;
import cn.iocoder.yudao.module.srm.enums.status.ErpOffStatus;
import cn.iocoder.yudao.module.srm.enums.status.ErpOrderStatus;
import cn.iocoder.yudao.module.srm.enums.status.ErpStorageStatus;
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
public class ErpPurchaseRequestItemStatusMachine {
    @Resource
    private Action<ErpOffStatus, SrmEventEnum, ErpPurchaseRequestItemsDO> actionItemOffImpl;
    @Resource
    private Action<ErpOrderStatus, SrmEventEnum, TmsOrderCountDTO> actionItemOrderImpl;
    @Resource
    private BaseFailCallbackImpl baseFailCallbackImpl;

    @Bean(SrmStateMachines.PURCHASE_REQUEST_ITEM_OFF_STATE_MACHINE_NAME)
    public StateMachine<ErpOffStatus, SrmEventEnum, ErpPurchaseRequestItemsDO> getPurchaseRequestStateMachine() {
        StateMachineBuilder<ErpOffStatus, SrmEventEnum, ErpPurchaseRequestItemsDO> builder = StateMachineBuilderFactory.create();
        // 初始化状态
        builder.internalTransition()
            .within(ErpOffStatus.OPEN)
            .on(SrmEventEnum.OFF_INIT)
            .perform(actionItemOffImpl);
        // 开启
        builder.externalTransitions()
            .fromAmong(ErpOffStatus.MANUAL_CLOSED)
            .to(ErpOffStatus.OPEN)
            .on(SrmEventEnum.ACTIVATE)
            .perform(actionItemOffImpl);
        // 手动关闭
        builder.externalTransition()
            .from(ErpOffStatus.OPEN)
            .to(ErpOffStatus.MANUAL_CLOSED)
            .on(SrmEventEnum.MANUAL_CLOSE)
            .perform(actionItemOffImpl);
        //自动关闭
        builder.externalTransition()
            .from(ErpOffStatus.OPEN)
            .to(ErpOffStatus.CLOSED)
            .on(SrmEventEnum.AUTO_CLOSE)
            .perform(actionItemOffImpl);
        //错误回调函数
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(SrmStateMachines.PURCHASE_REQUEST_ITEM_OFF_STATE_MACHINE_NAME);
    }

    //    子项采购状态
    @Bean(SrmStateMachines.PURCHASE_REQUEST_ITEM_ORDER_STATE_MACHINE_NAME)
    public StateMachine<ErpOrderStatus, SrmEventEnum, TmsOrderCountDTO> getPurchaseOrderStateMachine() {
        StateMachineBuilder<ErpOrderStatus, SrmEventEnum, TmsOrderCountDTO> builder = StateMachineBuilderFactory.create();
        //初始化事件
        builder.internalTransition()
            .within(ErpOrderStatus.OT_ORDERED)
            .on(SrmEventEnum.ORDER_INIT)
            .perform(actionItemOrderImpl);
        // 订购数量调整
        builder.externalTransitions()
            .fromAmong(ErpOrderStatus.OT_ORDERED)
            .to(ErpOrderStatus.PARTIALLY_ORDERED)
            .on(SrmEventEnum.ORDER_ADJUSTMENT)
            .perform(actionItemOrderImpl);

        builder.externalTransitions()
            .fromAmong(ErpOrderStatus.PARTIALLY_ORDERED)
            .to(ErpOrderStatus.ORDERED)
            .on(SrmEventEnum.ORDER_ADJUSTMENT)
            .perform(actionItemOrderImpl);

        builder.externalTransitions()
            .fromAmong(ErpOrderStatus.ORDERED)
            .to(ErpOrderStatus.PARTIALLY_ORDERED)
            .on(SrmEventEnum.ORDER_ADJUSTMENT)
            .perform(actionItemOrderImpl);

        builder.externalTransitions()
            .fromAmong(ErpOrderStatus.PARTIALLY_ORDERED)
            .to(ErpOrderStatus.OT_ORDERED)
            .on(SrmEventEnum.ORDER_ADJUSTMENT)
            .perform(actionItemOrderImpl);

        //放弃订购
        builder.externalTransitions()
            .fromAmong(ErpOrderStatus.PARTIALLY_ORDERED, ErpOrderStatus.OT_ORDERED)
            .to(ErpOrderStatus.ORDER_FAILED)
            .on(SrmEventEnum.ORDER_CANCEL)
            .perform(actionItemOrderImpl);

        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(SrmStateMachines.PURCHASE_REQUEST_ITEM_ORDER_STATE_MACHINE_NAME);
    }

    @Resource
    ActionItemStorageImpl actionItemStorageImpl;

    @Bean(SrmStateMachines.PURCHASE_REQUEST_ITEM_STORAGE_STATE_MACHINE_NAME)
    public StateMachine<ErpStorageStatus, SrmEventEnum, SrmInCountDTO> buildPurchaseOrderItemStorageStateMachine() {
        StateMachineBuilder<ErpStorageStatus, SrmEventEnum, SrmInCountDTO> builder = StateMachineBuilderFactory.create();
        // 初始化入库
        builder.externalTransition()
            .from(ErpStorageStatus.NONE_IN_STORAGE)
            .to(ErpStorageStatus.NONE_IN_STORAGE)
            .on(SrmEventEnum.STORAGE_INIT)
            .perform(actionItemStorageImpl);
        // 取消入库
        builder.externalTransitions()
            .fromAmong(ErpStorageStatus.NONE_IN_STORAGE, ErpStorageStatus.PARTIALLY_IN_STORAGE)
            .to(ErpStorageStatus.NONE_IN_STORAGE)
            .on(SrmEventEnum.CANCEL_STORAGE)
            .perform(actionItemStorageImpl);
        // 库存调整
        builder.externalTransitions()
            .fromAmong(ErpStorageStatus.NONE_IN_STORAGE, ErpStorageStatus.PARTIALLY_IN_STORAGE, ErpStorageStatus.ALL_IN_STORAGE)
            .to(ErpStorageStatus.NONE_IN_STORAGE)
            .on(SrmEventEnum.STOCK_ADJUSTMENT)
            .perform(actionItemStorageImpl);
        // 设置错误回调
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(SrmStateMachines.PURCHASE_REQUEST_ITEM_STORAGE_STATE_MACHINE_NAME);
    }


}
