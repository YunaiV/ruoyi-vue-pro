package cn.iocoder.yudao.module.erp.config.purchase.in.impl.action.item;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseInItemMapper;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpPaymentStatus;
import com.alibaba.cola.statemachine.Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class ActionInPayItemImpl implements Action<ErpPaymentStatus, ErpEventEnum, ErpPurchaseInItemDO> {
    @Autowired
    private ErpPurchaseInItemMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(ErpPaymentStatus from, ErpPaymentStatus to, ErpEventEnum event, ErpPurchaseInItemDO context) {

        ErpPurchaseInItemDO inItemDO = mapper.selectById(context.getId());

        // 付款金额调整 - 动态状态
//        if (event == ErpEventEnum.PAYMENT_ADJUSTMENT) {
//            BigDecimal paymentPrice = inItemDO.getProductPrice(); // 已支付金额
//            BigDecimal totalProductPrice = inItemDO.getTotalProductPrice(); // 合计产品金额
//
//            if (paymentPrice.compareTo(totalProductPrice) >= 0) {
//                // 完全支付
//                to = ErpPaymentStatus.ALL_PAYMENT;
//            } else if (paymentPrice.compareTo(BigDecimal.ZERO) == 0) {
//                // 未支付
//                to = ErpPaymentStatus.NONE_PAYMENT;
//            } else {
//                // 部分支付
//                to = ErpPaymentStatus.PARTIALLY_PAYMENT;
//            }
//        }

        // 更新数据库状态
        inItemDO.setPayStatus(to.getCode());
        mapper.updateById(inItemDO);
        log.debug("付款状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
    }
}
