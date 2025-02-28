package cn.iocoder.yudao.module.erp.config.impl;


import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ActionOffImpl extends BaseActionImpl {

    @Override
    protected void updatePurchaseRequest(ErpPurchaseRequestDO context, ErpAuditStatus to) {
        // ActionOffImpl 使用 setOffStatus 更新
        super.updatePurchaseRequest(context.setOffStatus(to.getCode()), to);
    }
}