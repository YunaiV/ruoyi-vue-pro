package cn.iocoder.yudao.module.erp.config.purchase.order.impl.action.item;

import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpStorageStatus;
import com.alibaba.cola.statemachine.Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

//订单项入库状态机
@Component
@Slf4j
public class ActionOrderItemInImpl implements Action<ErpStorageStatus, ErpEventEnum, ErpPurchaseRequestItemsDO> {

    //入库单->订单项->订单->申请项->申请单
    @Override
    public void execute(ErpStorageStatus f, ErpStorageStatus t, ErpEventEnum erpEventEnum, ErpPurchaseRequestItemsDO context) {
        //入库数量+入库状态
    }
}
