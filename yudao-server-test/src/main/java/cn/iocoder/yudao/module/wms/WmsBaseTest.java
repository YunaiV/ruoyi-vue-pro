package cn.iocoder.yudao.module.wms;

import cn.iocoder.yudao.module.wms.inventory.WmsInventoryClient;
import cn.iocoder.yudao.module.wms.stock.WmsStockBinClient;
import cn.iocoder.yudao.module.wms.stock.WmsStockOwnershipClient;
import cn.iocoder.yudao.module.wms.warehouse.WmsWarehouseBinClient;
import cn.iocoder.yudao.module.wms.warehouse.WmsWarehouseClient;
import cn.iocoder.yudao.test.BaseRestIntegrationTest;

/**
 * @author: LeeFJ
 * @date: 2025/4/8 9:47
 * @description:
 */
public class WmsBaseTest extends BaseRestIntegrationTest {


    public WmsWarehouseClient warehouseClient() {
        return getClient(WmsWarehouseClient.class);
    }

    public WmsWarehouseBinClient warehouseBinClient() {
        return getClient(WmsWarehouseBinClient.class);
    }

    public WmsStockBinClient stockBinClient() {
        return getClient(WmsStockBinClient.class);
    }

    public WmsStockOwnershipClient stockOwnershipClient() {
        return getClient(WmsStockOwnershipClient.class);
    }

    public WmsInventoryClient inventoryClient() {
        return getClient(WmsInventoryClient.class);
    }




}
