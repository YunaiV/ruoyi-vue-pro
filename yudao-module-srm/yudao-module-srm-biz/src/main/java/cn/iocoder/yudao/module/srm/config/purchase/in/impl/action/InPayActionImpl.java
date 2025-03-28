package cn.iocoder.yudao.module.srm.config.purchase.in.impl.action;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInItemDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseInItemMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseInMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmPaymentStatus;
import com.alibaba.cola.statemachine.Action;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
public class InPayActionImpl implements Action<SrmPaymentStatus, SrmEventEnum, SrmPurchaseInDO> {
    @Resource
    private SrmPurchaseInMapper srmPurchaseInMapper;
    @Autowired
    private SrmPurchaseInItemMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(SrmPaymentStatus from, SrmPaymentStatus to, SrmEventEnum event, SrmPurchaseInDO context) {

        SrmPurchaseInDO srmPurchaseInDO = srmPurchaseInMapper.selectById(context.getId());

        // 付款金额调整 - 动态状态
        if (event == SrmEventEnum.PAYMENT_ADJUSTMENT) {
            // 查询当前采购入库单的所有子项
            List<SrmPurchaseInItemDO> itemDOS = mapper.selectListByInId(context.getId());

            // 标记状态
            boolean allUnpaid = true;
            boolean allPaid = true;
            boolean hasPartialPayment = false;

            // 遍历所有子项，检查支付状态
            for (SrmPurchaseInItemDO itemDO : itemDOS) {
                int payStatus = itemDO.getPayStatus();

                if (payStatus == SrmPaymentStatus.NONE_PAYMENT.getCode()) {
                    allPaid = false;  // 存在未付款子项
                } else if (payStatus == SrmPaymentStatus.PARTIALLY_PAYMENT.getCode()) {
                    hasPartialPayment = true;  // 存在部分付款子项
                    allUnpaid = false;
                    allPaid = false;
                } else if (payStatus == SrmPaymentStatus.ALL_PAYMENT.getCode()) {
                    allUnpaid = false;  // 存在已付款子项
                }
            }

            // 如果所有子项都是未付款，主单保持为未付款状态
            if (allUnpaid) {
                // 主单状态保持为未付款
                log.debug("所有子项均为未付款，采购入库单ID: {}", context.getId());
                to = SrmPaymentStatus.NONE_PAYMENT;
            }
            // 如果所有子项都是已付款，更新主单为已付款状态
            if (allPaid) {
                to = SrmPaymentStatus.ALL_PAYMENT;
                log.debug("所有子项均为已付款，采购入库单ID: {} 付款状态已更新为已付款", context.getId());
            }
            // 如果存在部分付款的子项，更新主单为部分付款状态
            if (hasPartialPayment) {
                to = SrmPaymentStatus.PARTIALLY_PAYMENT;
                log.debug("存在部分付款的子项，采购入库单ID: {} 付款状态已更新为部分付款", context.getId());
            }
        }

        srmPurchaseInMapper.updateById(srmPurchaseInDO.setPayStatus(to.getCode()));
        log.debug("入库主单付款状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(srmPurchaseInDO), from.getDesc(), to.getDesc());
    }

}
