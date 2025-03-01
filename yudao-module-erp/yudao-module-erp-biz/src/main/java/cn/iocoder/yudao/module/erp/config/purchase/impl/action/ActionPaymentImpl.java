package cn.iocoder.yudao.module.erp.config.purchase.impl.action;

import cn.iocoder.yudao.module.erp.config.purchase.impl.BaseActionImpl;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpAuditStatus;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ActionPaymentImpl extends BaseActionImpl {
    @Resource
    private ErpPurchaseRequestMapper mapper;

    @Override
    public void execute(ErpAuditStatus from, ErpAuditStatus to, ErpEventEnum event, ErpPurchaseRequestDO context) {
        ErpPurchaseRequestDO aDo = mapper.selectById(context.getId());
        aDo.setOrderStatus(to.getCode());
        mapper.updateById(aDo);
        super.execute(from, to, event, aDo);
    }
}
