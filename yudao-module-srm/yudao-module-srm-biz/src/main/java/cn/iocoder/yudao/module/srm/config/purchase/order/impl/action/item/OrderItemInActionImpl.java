package cn.iocoder.yudao.module.srm.config.purchase.order.impl.action.item;

import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.module.srm.api.purchase.SrmInCountDTO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseOrderMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseRequestItemsMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmStorageStatus;
import com.alibaba.cola.statemachine.Action;
import com.alibaba.cola.statemachine.StateMachine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static cn.iocoder.yudao.module.srm.enums.SrmErrorCodeConstants.PURCHASE_REQUEST_ITEM_NOT_FOUND;
import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.PURCHASE_ORDER_STORAGE_STATE_MACHINE_NAME;
import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.PURCHASE_REQUEST_ITEM_STORAGE_STATE_MACHINE_NAME;

//订单项入库状态机
@Component
@Slf4j
public class OrderItemInActionImpl implements Action<SrmStorageStatus, SrmEventEnum, SrmInCountDTO> {

    @Resource
    private SrmPurchaseOrderItemMapper itemMapper;
    @Autowired
    SrmPurchaseRequestItemsMapper erpPurchaseRequestItemsMapper;
    @Resource
    private SrmPurchaseOrderMapper mapper;
    @Resource(name = PURCHASE_ORDER_STORAGE_STATE_MACHINE_NAME)
    private StateMachine storageStateMachine;

    @Resource(name = PURCHASE_REQUEST_ITEM_STORAGE_STATE_MACHINE_NAME)
    private StateMachine purchaseRequestItemStateMachine;


    //入库项(->入库主单)->订单项(->订单主单)->申请项(->订单主单)
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(SrmStorageStatus from, SrmStorageStatus to, SrmEventEnum event, SrmInCountDTO dto) {
        // 1. 先查询数据库中的采购项信息
        SrmPurchaseOrderItemDO oldData = itemMapper.selectById(dto.getOrderItemId());
        if (oldData == null) {
            return; // 防止空指针异常
        }
        // 根据事件类型更新入库状态
        if (event == null) {
            return; // 防止空指针异常
        }
        // 计算最新入库数量
        BigDecimal oldInCount = oldData.getInCount() == null ? BigDecimal.ZERO : oldData.getInCount();
        BigDecimal dtoCount = dto.getInCount() == null ? BigDecimal.ZERO : dto.getInCount();
        BigDecimal newInCount = oldInCount.add(dtoCount); // 计算新的入库数量
        //退货数量
        BigDecimal oldReturnCount = dto.getReturnCount() == null ? BigDecimal.ZERO : dto.getReturnCount();
        if (dto.getReturnCount() != null) {
            oldReturnCount = oldReturnCount.add(dto.getReturnCount());
            oldData.setReturnCount(oldReturnCount);//退货数量
        }

        if (event == SrmEventEnum.STORAGE_INIT) {

        }

        if (event == SrmEventEnum.STOCK_ADJUSTMENT) {

            if (newInCount.compareTo(BigDecimal.ZERO) <= 0) {
                // 未入库,最新入库数量 <= 0
                to = (SrmStorageStatus.NONE_IN_STORAGE);
            } else if (newInCount.compareTo(oldData.getCount()) < 0) {//入库值 小于 申请数量
                // 部分入库
                to = (SrmStorageStatus.PARTIALLY_IN_STORAGE);
            } else {
                // 全部入库
                to = (SrmStorageStatus.ALL_IN_STORAGE);
                //执行状态完毕
            }

        }
        // 更新数据库中的采购项状态
        itemMapper.updateById(oldData
            .setInStatus(to.getCode())//状态
            .setInCount(newInCount)//入库数量
        );

        // 3. 记录日志
        log.debug("订单项入库状态机触发({})事件：订单项ID={}，状态 {} -> {}, 入库数量={}, 退货数量={}",
            event.getDesc(),
            oldData.getId(),
            from.getDesc(),
            to.getDesc(),
            dto.getInCount(),
            dto.getReturnCount()
        );
        //4.0
        transferOrder(event, oldData);
        transferRequestItem(oldData, dtoCount);
    }

    private void transferRequestItem(SrmPurchaseOrderItemDO oldData, BigDecimal dtoCount) {
        //2.0 联动申请项的入库数量
        Optional.ofNullable(oldData.getPurchaseApplyItemId()).ifPresent(
            applyItemId -> {
                //传递给申请项入库状态机
                SrmPurchaseRequestItemsDO applyItemDO = erpPurchaseRequestItemsMapper.selectById(applyItemId);
                ThrowUtil.ifThrow(applyItemDO == null, PURCHASE_REQUEST_ITEM_NOT_FOUND, oldData.getId(), applyItemId);
                BigDecimal oldCount = applyItemDO.getInCount();
//                BigDecimal result = (oldCount != null && oldCount.compareTo(BigDecimal.ZERO) == 0) ? BigDecimal.ZERO : oldCount;
                BigDecimal result = oldCount == null ? BigDecimal.ZERO : oldCount;
                BigDecimal changeCount = result.subtract(dtoCount);
                purchaseRequestItemStateMachine.fireEvent(SrmStorageStatus.fromCode(applyItemDO.getInStatus())
                    , SrmEventEnum.STOCK_ADJUSTMENT
                    , SrmInCountDTO.builder().applyItemId(applyItemId).inCount(changeCount).build()
                );
            }
        );
    }

    private void transferOrder(SrmEventEnum event, SrmPurchaseOrderItemDO oldData) {
        // -触发订单入库状态变更
        // 根据订单项ID查询订单信息
        SrmPurchaseOrderDO orderDO = mapper.selectById(oldData.getOrderId());
        if (orderDO == null) {
            log.error("未找到对应的采购订单,订单ID={}", oldData.getOrderId());
        }
        storageStateMachine.fireEvent(SrmStorageStatus.fromCode(orderDO.getInStatus()), event, orderDO);
    }
}
