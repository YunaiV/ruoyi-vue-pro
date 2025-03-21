package cn.iocoder.yudao.module.srm.config.purchase.order.impl.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.ErpPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.ErpPaymentStatus;
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
public class ActionOrderPayImpl implements Action<ErpPaymentStatus, SrmEventEnum, ErpPurchaseOrderDO> {
    @Resource
    private ErpPurchaseOrderMapper mapper;

    @Resource
    private ErpPurchaseOrderItemMapper itemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(ErpPaymentStatus from, ErpPaymentStatus to, SrmEventEnum event, ErpPurchaseOrderDO context) {
        // 参数校验
        if (context == null || context.getId() == null) {
            return;
        }


        if (event == PAYMENT_ADJUSTMENT) {//付款调整->调整状态,根据子表来设置状态
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
        }

        ErpPurchaseOrderDO orderDO = mapper.selectById(context.getId());
        orderDO.setPayStatus(to.getCode());
        mapper.updateById(orderDO);
        //log
        log.debug("支付状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
    }
}