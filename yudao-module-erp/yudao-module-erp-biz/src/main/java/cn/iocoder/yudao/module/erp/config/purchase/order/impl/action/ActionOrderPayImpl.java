package cn.iocoder.yudao.module.erp.config.purchase.order.impl.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpPaymentStatus;
import com.alibaba.cola.statemachine.Action;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Slf4j
//支付状态机
public class ActionOrderPayImpl implements Action<ErpPaymentStatus, ErpEventEnum, ErpPurchaseOrderDO> {
    @Resource
    private ErpPurchaseOrderMapper mapper;

    @Resource
    private ErpPurchaseOrderItemMapper itemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(ErpPaymentStatus from, ErpPaymentStatus to, ErpEventEnum event, ErpPurchaseOrderDO context) {
        // 参数校验
        if (context == null || context.getId() == null) {
            return;
        }

        // 获取订单项的支付状态
        List<ErpPurchaseOrderItemDO> items = itemMapper.selectListByOrderId(context.getId());
        if (CollUtil.isEmpty(items)) {
            return;
        }

        // 判断订单项的支付状态
        boolean hasUnpaid = false;
        boolean hasPartialPaid = false;
        boolean allPaid = true;

        for (ErpPurchaseOrderItemDO item : items) {
            Integer payStatus = item.getPayStatus();
            if (payStatus == null || payStatus.equals(ErpPaymentStatus.NONE_PAYMENT.getCode())) {
                hasUnpaid = true;
                allPaid = false;
            } else if (payStatus.equals(ErpPaymentStatus.PARTIALLY_PAYMENT.getCode())) {
                hasPartialPaid = true;
                allPaid = false;
            }
        }

        // 设置订单支付状态
        if (allPaid) {
            to = ErpPaymentStatus.ALL_PAYMENT;
        } else if (hasPartialPaid || (hasUnpaid && hasPartialPaid)) {
            to = ErpPaymentStatus.PARTIALLY_PAYMENT;
        } else {
            to = ErpPaymentStatus.NONE_PAYMENT;
        }
        ErpPurchaseOrderDO orderDO = mapper.selectById(context.getId());
        orderDO.setInStatus(to.getCode());
        mapper.updateById(orderDO);
        //log
        log.info("支付状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
    }
}