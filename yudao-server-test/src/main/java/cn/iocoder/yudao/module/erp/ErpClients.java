package cn.iocoder.yudao.module.erp;

import cn.iocoder.yudao.module.erp.product.ErpProductClient;
import cn.iocoder.yudao.test.BaseRestIntegrationTest;

/**
 * @author: LeeFJ
 * @date: 2025/4/17 11:23
 * @description:
 */
public class ErpClients {

    private BaseRestIntegrationTest baseRestIntegrationTest;

    public ErpClients(BaseRestIntegrationTest baseRestIntegrationTest) {
        this.baseRestIntegrationTest = baseRestIntegrationTest;
    }

    public ErpProductClient productClient() {
        return this.baseRestIntegrationTest.getClient(ErpProductClient.class);
    }

}
