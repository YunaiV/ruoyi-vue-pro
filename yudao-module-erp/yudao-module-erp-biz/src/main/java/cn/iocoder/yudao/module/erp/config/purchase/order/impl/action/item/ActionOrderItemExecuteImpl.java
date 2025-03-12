package cn.iocoder.yudao.module.erp.config.purchase.order.impl.action.item;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpExecutionStatus;
import com.alibaba.cola.statemachine.Action;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class ActionOrderItemExecuteImpl implements Action<ErpExecutionStatus, ErpEventEnum, ErpPurchaseOrderItemDO> {

    @Resource
    ErpPurchaseOrderItemMapper mapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(ErpExecutionStatus from, ErpExecutionStatus to, ErpEventEnum event, ErpPurchaseOrderItemDO context) {
        ErpPurchaseOrderItemDO aDo = mapper.selectById(context.getId());
        aDo.setExecuteStatus(to.getCode());
        mapper.updateById(aDo);
        //log
        log.info("执行状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());

    }
}
