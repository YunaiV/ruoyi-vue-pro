package cn.iocoder.yudao.module.erp.config.purchase.in.impl.action;

import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpPaymentStatus;
import com.alibaba.cola.statemachine.Action;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class ActionInPayImpl implements Action<ErpPaymentStatus, ErpEventEnum, ErpPurchaseInDO> {
    @Resource
    private ErpPurchaseInMapper erpPurchaseInMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(ErpPaymentStatus from, ErpPaymentStatus to, ErpEventEnum event, ErpPurchaseInDO context) {

        //付款金额调整-动态状态
        if (event == ErpEventEnum.PAYMENT_ADJUSTMENT) {

        }
        erpPurchaseInMapper.updateById(context.setPayStatus(to.getCode()));
    }
}
