package com.somle.esb.model;

import com.somle.esb.enums.SalesPlatform;
import com.somle.esb.enums.SyncOmsType;
import lombok.Data;


@Data
public class ShopProfileDTO<T> {
    private SalesPlatform salesPlatform;
    private SyncOmsType syncOmsType;
    private T payload;

    public ShopProfileDTO(SalesPlatform salesPlatform, SyncOmsType syncOmsType, T payload) {
        this.salesPlatform = salesPlatform;
        this.syncOmsType = syncOmsType;
        this.payload = payload;
    }
}
