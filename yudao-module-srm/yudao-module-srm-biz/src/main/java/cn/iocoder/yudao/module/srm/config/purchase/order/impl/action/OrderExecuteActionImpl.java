package cn.iocoder.yudao.module.srm.config.purchase.order.impl.action;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.cola.statemachine.Action;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseOrderMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmExecutionStatus;
import cn.iocoder.yudao.module.srm.service.purchase.SrmPurchaseOrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class OrderExecuteActionImpl implements Action<SrmExecutionStatus, SrmEventEnum, SrmPurchaseOrderDO> {
    @Resource
    SrmPurchaseOrderMapper mapper;
    @Autowired
    @Lazy
    SrmPurchaseOrderService srmPurchaseOrderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(SrmExecutionStatus from, SrmExecutionStatus to, SrmEventEnum event, SrmPurchaseOrderDO context) {

        //动态调整执行状态
        if (event == SrmEventEnum.EXECUTION_ADJUSTMENT) {
            //判断子 是否全部执行？部分执行？全部未执行，以此变化
            boolean hasNotInEx = false;
            boolean hasPartialEx = false;
            boolean allInEx = true;
            // 假设子订单列表为subOrders
            List<SrmPurchaseOrderItemDO> orderItemDOList = srmPurchaseOrderService.getPurchaseOrderItemListByOrderId(context.getId());
            for (SrmPurchaseOrderItemDO orderItemDO : orderItemDOList) {
                if (Objects.equals(orderItemDO.getExecuteStatus(), SrmExecutionStatus.PENDING.getCode())) {
                    hasNotInEx = true;
                } else if (Objects.equals(orderItemDO.getExecuteStatus(), SrmExecutionStatus.IN_PROGRESS.getCode())) {
                    hasPartialEx = true;
                } else if (!Objects.equals(orderItemDO.getExecuteStatus(), SrmExecutionStatus.COMPLETED.getCode())) {
                    allInEx = false;
                }
            }

            // 根据子订单的执行状态更新采购订单的执行状态
            SrmExecutionStatus newStatus = to;
            if (hasNotInEx && !hasPartialEx) {
                newStatus = SrmExecutionStatus.PENDING;
            } else if (hasPartialEx) {
                newStatus = SrmExecutionStatus.IN_PROGRESS;
            } else if (allInEx) {
                newStatus = SrmExecutionStatus.COMPLETED;
            }

            SrmPurchaseOrderDO orderDO = mapper.selectById(context.getId());
            orderDO.setExecuteStatus(newStatus.getCode());
            mapper.updateById(orderDO);
            log.debug("执行状态机触发({})事件", event.getDesc());
        } else {
            SrmPurchaseOrderDO orderDO = mapper.selectById(context.getId());
            orderDO.setExecuteStatus(to.getCode());
            mapper.updateById(orderDO);
            log.debug("执行状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
        }
    }
}