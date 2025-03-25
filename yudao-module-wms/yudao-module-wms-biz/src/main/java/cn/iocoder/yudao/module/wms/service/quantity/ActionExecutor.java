package cn.iocoder.yudao.module.wms.service.quantity;

import cn.iocoder.yudao.module.wms.dal.redis.lock.WmsLockRedisDAO;
import cn.iocoder.yudao.module.wms.enums.stock.StockReason;
import cn.iocoder.yudao.module.wms.service.inbound.WmsInboundService;
import cn.iocoder.yudao.module.wms.service.quantity.context.ActionContext;
import cn.iocoder.yudao.module.wms.service.stock.bin.WmsStockBinService;
import cn.iocoder.yudao.module.wms.service.stock.flow.WmsStockFlowService;
import cn.iocoder.yudao.module.wms.service.stock.ownership.WmsStockOwnershipService;
import cn.iocoder.yudao.module.wms.service.stock.warehouse.WmsStockWarehouseService;
import jakarta.annotation.Resource;
import lombok.Getter;
import org.springframework.context.annotation.Lazy;

public abstract class ActionExecutor<T extends ActionContext> {

    @Getter
    private StockReason reason;

    @Resource
    protected WmsLockRedisDAO lockRedisDAO;

    @Resource
    @Lazy
    protected WmsInboundService inboundService;

    @Resource
    @Lazy
    protected WmsStockFlowService stockFlowService;

    @Resource
    @Lazy
    protected WmsStockWarehouseService stockWarehouseService;

    @Resource
    @Lazy
    protected WmsStockOwnershipService stockOwnershipService;

    @Resource
    @Lazy
    protected WmsStockBinService stockBinService;


    public ActionExecutor(StockReason reason) {
        this.reason = reason;
    }


    public abstract void execute(T context);
}
