package cn.iocoder.yudao.module.erp.config.impl;

import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ActionAuditImpl extends BaseActionImpl {

    @Override
    protected void updatePurchaseRequest(ErpPurchaseRequestDO context, ErpAuditStatus to) {
        // ActionAuditImpl 使用 setStatus 更新
        super.updatePurchaseRequest(context.setStatus(to.getCode()), to);
    }
}
