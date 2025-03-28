package cn.iocoder.yudao.module.srm.config.purchase.returned.impl.action;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseReturnMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmReturnStatus;
import com.alibaba.cola.statemachine.Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class RefundActionImpl implements Action<SrmReturnStatus, SrmEventEnum, SrmPurchaseReturnDO> {
    @Autowired
    SrmPurchaseReturnMapper srmPurchaseReturnMapper;


    @Override
    @Transactional
    public void execute(SrmReturnStatus from, SrmReturnStatus to, SrmEventEnum event, SrmPurchaseReturnDO context) {
        SrmPurchaseReturnDO returnDO = srmPurchaseReturnMapper.selectById(context.getId());
        returnDO.setRefundStatus(to.getCode());
        srmPurchaseReturnMapper.updateById(returnDO);

        log.debug("退货状态机-退货-触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
    }
}
