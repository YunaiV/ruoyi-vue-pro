package cn.iocoder.yudao.module.wms.service.exchange.transition;


import cn.iocoder.yudao.framework.cola.statemachine.builder.TransitionContext;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.WmsExchangeDO;
import cn.iocoder.yudao.module.wms.enums.exchange.WmsExchangeAuditStatus;
import cn.iocoder.yudao.module.wms.service.exchange.item.WmsExchangeItemService;
import cn.iocoder.yudao.module.wms.service.quantity.ExchangeExecutor;
import cn.iocoder.yudao.module.wms.service.quantity.context.ExchangeContext;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;


/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:25
 * @description: 同意
 */
@Component
public class ExchangeAgreeTransitionHandler extends BaseExchangeTransitionHandler {

    @Resource
    private ExchangeExecutor exchangeExecutor;

    @Resource
    private WmsExchangeItemService exchangeItemService;



    @Override
    public void perform(Integer from, Integer to, WmsExchangeAuditStatus.Event event, TransitionContext<WmsExchangeDO> context) {
        super.perform(from, to, event, context);

        ExchangeContext exchangeContext = new ExchangeContext();
        exchangeContext.setExchangeDO(context.data());
        exchangeContext.setExchangeItemDOList(exchangeItemService.selectByExchangeId(context.data().getId()));
        exchangeExecutor.execute(exchangeContext);
    }

}
