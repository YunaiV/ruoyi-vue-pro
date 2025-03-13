package cn.iocoder.yudao.module.erp.config.purchase.in.impl.action;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpAuditStatus;
import com.alibaba.cola.statemachine.Action;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
public class ActionInAuditImpl implements Action<ErpAuditStatus, ErpEventEnum, ErpPurchaseInDO> {


    @Resource
    private ErpPurchaseInMapper erpPurchaseInMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(ErpAuditStatus from, ErpAuditStatus to, ErpEventEnum event, ErpPurchaseInDO context) {
        erpPurchaseInMapper.updateById(context.setAuditorStatus(to.getCode()));
        //log
        log.info("入库状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
    }
}
