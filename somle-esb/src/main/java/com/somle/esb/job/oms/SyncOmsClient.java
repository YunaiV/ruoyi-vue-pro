package com.somle.esb.job.oms;

import com.somle.esb.enums.SalesPlatform;
import java.util.List;


public abstract class SyncOmsClient<S,P> {

    private final SalesPlatform salesPlatform;

    public SyncOmsClient(SalesPlatform salesPlatform) {
        this.salesPlatform = salesPlatform;
    }

    public SalesPlatform getSalesPlatform() {
        return salesPlatform;
    }

    public abstract List<S> getShops();
}
