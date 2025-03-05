package cn.iocoder.yudao.module.erp.config.purchase.request;

import cn.iocoder.yudao.module.erp.config.purchase.request.impl.BaseFailCallbackImpl;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.ErpStateMachines;
import cn.iocoder.yudao.module.erp.enums.status.ErpOffStatus;
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
    private Action<ErpStorageStatus, ErpEventEnum, ErpPurchaseRequestItemsDO> actionItemStorageImpl;
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

    //    子项入库状态
    @Bean(ErpStateMachines.PURCHASE_ORDER_ITEM_STATE_MACHINE_NAME)
    public StateMachine<ErpStorageStatus, ErpEventEnum, ErpPurchaseRequestItemsDO> getPurchaseOrderStateMachine() {
        StateMachineBuilder<ErpStorageStatus, ErpEventEnum, ErpPurchaseRequestItemsDO> builder = StateMachineBuilderFactory.create();
//        INIT_STORAGE("入库初始化"),
//            ADD_TO_STORAGE("添加至库存"),
//            REMOVE_FROM_STORAGE("从库存移除"),
//            STOCK_ADJUSTMENT("库存调整");
        // 初始化状态
        builder.internalTransition()
            .within(ErpStorageStatus.NONE_IN_STORAGE)
            .on(ErpEventEnum.STORAGE_INIT)
            .perform(actionItemStorageImpl);
        //添加库存
        builder.externalTransition()
            .from(ErpStorageStatus.NONE_IN_STORAGE)
            .to(ErpStorageStatus.PARTIALLY_IN_STORAGE)
            .on(ErpEventEnum.ADD_STOCK)
            .perform(actionItemStorageImpl);
        //减少库存
        builder.externalTransition()
            .from(ErpStorageStatus.PARTIALLY_IN_STORAGE)
            .to(ErpStorageStatus.NONE_IN_STORAGE)
            .on(ErpEventEnum.REDUCE_STOCK)
            .perform(actionItemStorageImpl);
        // 库存调整：从任何存储状态到调整状态，触发STOCK_ADJUSTMENT事件
//        builder.externalTransition()
//            .from(ErpStorageStatus.PARTIALLY_IN_STORAGE)
//            .to(ErpStorageStatus.STOCK_ADJUSTMENT)
//            .on(ErpEventEnum.STOCK_ADJUSTMENT)
//            .perform(actionItemStorageImpl);
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(ErpStateMachines.PURCHASE_ORDER_ITEM_STATE_MACHINE_NAME);
    }
}
