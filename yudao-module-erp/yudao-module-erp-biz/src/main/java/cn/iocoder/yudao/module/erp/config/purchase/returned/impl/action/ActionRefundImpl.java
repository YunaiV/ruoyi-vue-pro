package cn.iocoder.yudao.module.erp.config.purchase.returned.impl.action;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseReturnMapper;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpReturnStatus;
import com.alibaba.cola.statemachine.Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class ActionRefundImpl implements Action<ErpReturnStatus, ErpEventEnum, ErpPurchaseReturnDO> {
    @Autowired
    ErpPurchaseReturnMapper erpPurchaseReturnMapper;


    @Override
    @Transactional
    public void execute(ErpReturnStatus from, ErpReturnStatus to, ErpEventEnum event, ErpPurchaseReturnDO context) {
        ErpPurchaseReturnDO returnDO = erpPurchaseReturnMapper.selectById(context.getId());
        returnDO.setAuditStatus(to.getCode());
        erpPurchaseReturnMapper.updateById(returnDO);

        log.debug("退货状态机-退货-触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
    }
}
