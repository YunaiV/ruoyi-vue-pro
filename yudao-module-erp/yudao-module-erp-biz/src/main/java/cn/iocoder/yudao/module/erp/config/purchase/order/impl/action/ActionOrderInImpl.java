package cn.iocoder.yudao.module.erp.config.purchase.order.impl.action;

import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpStorageStatus;
import com.alibaba.cola.statemachine.Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class ActionOrderInImpl implements Action<ErpStorageStatus, ErpEventEnum, ErpPurchaseOrderDO> {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(ErpStorageStatus from, ErpStorageStatus to, ErpEventEnum event, ErpPurchaseOrderDO context) {

    }
}
