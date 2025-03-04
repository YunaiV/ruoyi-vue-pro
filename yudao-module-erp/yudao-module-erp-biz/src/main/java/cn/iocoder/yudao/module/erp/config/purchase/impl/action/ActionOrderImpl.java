package cn.iocoder.yudao.module.erp.config.purchase.impl.action;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpOrderStatus;
import com.alibaba.cola.statemachine.Action;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class ActionOrderImpl implements Action<ErpOrderStatus, ErpEventEnum, ErpPurchaseRequestDO> {
    @Resource
    private ErpPurchaseRequestMapper mapper;

    @Override
    @Transactional
    public void execute(ErpOrderStatus from, ErpOrderStatus to, ErpEventEnum event, ErpPurchaseRequestDO context) {
        ErpPurchaseRequestDO aDo = mapper.selectById(context.getId());
        aDo.setOrderStatus(to.getCode());
        ThrowUtil.ifSqlThrow( mapper.updateById(aDo), GlobalErrorCodeConstants.DB_UPDATE_ERROR);
        log.info("采购状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
    }
}
