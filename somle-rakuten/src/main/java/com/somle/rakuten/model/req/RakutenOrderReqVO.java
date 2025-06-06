package com.somle.rakuten.model.req;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RakutenOrderReqVO {

    /**
     * 最多可以指定 100 件过去
     * <p>
     * 730 天（2 年内）的订单。
     * 如：[“502763-20171027-00006701”，“502763-20171027-00006702”]
     */
    @NotNull
    private List<String> orderNumberList; // 订单编号列表，最多可以指定100个订单号（过去2年内的订单）
    /**
     * 以下任意一项
     * <p>
     * 3：兼容消费税上调
     * 4：兼容普通航运公司
     * 5：收据、预付款期限版本
     * 6：客户/交货响应警告显示详细信息
     * 7：兼容 SKU
     * 8：兼容交货质量改进系统
     * 9 ：免费送货 可分发
     * <p>
     * *将来会添加版本。
     * 请使用最新版本（数字较大的版本）。
     * *旧版本将被淘汰。时间确定后我们会尽快与您联系。
     */
    @NotNull
    private Integer version;              // 版本号，指定请求的版本，支持多个不同的版本号
}
