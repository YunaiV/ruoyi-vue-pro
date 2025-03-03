package cn.iocoder.yudao.module.erp.config.purchase.impl.action;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpAuditStatus;
import com.alibaba.cola.statemachine.Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
public class ActionAuditImpl implements Action<ErpAuditStatus, ErpEventEnum, ErpPurchaseRequestDO> {
    @Autowired
    private ErpPurchaseRequestMapper mapper;

    @Override
    @Transactional
    public void execute(ErpAuditStatus from, ErpAuditStatus to, ErpEventEnum event, ErpPurchaseRequestDO context) {
        ErpPurchaseRequestDO purchaseRequestDO = mapper.selectById(context.getId());
        purchaseRequestDO.setStatus(to.getCode());
        mapper.updateById(purchaseRequestDO);
        log.info("审核状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
    }
}
