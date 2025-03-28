package cn.iocoder.yudao.module.srm.config.purchase.order.impl.action.item;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmExecutionStatus;
import com.alibaba.cola.statemachine.Action;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class OrderItemExecuteActionImpl implements Action<SrmExecutionStatus, SrmEventEnum, SrmPurchaseOrderItemDO> {

    @Resource
    SrmPurchaseOrderItemMapper mapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(SrmExecutionStatus from, SrmExecutionStatus to, SrmEventEnum event, SrmPurchaseOrderItemDO context) {
        SrmPurchaseOrderItemDO aDo = mapper.selectById(context.getId());
        aDo.setExecuteStatus(to.getCode());
        mapper.updateById(aDo);
        //log
        log.debug("执行状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());

    }
}
