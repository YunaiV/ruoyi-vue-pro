package cn.iocoder.yudao.module.erp.config.purchase.impl;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpAuditStatus;
import com.alibaba.cola.statemachine.Action;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseActionImpl implements Action<ErpAuditStatus, ErpEventEnum, ErpPurchaseRequestDO> {


    @Override
    public void execute(ErpAuditStatus from, ErpAuditStatus to, ErpEventEnum event, ErpPurchaseRequestDO context) {
        log.info("状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
        //可以日志持久化记录
    }

}
