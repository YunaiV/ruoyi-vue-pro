package cn.iocoder.yudao.module.erp.config.purchase.order.impl.action.item;

import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpStorageStatus;
import com.alibaba.cola.statemachine.Action;
import com.alibaba.cola.statemachine.StateMachine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static cn.iocoder.yudao.module.erp.enums.ErpStateMachines.PURCHASE_ORDER_STORAGE_STATE_MACHINE_NAME;

//订单项入库状态机
@Component
@Slf4j
public class ActionOrderItemInImpl implements Action<ErpStorageStatus, ErpEventEnum, ErpPurchaseOrderItemDO> {

    @Resource
    private ErpPurchaseOrderItemMapper itemMapper;
    @Resource
    private ErpPurchaseOrderMapper mapper;
    @Resource(name = PURCHASE_ORDER_STORAGE_STATE_MACHINE_NAME)
    private StateMachine storageStateMachine;
    //入库单->订单项->订单->申请项->申请单
    @Override
    public void execute(ErpStorageStatus from, ErpStorageStatus to, ErpEventEnum erpEventEnum, ErpPurchaseOrderItemDO context) {
        //入库数量+入库状态
        // 1. 校验参数
        if (context == null) {
            return;
        }

        // 2. 根据事件类型处理
        if (Objects.requireNonNull(erpEventEnum) == ErpEventEnum.STOCK_ADJUSTMENT) {// 库存调整时,需要重新计算入库状态
            if (context.getInCount() == null || context.getInCount().compareTo(context.getCount()) == 0) {
                // 未入库或已全部入库
                context.setInStatus(ErpStorageStatus.NONE_IN_STORAGE.getCode());
            } else if (context.getInCount().compareTo(context.getCount()) < 0) {
                // 部分入库
                context.setInStatus(ErpStorageStatus.PARTIALLY_IN_STORAGE.getCode());
            } else {
                // 全部入库
                context.setInStatus(ErpStorageStatus.ALL_IN_STORAGE.getCode());
            }
        } else {// 其他事件直接更新状态
            context.setInStatus(to.getCode());
        }
        // 4. 触发订单入库状态变更
        // 根据订单项ID查询订单信息
        ErpPurchaseOrderDO orderDO = mapper.selectById(context.getOrderId());
        if (orderDO == null) {
            log.error("未找到对应的采购订单,订单ID={}", context.getOrderId());
            return;
        }
        orderDO.setId(context.getOrderId());
        storageStateMachine.fireEvent(ErpStorageStatus.fromCode(orderDO.getInStatus()), erpEventEnum, orderDO);

        // 3. 记录日志
        log.info("子项入库状态机触发({})事件：对象ID={}，状态 {} -> {}, 入库数量={}",
            erpEventEnum.getDesc(),
            context.getId(),
            from.getDesc(),
            to.getDesc(),
            context.getInCount());
    }
}
