package com.somle.esb.model;

import com.somle.esb.enums.SalesPlatform;
import com.somle.esb.enums.SyncOmsType;
import lombok.Data;


/**
 * @Description: OMS配置DTO
 * @Author: gumaomao
 * @Date: 2025/03/31
 */

@Data
public class OmsProfileDTO<T> {
    //表示是哪个销售平台，如沃尔玛，亚马逊等
    private SalesPlatform salesPlatform;
    //表示是哪个同步类型，如店铺，商品，订单等
    private SyncOmsType syncOmsType;
    //存储数据
    private T payload;

    public OmsProfileDTO(SalesPlatform salesPlatform, SyncOmsType syncOmsType, T payload) {
        this.salesPlatform = salesPlatform;
        this.syncOmsType = syncOmsType;
        this.payload = payload;
    }
}
