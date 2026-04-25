package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.service.inventory.InventoryService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 库存初始化 Agent。
 *
 * <p>职责：在 {@link ChainAgent} 成功落库后，为新链码商品初始化默认库存（10件）。</p>
 *
 * <p>此 Agent 作为商品创建流水线的一部分，确保每个新商品在创建时即拥有可售库存。</p>
 */
@Component
public class InventoryInitAgent implements Agent {

    @Resource
    private InventoryService inventoryService;

    @Override
    public Context run(Context ctx) {
        inventoryService.initInventory(ctx.chainCode);
        return ctx;
    }

}
