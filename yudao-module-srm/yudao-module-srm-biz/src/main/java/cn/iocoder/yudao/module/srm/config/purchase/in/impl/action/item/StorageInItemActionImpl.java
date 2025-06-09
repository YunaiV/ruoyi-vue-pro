package cn.iocoder.yudao.module.srm.config.purchase.in.impl.action.item;

import cn.iocoder.yudao.framework.cola.statemachine.Action;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.module.srm.config.machine.SrmOrderInCountContext;
import cn.iocoder.yudao.module.srm.config.machine.in.SrmPurchaseInCountContext;
import cn.iocoder.yudao.module.srm.config.machine.inItem.SrmPurchaseInItemCountContext;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInItemDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseInItemMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmStorageStatus;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.PURCHASE_IN_STORAGE_STATE_MACHINE;
import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.PURCHASE_ORDER_ITEM_STORAGE_STATE_MACHINE_NAME;

/**
 * 到货单明细行 入库状态机
 * <p>
 * 更改item实际入库数量+入库状态
 */
@Slf4j
@Component("SrmPurchaseInItemStorageActionImpl")
public class StorageInItemActionImpl implements Action<SrmStorageStatus, SrmEventEnum, SrmPurchaseInItemCountContext> {
    @Autowired
    private SrmPurchaseInItemMapper srmPurchaseInItemMapper;

    @Resource(name = PURCHASE_ORDER_ITEM_STORAGE_STATE_MACHINE_NAME)
    @Lazy
    StateMachine<SrmStorageStatus, SrmEventEnum, SrmOrderInCountContext> orderItemStorageStateMachine;
    @Resource(name = PURCHASE_IN_STORAGE_STATE_MACHINE)
    @Lazy
    StateMachine<SrmStorageStatus, SrmEventEnum, SrmPurchaseInCountContext> pushInStorageStateMachine;

    @Override
    public void execute(SrmStorageStatus from, SrmStorageStatus to, SrmEventEnum event, SrmPurchaseInItemCountContext context) {
        SrmPurchaseInItemDO inItemDO = srmPurchaseInItemMapper.selectById(context.getArriveItemId());

        //调整库存
        if (event == SrmEventEnum.STOCK_ADJUSTMENT) {
            BigDecimal oldActualQty = inItemDO.getActualQty() == null ? BigDecimal.ZERO : inItemDO.getActualQty(); // 原实际入库数量
            BigDecimal changeActualQty = context.getInCount() == null ? BigDecimal.ZERO : context.getInCount(); // 变更数量
            BigDecimal finalActualQty = oldActualQty.add(changeActualQty); // 最终实际入库数量
            BigDecimal qty = inItemDO.getQty(); // 计划入库数量
            //qty == actualQty to为ALL_IN_STORAGE 完全入库
            //qty > actualQty to为PARTIALLY_IN_STORAGE 部分入库
            if (finalActualQty.compareTo(qty) >= 0) {
                // 实际入库数量大于等于计划数量,完全入库
                to = SrmStorageStatus.ALL_IN_STORAGE;
            } else if (finalActualQty.compareTo(BigDecimal.ZERO) > 0) {
                // 实际入库数量大于0但小于计划数量,部分入库
                to = SrmStorageStatus.PARTIALLY_IN_STORAGE;
            } else {
                // 实际入库数量为0,未入库
                to = SrmStorageStatus.NONE_IN_STORAGE;
            }

            //  更新数据库中的实际入库数量
            inItemDO.setActualQty(finalActualQty);
        }

        inItemDO.setInboundStatus(to.getCode());
        //
        srmPurchaseInItemMapper.updateById(inItemDO);

        //1. 转递给主单
        pushInStorageStateMachine.fireEvent(SrmStorageStatus.NONE_IN_STORAGE, SrmEventEnum.STOCK_ADJUSTMENT, SrmPurchaseInCountContext.builder().arriveId(inItemDO.getArriveId()).build());
        //2. 传递事件给订单项, 入库状态
        if (event != SrmEventEnum.ORDER_INIT) {
            orderItemStorageStateMachine.fireEvent(
                SrmStorageStatus.NONE_IN_STORAGE
                , SrmEventEnum.STOCK_ADJUSTMENT
                , SrmOrderInCountContext.builder().orderItemId(inItemDO.getOrderItemId()).inCount(context.getInCount()).build());
        }

    }
}
