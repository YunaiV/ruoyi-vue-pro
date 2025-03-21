package cn.iocoder.yudao.module.erp.config.purchase.request.impl.action;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestItemsMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.erp.enums.SrmEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpOrderStatus;
import com.alibaba.cola.statemachine.Action;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class ActionOrderImpl implements Action<ErpOrderStatus, SrmEventEnum, ErpPurchaseRequestDO> {

    @Resource
    private ErpPurchaseRequestMapper purchaseRequestMapper;

    @Resource
    private ErpPurchaseRequestItemsMapper purchaseRequestItemsMapper;

    @Override
    @Transactional
    public void execute(ErpOrderStatus from, ErpOrderStatus to, SrmEventEnum event, ErpPurchaseRequestDO context) {
//        if (event == SrmEventEnum.ORDER_GOODS_ADD || event == SrmEventEnum.ORDER_GOODS_REDUCE) {
//            updatePurchaseOrderStatus(context);
//            return;
//        }

        ErpPurchaseRequestDO requestDO = purchaseRequestMapper.selectById(context.getId());
        if (requestDO == null) {
            log.error("采购申请单不存在，ID: {}", context.getId());
            return;
        }
        //子数量调整
        if (event == SrmEventEnum.ORDER_ADJUSTMENT) {
            List<ErpPurchaseRequestItemsDO> requestItemsDOS = purchaseRequestItemsMapper.selectListByRequestId(requestDO.getId());
            //全部采购
            if (requestItemsDOS.stream().allMatch(item -> item.getOrderStatus().equals(ErpOrderStatus.ORDERED.getCode()))) {
                requestDO.setOrderStatus(ErpOrderStatus.ORDERED.getCode());
            }
            //部分采购
            if (requestItemsDOS.stream().anyMatch(item -> item.getOrderStatus().equals(ErpOrderStatus.PARTIALLY_ORDERED.getCode()))) {
                requestDO.setOrderStatus(ErpOrderStatus.PARTIALLY_ORDERED.getCode());
            }
            //未采购,状态都是未采购
            if (requestItemsDOS.stream().allMatch(item -> item.getOrderStatus().equals(ErpOrderStatus.OT_ORDERED.getCode()))) {
                requestDO.setOrderStatus(ErpOrderStatus.OT_ORDERED.getCode());
            }
        } else {
            //其他事件,初始化+放弃
            requestDO.setOrderStatus(to.getCode());
        }
        // 其他事件：直接更新采购申请的状态

        ThrowUtil.ifSqlThrow(purchaseRequestMapper.updateById(requestDO), GlobalErrorCodeConstants.DB_UPDATE_ERROR);
        log.debug("采购状态机触发({})事件：将对象{},由状态 {}->{}",
            event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), ErpOrderStatus.fromCode(requestDO.getOrderStatus()).getDesc());
    }

    /**
     * 更新采购申请的订单状态
     */
    private void updatePurchaseOrderStatus(ErpPurchaseRequestDO requestDO) {
        List<ErpPurchaseRequestItemsDO> items = purchaseRequestItemsMapper.selectListByRequestId(requestDO.getId());

        if (items.isEmpty()) {
            log.warn("采购申请单ID={} 为空，无法更新采购状态", requestDO.getId());
            return;
        }

        // 1. 检查是否所有子项都满足 "批准数量 == 已订购数量"
        boolean allFullyOrdered = items.stream()
            .allMatch(item -> Objects.equals(item.getApproveCount(), item.getOrderedQuantity()));

        // 2. 检查是否所有子项的 "已订购数量 == 0 或 null"
        boolean allNotOrdered = items.stream()
            .allMatch(item -> item.getOrderedQuantity() == null || item.getOrderedQuantity() == 0);

        ErpOrderStatus newStatus;
        if (allFullyOrdered) {
            newStatus = ErpOrderStatus.ORDERED;  // 已采购
        } else if (allNotOrdered) {
            newStatus = ErpOrderStatus.OT_ORDERED;  // 未采购
        } else {
            newStatus = ErpOrderStatus.PARTIALLY_ORDERED;  // 部分采购
        }

        // 仅在状态变更时更新数据库
        if (!Objects.equals(requestDO.getOrderStatus(), newStatus.getCode())) {
            requestDO.setOrderStatus(newStatus.getCode());
            ThrowUtil.ifSqlThrow(purchaseRequestMapper.updateById(requestDO), GlobalErrorCodeConstants.DB_UPDATE_ERROR);
            log.debug("采购申请单状态更新：ID={}，新状态={}", requestDO.getId(), newStatus);
        }
    }
}
