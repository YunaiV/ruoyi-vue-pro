package cn.iocoder.yudao.module.erp.config.purchase.order.impl.action.item;

import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpOffStatus;
import com.alibaba.cola.statemachine.Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class ActionOrderItemOffImpl implements Action<ErpOffStatus, ErpEventEnum, ErpPurchaseOrderItemDO> {

    @Autowired
    private ErpPurchaseOrderItemMapper itemMapper;

    @Autowired
    private ErpPurchaseOrderMapper orderMapper;

    @Override
    @Transactional
    public void execute(ErpOffStatus from, ErpOffStatus to, ErpEventEnum event, ErpPurchaseOrderItemDO context) {
        // 查询采购订单子项
        ErpPurchaseOrderItemDO itemDO = itemMapper.selectById(context.getId());
        if (itemDO == null) {
            log.warn("采购订单子项不存在，ID: {}", context.getId());
            return;
        }

        // 更新子项的状态
        itemDO.setOffStatus(to.getCode());
        itemMapper.updateById(itemDO);
        log.info("子项开关状态机触发({})事件：对象ID={}，状态 {} -> {}", event.getDesc(), itemDO.getId(), from.getDesc(), to.getDesc());

        // 检查是否需要传递事件给主表
        isPass(itemDO);
    }

    /**
     * 根据所有子项的状态，决定是否更新主表状态
     */
    @Transactional
    public void isPass(ErpPurchaseOrderItemDO updatedItem) {
        Long orderId = updatedItem.getOrderId();
        if (orderId == null) {
            log.warn("子项ID={} 无关联的订单，无法更新主表状态", updatedItem.getId());
            return;
        }

        // 查询该订单下所有子项
        List<ErpPurchaseOrderItemDO> itemList = itemMapper.selectListByOrderId(orderId);
        if (itemList.isEmpty()) {
            log.warn("采购订单ID={} 没有子项，无法更新主表状态", orderId);
            return;
        }

        // 判断主表状态
        boolean hasOpenItems = itemList.stream().anyMatch(item -> Objects.equals(item.getOffStatus(), ErpOffStatus.OPEN.getCode()));
        boolean allClosed = itemList.stream().allMatch(item -> Objects.equals(item.getOffStatus(), ErpOffStatus.CLOSED.getCode()));

//        // 更新主表状态
//        if (hasOpenItems) {
//            orderDO.setOffStatus(ErpOffStatus.OPEN.getCode());
//        } else if (allClosed) {
//            orderDO.setOffStatus(ErpOffStatus.CLOSED.getCode());
//        }
    }
}
