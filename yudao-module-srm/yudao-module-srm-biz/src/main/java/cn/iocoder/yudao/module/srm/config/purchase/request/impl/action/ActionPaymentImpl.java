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
        log.debug("付款状态机执行成功,订单：{}，事件：{}，from状态({})", JSONUtil.toJsonStr(context), event.getDesc(), from.getDesc());
    }
}
