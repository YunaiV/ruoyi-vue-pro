package cn.iocoder.yudao.module.wms;

import cn.iocoder.yudao.module.wms.exchange.WmsExchangeClient;
import cn.iocoder.yudao.module.wms.inbound.WmsInboundClient;
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
public class WmsClients {

    private BaseRestIntegrationTest baseRestIntegrationTest;

    public WmsClients(BaseRestIntegrationTest baseRestIntegrationTest) {
        this.baseRestIntegrationTest = baseRestIntegrationTest;
    }

    public WmsWarehouseClient warehouseClient() {
        return this.baseRestIntegrationTest.getClient(WmsWarehouseClient.class);
    }

    public WmsWarehouseBinClient warehouseBinClient() {
        return this.baseRestIntegrationTest.getClient(WmsWarehouseBinClient.class);
    }

    public WmsStockBinClient stockBinClient() {
        return this.baseRestIntegrationTest.getClient(WmsStockBinClient.class);
    }

    public WmsStockOwnershipClient stockOwnershipClient() {
        return this.baseRestIntegrationTest.getClient(WmsStockOwnershipClient.class);
    }

    public WmsInventoryClient inventoryClient() {
        return this.baseRestIntegrationTest.getClient(WmsInventoryClient.class);
    }

    public WmsInboundClient inboundClient() {
        return this.baseRestIntegrationTest.getClient(WmsInboundClient.class);
    }

    public WmsExchangeClient exchangeClient() {
        return this.baseRestIntegrationTest.getClient(WmsExchangeClient.class);
    }









}
