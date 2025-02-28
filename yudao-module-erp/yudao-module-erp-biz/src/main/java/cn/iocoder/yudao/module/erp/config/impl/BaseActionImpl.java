package cn.iocoder.yudao.module.erp.config.impl;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import com.alibaba.cola.statemachine.Action;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public abstract class BaseActionImpl implements Action<ErpAuditStatus, ErpEventEnum, ErpPurchaseRequestDO> {

    @Resource
    private ErpPurchaseRequestMapper mapper;

    @Override
    @Transactional
    public void execute(ErpAuditStatus from, ErpAuditStatus to, ErpEventEnum event, ErpPurchaseRequestDO context) {
        log.info("状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
        updatePurchaseRequest(context, to);
    }

    protected void updatePurchaseRequest(ErpPurchaseRequestDO context, ErpAuditStatus to) {
        mapper.update(context.setStatus(to.getCode()),
            new LambdaQueryWrapperX<ErpPurchaseRequestDO>().eq(ErpPurchaseRequestDO::getId, context.getId()));
    }
}
