package cn.iocoder.yudao.module.erp.config.purchase.impl.action;

import cn.iocoder.yudao.module.erp.config.purchase.impl.BaseActionImpl;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpAuditStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
public class ActionAuditImpl extends BaseActionImpl {
    @Autowired
    private ErpPurchaseRequestMapper mapper;

    @Override
    @Transactional
    public void execute(ErpAuditStatus from, ErpAuditStatus to, ErpEventEnum event, ErpPurchaseRequestDO context) {
        ErpPurchaseRequestDO purchaseRequestDO = mapper.selectById(context.getId());
        purchaseRequestDO.setStatus(to.getCode());
        mapper.updateById(purchaseRequestDO);
        super.execute(from, to, event, context);
    }
}
