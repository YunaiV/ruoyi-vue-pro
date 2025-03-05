package cn.iocoder.yudao.module.erp.config.purchase.request.impl.action;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpStorageStatus;
import com.alibaba.cola.statemachine.Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class ActionStorageImpl implements Action<ErpStorageStatus, ErpEventEnum, ErpPurchaseRequestDO> {


    @Override
    @Transactional
    public void execute(ErpStorageStatus from, ErpStorageStatus to, ErpEventEnum event, ErpPurchaseRequestDO context) {
        log.info("入库状态机执行成功,订单：{}，事件：{}，from状态({})", JSONUtil.toJsonStr(context), event.getDesc(), from.getDesc());
    }
}
