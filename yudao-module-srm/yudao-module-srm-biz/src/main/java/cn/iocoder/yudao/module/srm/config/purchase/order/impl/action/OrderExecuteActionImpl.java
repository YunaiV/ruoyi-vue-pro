package cn.iocoder.yudao.module.srm.config.purchase.order.impl.action;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseOrderMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmExecutionStatus;
import com.alibaba.cola.statemachine.Action;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class OrderExecuteActionImpl implements Action<SrmExecutionStatus, SrmEventEnum, SrmPurchaseOrderDO> {
    @Resource
    SrmPurchaseOrderMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(SrmExecutionStatus from, SrmExecutionStatus to, SrmEventEnum event, SrmPurchaseOrderDO context) {
        SrmPurchaseOrderDO orderDO = mapper.selectById(context.getId());
        orderDO.setExecuteStatus(to.getCode());
        mapper.updateById(orderDO);
        //log
        log.debug("执行状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
    }
}