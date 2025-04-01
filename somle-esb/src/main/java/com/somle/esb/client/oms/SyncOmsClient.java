package com.somle.esb.client.oms;

import com.somle.esb.enums.oms.SalesPlatformEnum;

import java.util.List;


/**
 * @Description: 公共类，获取各销售平台各类型数据，每一个销售平台对应一个子类
 * @Author: gumaomao
 * @Date: 2025/03/31
 */

public abstract class SyncOmsClient<S, P> {

    private final SalesPlatformEnum salesPlatform;

    public SyncOmsClient(SalesPlatformEnum salesPlatform) {
        this.salesPlatform = salesPlatform;
    }

    public SalesPlatformEnum getSalesPlatform() {
        return salesPlatform;
    }

    /**
     * @Author: gumaomao
     * @Date: 2025/03/31
     * @Description: 从销售平台获取店铺信息
     * @return: @return {@link List }<{@link }>
     */
    public abstract List<S> getShops();

    public abstract List<P> getProducts();
}
