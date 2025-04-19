package cn.iocoder.yudao.module.wms.service.inventory.transition;


import cn.iocoder.yudao.framework.cola.statemachine.builder.TransitionContext;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryDO;
import cn.iocoder.yudao.module.wms.enums.inventory.WmsInventoryAuditStatus;
import cn.iocoder.yudao.module.wms.service.inventory.bin.WmsInventoryBinService;
import cn.iocoder.yudao.module.wms.service.inventory.product.WmsInventoryProductService;
import cn.iocoder.yudao.module.wms.service.quantity.InventoryExecutor;
import cn.iocoder.yudao.module.wms.service.quantity.context.InventoryContext;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;


/**
 * @author: LeeFJ
 * @date: 2025/3/31 16:25
 * @description: 同意
 */
@Component
public class InventoryAgreeTransitionHandler extends BaseInventoryTransitionHandler {

    @Resource
    private InventoryExecutor inventoryExecutor;

    @Resource
    private WmsInventoryProductService inventoryProductService;

    @Resource
    private WmsInventoryBinService inventoryBinService;


    @Override
    public void perform(Integer from, Integer to, WmsInventoryAuditStatus.Event event, TransitionContext<WmsInventoryDO> context) {
        super.perform(from, to, event, context);

        InventoryContext inventoryContext = new InventoryContext();
        inventoryContext.setInventoryDO(context.data());
        inventoryContext.setInventoryProductDOList(inventoryProductService.selectByInventoryId(context.data().getId()));
        inventoryContext.setWmsInventoryBinDOList(inventoryBinService.selectByInventoryId(context.data().getId()));
        inventoryExecutor.execute(inventoryContext);
    }

}
