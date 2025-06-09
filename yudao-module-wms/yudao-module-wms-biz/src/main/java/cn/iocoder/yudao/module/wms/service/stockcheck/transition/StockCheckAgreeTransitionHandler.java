package cn.iocoder.yudao.module.wms.service.stockcheck.transition;


import cn.iocoder.yudao.framework.cola.statemachine.builder.TransitionContext;
import cn.iocoder.yudao.module.wms.dal.dataobject.stockcheck.WmsStockCheckDO;
import cn.iocoder.yudao.module.wms.enums.stock.check.WmsStockCheckAuditStatus;
import cn.iocoder.yudao.module.wms.service.quantity.StockCheckExecutor;
import cn.iocoder.yudao.module.wms.service.quantity.context.StockCheckContext;
import cn.iocoder.yudao.module.wms.service.stockcheck.bin.WmsStockCheckBinService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;


/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:25
 * @description: 同意
 */
@Component
public class StockCheckAgreeTransitionHandler extends BaseStockCheckTransitionHandler {

    @Resource
    private StockCheckExecutor stockCheckExecutor;

    @Resource
    private WmsStockCheckBinService stockCheckBinService;


    @Override
    public void perform(Integer from, Integer to, WmsStockCheckAuditStatus.Event event, TransitionContext<WmsStockCheckDO> context) {
        super.perform(from, to, event, context);

        StockCheckContext stockCheckContext = new StockCheckContext();
        stockCheckContext.setStockCheckDO(context.data());
        stockCheckContext.setWmsStockCheckBinDOList(stockCheckBinService.selectByStockCheckId(context.data().getId()));
        stockCheckExecutor.execute(stockCheckContext);
    }

}
