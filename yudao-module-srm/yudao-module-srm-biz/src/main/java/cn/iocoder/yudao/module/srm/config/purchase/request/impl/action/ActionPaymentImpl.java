package cn.iocoder.yudao.module.srm.config.purchase.request.impl.action;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.ErpPaymentStatus;
import com.alibaba.cola.statemachine.Action;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class ActionPaymentImpl implements Action<ErpPaymentStatus, SrmEventEnum, ErpPurchaseRequestDO> {
    @Resource
    private ErpPurchaseRequestMapper mapper;

    @Override
    @Transactional
    public void execute(ErpPaymentStatus from, ErpPaymentStatus to, SrmEventEnum event, ErpPurchaseRequestDO context) {
        ErpPurchaseRequestDO aDo = mapper.selectById(context.getId());
        aDo.setOrderStatus(to.getCode());
        mapper.updateById(aDo);
        // //采购状态机触发({})事件：将对象{},由状态 {}->{}
        log.debug("采购申请项付款状态触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
    }
}
