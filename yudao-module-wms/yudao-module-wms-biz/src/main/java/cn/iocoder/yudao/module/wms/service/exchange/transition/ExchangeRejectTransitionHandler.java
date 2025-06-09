package cn.iocoder.yudao.module.wms.service.exchange.transition;


import cn.iocoder.yudao.framework.cola.statemachine.builder.TransitionContext;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.WmsExchangeDO;
import cn.iocoder.yudao.module.wms.enums.exchange.WmsExchangeAuditStatus;
import org.springframework.stereotype.Component;


/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:27
 * @description: 拒绝
 */
@Component
public class ExchangeRejectTransitionHandler extends BaseExchangeTransitionHandler {

    @Override
    public void perform(Integer from, Integer to, WmsExchangeAuditStatus.Event event, TransitionContext<WmsExchangeDO> context) {
        super.perform(from, to, event, context);
    }

}
