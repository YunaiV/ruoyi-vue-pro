package cn.iocoder.yudao.module.srm.config.purchase.returned.impl.action;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.ErpPurchaseReturnMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.ErpReturnStatus;
import com.alibaba.cola.statemachine.Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class ActionRefundImpl implements Action<ErpReturnStatus, SrmEventEnum, ErpPurchaseReturnDO> {
    @Autowired
    ErpPurchaseReturnMapper erpPurchaseReturnMapper;


    @Override
    @Transactional
    public void execute(ErpReturnStatus from, ErpReturnStatus to, SrmEventEnum event, ErpPurchaseReturnDO context) {
        ErpPurchaseReturnDO returnDO = erpPurchaseReturnMapper.selectById(context.getId());
        returnDO.setRefundStatus(to.getCode());
        erpPurchaseReturnMapper.updateById(returnDO);

        log.debug("退货状态机-退货-触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
    }
}
