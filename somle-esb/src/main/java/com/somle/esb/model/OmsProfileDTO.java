package com.somle.esb.model;

import com.somle.esb.enums.oms.PlatformEnum;
import com.somle.esb.enums.oms.SyncOmsTypeEnum;
import lombok.Data;


/**
 * @Description: OMS配置DTO
 * @Author: gumaomao
 * @Date: 2025/03/31
 */

@Data
public class OmsProfileDTO<T> {
    //表示是哪个销售平台，如沃尔玛，亚马逊等
    private PlatformEnum salesPlatform;
    //表示是哪个同步类型，如店铺，商品，订单等
    private SyncOmsTypeEnum syncOmsTypeEnum;
    //存储数据
    private T payload;

    public OmsProfileDTO(PlatformEnum salesPlatform, SyncOmsTypeEnum syncOmsTypeEnum, T payload) {
        this.salesPlatform = salesPlatform;
        this.syncOmsTypeEnum = syncOmsTypeEnum;
        this.payload = payload;
    }
}
