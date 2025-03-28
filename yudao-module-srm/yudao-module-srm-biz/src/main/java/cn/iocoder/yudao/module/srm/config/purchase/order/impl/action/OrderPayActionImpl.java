package cn.iocoder.yudao.module.srm.config.purchase.order.impl.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseOrderMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmPaymentStatus;
import com.alibaba.cola.statemachine.Action;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static cn.iocoder.yudao.module.srm.enums.SrmEventEnum.PAYMENT_ADJUSTMENT;

@Component
@Slf4j
//支付状态机
public class OrderPayActionImpl implements Action<SrmPaymentStatus, SrmEventEnum, SrmPurchaseOrderDO> {
    @Resource
    private SrmPurchaseOrderMapper mapper;

    @Resource
    private SrmPurchaseOrderItemMapper itemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(SrmPaymentStatus from, SrmPaymentStatus to, SrmEventEnum event, SrmPurchaseOrderDO context) {
        // 参数校验
        if (context == null || context.getId() == null) {
            return;
        }


        if (event == PAYMENT_ADJUSTMENT) {//付款调整->调整状态,根据子表来设置状态
            // 获取订单项的支付状态
            List<SrmPurchaseOrderItemDO> items = itemMapper.selectListByOrderId(context.getId());
            if (CollUtil.isEmpty(items)) {
                return;
            }
            // 判断订单项的支付状态
            boolean hasUnpaid = false;
            boolean hasPartialPaid = false;
            boolean allPaid = true;

            for (SrmPurchaseOrderItemDO item : items) {
                Integer payStatus = item.getPayStatus();
                if (payStatus == null || payStatus.equals(SrmPaymentStatus.NONE_PAYMENT.getCode())) {
                    hasUnpaid = true;
                    allPaid = false;
                } else if (payStatus.equals(SrmPaymentStatus.PARTIALLY_PAYMENT.getCode())) {
                    hasPartialPaid = true;
                    allPaid = false;
                }
            }

            // 设置订单支付状态
            if (allPaid) {
                to = SrmPaymentStatus.ALL_PAYMENT;
            } else if (hasPartialPaid || (hasUnpaid && hasPartialPaid)) {
                to = SrmPaymentStatus.PARTIALLY_PAYMENT;
            } else {
                to = SrmPaymentStatus.NONE_PAYMENT;
            }
        }

        SrmPurchaseOrderDO orderDO = mapper.selectById(context.getId());
        orderDO.setPayStatus(to.getCode());
        mapper.updateById(orderDO);
        //log
        log.debug("订单主表支付状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
    }
}