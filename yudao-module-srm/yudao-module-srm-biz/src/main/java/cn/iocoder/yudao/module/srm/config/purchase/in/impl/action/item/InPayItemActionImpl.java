package cn.iocoder.yudao.module.srm.config.purchase.in.impl.action.item;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.cola.statemachine.Action;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.module.srm.config.machine.SrmPayCountContext;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInItemDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseInItemMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseInMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmPaymentStatus;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.PURCHASE_IN_PAYMENT_STATE_MACHINE;
import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.PURCHASE_ORDER_ITEM_PAYMENT_STATE_MACHINE_NAME;

/**
 * 入库付款 -> 订单项付款状态机
 * <p>
 * 产品总价 -> 付款金额
 */
@Slf4j
@Component
public class InPayItemActionImpl implements Action<SrmPaymentStatus, SrmEventEnum, SrmPurchaseInItemDO> {
    @Autowired
    private SrmPurchaseInItemMapper mapper;
    @Resource(name = PURCHASE_IN_PAYMENT_STATE_MACHINE)
    @Lazy
    private StateMachine<SrmPaymentStatus, SrmEventEnum, SrmPurchaseInDO> stateMachine;
    @Resource(name = PURCHASE_ORDER_ITEM_PAYMENT_STATE_MACHINE_NAME)
    @Lazy
    private StateMachine<SrmPaymentStatus, SrmEventEnum, SrmPayCountContext> purchaseOrderItemPaymentStateMachine;
    @Autowired
    private SrmPurchaseInMapper srmPurchaseInMapper;
    @Autowired
    private SrmPurchaseOrderItemMapper srmPurchaseOrderItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(SrmPaymentStatus from, SrmPaymentStatus to, SrmEventEnum event, SrmPurchaseInItemDO context) {

        SrmPurchaseInItemDO inItemDO = mapper.selectById(context.getId());

//         付款金额调整 - 动态状态
        if (event == SrmEventEnum.PAYMENT_ADJUSTMENT) {
            BigDecimal paymentPrice = inItemDO.getPayPrice(); // 已支付金额
            BigDecimal totalProductPrice = inItemDO.getTotalPrice(); // 合计产品金额

            if (paymentPrice.compareTo(totalProductPrice) >= 0) {
                // 完全支付
                to = SrmPaymentStatus.ALL_PAYMENT;
            } else if (paymentPrice.compareTo(BigDecimal.ZERO) == 0) {
                // 未支付
                to = SrmPaymentStatus.NONE_PAYMENT;
            } else {
                // 部分支付
                to = SrmPaymentStatus.PARTIALLY_PAYMENT;
            }
        }

        // 更新数据库状态
        inItemDO.setPayStatus(to.getCode());
        mapper.updateById(inItemDO);
        log.debug("入库子项付款状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
        //传递给主表状态机
        toIn(inItemDO);
        //付款 传递给订单
        toOrderItemPay(inItemDO, event);
    }

    private void toOrderItemPay(SrmPurchaseInItemDO inItemDO, SrmEventEnum event) {
        Long orderItemId = inItemDO.getOrderItemId();
        SrmPurchaseOrderItemDO orderItemDO = srmPurchaseOrderItemMapper.selectById(orderItemId);
        BigDecimal totalPrice = inItemDO.getPayPrice();//已付款金额

//        if (event == SrmEventEnum.CANCEL_PAYMENT) {
//            totalPrice = totalPrice.negate();//取反
//        }
        purchaseOrderItemPaymentStateMachine.fireEvent(SrmPaymentStatus.fromCode(orderItemDO.getPayStatus()), SrmEventEnum.PAYMENT_ADJUSTMENT,
            SrmPayCountContext.builder().orderItemId(orderItemId).payCountDiff(totalPrice).build());
    }

    private void toIn(SrmPurchaseInItemDO inItemDO) {
        SrmPurchaseInDO srmPurchaseInDO = srmPurchaseInMapper.selectById(inItemDO.getArriveId());
        //付款调整->主表动态判断
        stateMachine.fireEvent(SrmPaymentStatus.fromCode(srmPurchaseInDO.getPayStatus()), SrmEventEnum.PAYMENT_ADJUSTMENT, srmPurchaseInDO);
    }

}
