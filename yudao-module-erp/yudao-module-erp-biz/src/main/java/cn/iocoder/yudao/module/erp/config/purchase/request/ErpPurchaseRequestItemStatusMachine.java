package cn.iocoder.yudao.module.erp.config.purchase.request;

import cn.iocoder.yudao.module.erp.api.purchase.SrmInCountDTO;
import cn.iocoder.yudao.module.erp.api.purchase.TmsOrderCountDTO;
import cn.iocoder.yudao.module.erp.config.BaseFailCallbackImpl;
import cn.iocoder.yudao.module.erp.config.purchase.request.impl.action.item.ActionItemStorageImpl;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.ErpStateMachines;
import cn.iocoder.yudao.module.erp.enums.status.ErpOffStatus;
import cn.iocoder.yudao.module.erp.enums.status.ErpOrderStatus;
import cn.iocoder.yudao.module.erp.enums.status.ErpStorageStatus;
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
    private Action<ErpOffStatus, ErpEventEnum, ErpPurchaseRequestItemsDO> actionItemOffImpl;
    @Resource
    private Action<ErpOrderStatus, ErpEventEnum, TmsOrderCountDTO> actionItemOrderImpl;
    @Resource
    private BaseFailCallbackImpl baseFailCallbackImpl;

    @Bean(ErpStateMachines.PURCHASE_REQUEST_ITEM_OFF_STATE_MACHINE_NAME)
    public StateMachine<ErpOffStatus, ErpEventEnum, ErpPurchaseRequestItemsDO> getPurchaseRequestStateMachine() {
        StateMachineBuilder<ErpOffStatus, ErpEventEnum, ErpPurchaseRequestItemsDO> builder = StateMachineBuilderFactory.create();
        // 初始化状态
        builder.internalTransition()
            .within(ErpOffStatus.OPEN)
            .on(ErpEventEnum.OFF_INIT)
            .perform(actionItemOffImpl);
        // 开启
        builder.externalTransitions()
            .fromAmong(ErpOffStatus.MANUAL_CLOSED)
            .to(ErpOffStatus.OPEN)
            .on(ErpEventEnum.ACTIVATE)
            .perform(actionItemOffImpl);
        // 手动关闭
        builder.externalTransition()
            .from(ErpOffStatus.OPEN)
            .to(ErpOffStatus.MANUAL_CLOSED)
            .on(ErpEventEnum.MANUAL_CLOSE)
            .perform(actionItemOffImpl);
        //自动关闭
        builder.externalTransition()
            .from(ErpOffStatus.OPEN)
            .to(ErpOffStatus.CLOSED)
            .on(ErpEventEnum.AUTO_CLOSE)
            .perform(actionItemOffImpl);
        //错误回调函数
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(ErpStateMachines.PURCHASE_REQUEST_ITEM_OFF_STATE_MACHINE_NAME);
    }

    //    子项采购状态
    @Bean(ErpStateMachines.PURCHASE_REQUEST_ITEM_ORDER_STATE_MACHINE_NAME)
    public StateMachine<ErpOrderStatus, ErpEventEnum, TmsOrderCountDTO> getPurchaseOrderStateMachine() {
        StateMachineBuilder<ErpOrderStatus, ErpEventEnum, TmsOrderCountDTO> builder = StateMachineBuilderFactory.create();
        //初始化事件
        builder.internalTransition()
            .within(ErpOrderStatus.OT_ORDERED)
            .on(ErpEventEnum.ORDER_INIT)
            .perform(actionItemOrderImpl);
        // 订购数量调整
        builder.externalTransitions()
            .fromAmong(ErpOrderStatus.OT_ORDERED)
            .to(ErpOrderStatus.PARTIALLY_ORDERED)
            .on(ErpEventEnum.ORDER_ADJUSTMENT)
            .perform(actionItemOrderImpl);

        builder.externalTransitions()
            .fromAmong(ErpOrderStatus.PARTIALLY_ORDERED)
            .to(ErpOrderStatus.ORDERED)
            .on(ErpEventEnum.ORDER_ADJUSTMENT)
            .perform(actionItemOrderImpl);

        builder.externalTransitions()
            .fromAmong(ErpOrderStatus.ORDERED)
            .to(ErpOrderStatus.PARTIALLY_ORDERED)
            .on(ErpEventEnum.ORDER_ADJUSTMENT)
            .perform(actionItemOrderImpl);

        builder.externalTransitions()
            .fromAmong(ErpOrderStatus.PARTIALLY_ORDERED)
            .to(ErpOrderStatus.OT_ORDERED)
            .on(ErpEventEnum.ORDER_ADJUSTMENT)
            .perform(actionItemOrderImpl);

        //放弃订购
        builder.externalTransitions()
            .fromAmong(ErpOrderStatus.PARTIALLY_ORDERED, ErpOrderStatus.OT_ORDERED)
            .to(ErpOrderStatus.ORDER_FAILED)
            .on(ErpEventEnum.ORDER_CANCEL)
            .perform(actionItemOrderImpl);

        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(ErpStateMachines.PURCHASE_REQUEST_ITEM_ORDER_STATE_MACHINE_NAME);
    }

    @Resource
    ActionItemStorageImpl actionItemStorageImpl;

    @Bean(ErpStateMachines.PURCHASE_REQUEST_ITEM_STORAGE_STATE_MACHINE_NAME)
    public StateMachine<ErpStorageStatus, ErpEventEnum, SrmInCountDTO> buildPurchaseOrderItemStorageStateMachine() {
        StateMachineBuilder<ErpStorageStatus, ErpEventEnum, SrmInCountDTO> builder = StateMachineBuilderFactory.create();
        // 初始化入库
        builder.externalTransition()
            .from(ErpStorageStatus.NONE_IN_STORAGE)
            .to(ErpStorageStatus.NONE_IN_STORAGE)
            .on(ErpEventEnum.STORAGE_INIT)
            .perform(actionItemStorageImpl);
        // 取消入库
        builder.externalTransitions()
            .fromAmong(ErpStorageStatus.NONE_IN_STORAGE, ErpStorageStatus.PARTIALLY_IN_STORAGE)
            .to(ErpStorageStatus.NONE_IN_STORAGE)
            .on(ErpEventEnum.CANCEL_STORAGE)
            .perform(actionItemStorageImpl);
        // 库存调整
        builder.externalTransitions()
            .fromAmong(ErpStorageStatus.NONE_IN_STORAGE, ErpStorageStatus.PARTIALLY_IN_STORAGE, ErpStorageStatus.ALL_IN_STORAGE)
            .to(ErpStorageStatus.NONE_IN_STORAGE)
            .on(ErpEventEnum.STOCK_ADJUSTMENT)
            .perform(actionItemStorageImpl);
        // 设置错误回调
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(ErpStateMachines.PURCHASE_REQUEST_ITEM_STORAGE_STATE_MACHINE_NAME);
    }
}
