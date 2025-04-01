package cn.iocoder.yudao.module.srm.config.purchase.request.impl.action;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseRequestItemsMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseRequestMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmOrderStatus;
import com.alibaba.cola.statemachine.Action;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class OrderActionImpl implements Action<SrmOrderStatus, SrmEventEnum, SrmPurchaseRequestDO> {

    @Resource
    private SrmPurchaseRequestMapper purchaseRequestMapper;

    @Resource
    private SrmPurchaseRequestItemsMapper purchaseRequestItemsMapper;

    @Override
    @Transactional
    public void execute(SrmOrderStatus from, SrmOrderStatus to, SrmEventEnum event, SrmPurchaseRequestDO context) {
        //        if (event == SrmEventEnum.ORDER_GOODS_ADD || event == SrmEventEnum.ORDER_GOODS_REDUCE) {
        //            updatePurchaseOrderStatus(context);
        //            return;
        //        }

        SrmPurchaseRequestDO requestDO = purchaseRequestMapper.selectById(context.getId());
        if (requestDO == null) {
            log.error("采购申请单不存在，ID: {}", context.getId());
            return;
        }
        //子数量调整
        if (event == SrmEventEnum.ORDER_ADJUSTMENT) {
            List<SrmPurchaseRequestItemsDO> requestItemsDOS = purchaseRequestItemsMapper.selectListByRequestId(requestDO.getId());
            //全部采购
            if (requestItemsDOS.stream().allMatch(item -> item.getOrderStatus().equals(SrmOrderStatus.ORDERED.getCode()))) {
                requestDO.setOrderStatus(SrmOrderStatus.ORDERED.getCode());
            }
            //部分采购
            if (requestItemsDOS.stream().anyMatch(item -> item.getOrderStatus().equals(SrmOrderStatus.PARTIALLY_ORDERED.getCode()))) {
                requestDO.setOrderStatus(SrmOrderStatus.PARTIALLY_ORDERED.getCode());
            }
            //未采购,状态都是未采购
            if (requestItemsDOS.stream().allMatch(item -> item.getOrderStatus().equals(SrmOrderStatus.OT_ORDERED.getCode()))) {
                requestDO.setOrderStatus(SrmOrderStatus.OT_ORDERED.getCode());
            }
        } else {
            //其他事件,初始化+放弃
            requestDO.setOrderStatus(to.getCode());
        }
        // 其他事件：直接更新采购申请的状态

        ThrowUtil.ifSqlThrow(purchaseRequestMapper.updateById(requestDO), GlobalErrorCodeConstants.DB_UPDATE_ERROR);
        log.debug("采购状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(),
            SrmOrderStatus.fromCode(requestDO.getOrderStatus()).getDesc());
    }

    /**
     * 更新采购申请的订单状态
     */
    private void updatePurchaseOrderStatus(SrmPurchaseRequestDO requestDO) {
        List<SrmPurchaseRequestItemsDO> items = purchaseRequestItemsMapper.selectListByRequestId(requestDO.getId());

        if (items.isEmpty()) {
            log.warn("采购申请单ID={} 为空，无法更新采购状态", requestDO.getId());
            return;
        }

        // 1. 检查是否所有子项都满足 "批准数量 == 已订购数量"
        boolean allFullyOrdered = items.stream().allMatch(item -> Objects.equals(item.getApprovedQty(), item.getOrderClosedQty()));

        // 2. 检查是否所有子项的 "已订购数量 == 0 或 null"
        boolean allNotOrdered = items.stream().allMatch(item -> item.getOrderClosedQty() == null || item.getOrderClosedQty() == 0);

        SrmOrderStatus newStatus;
        if (allFullyOrdered) {
            newStatus = SrmOrderStatus.ORDERED;  // 已采购
        } else if (allNotOrdered) {
            newStatus = SrmOrderStatus.OT_ORDERED;  // 未采购
        } else {
            newStatus = SrmOrderStatus.PARTIALLY_ORDERED;  // 部分采购
        }

        // 仅在状态变更时更新数据库
        if (!Objects.equals(requestDO.getOrderStatus(), newStatus.getCode())) {
            requestDO.setOrderStatus(newStatus.getCode());
            ThrowUtil.ifSqlThrow(purchaseRequestMapper.updateById(requestDO), GlobalErrorCodeConstants.DB_UPDATE_ERROR);
            log.debug("采购申请单状态更新：ID={}，新状态={}", requestDO.getId(), newStatus);
        }
    }
}
