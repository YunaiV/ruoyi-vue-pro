package cn.iocoder.yudao.module.wms.service.stockcheck.transition;


import cn.iocoder.yudao.framework.cola.statemachine.builder.TransitionContext;
import cn.iocoder.yudao.module.wms.dal.dataobject.stockcheck.WmsStockCheckDO;
import cn.iocoder.yudao.module.wms.enums.stock.check.WmsStockCheckAuditStatus;
import org.springframework.stereotype.Component;


/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:27
 * @description: 拒绝
 */
@Component
public class StockCheckRejectTransitionHandler extends BaseStockCheckTransitionHandler {

    @Override
    public void perform(Integer from, Integer to, WmsStockCheckAuditStatus.Event event, TransitionContext<WmsStockCheckDO> context) {
        super.perform(from, to, event, context);
    }

}
