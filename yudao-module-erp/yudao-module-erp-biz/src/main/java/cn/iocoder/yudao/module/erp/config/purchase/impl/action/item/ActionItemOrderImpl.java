package cn.iocoder.yudao.module.erp.config.purchase.impl.action.item;

import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestItemsMapper;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpOrderStatus;
import com.alibaba.cola.statemachine.Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//订购子表状态机
@Component
@Slf4j
public class ActionItemOrderImpl implements Action<ErpOrderStatus, ErpEventEnum, ErpPurchaseRequestItemsDO> {
    @Autowired
    private ErpPurchaseRequestItemsMapper mapper;

    @Override
    public void execute(ErpOrderStatus from, ErpOrderStatus to, ErpEventEnum event, ErpPurchaseRequestItemsDO context) {

    }
}
