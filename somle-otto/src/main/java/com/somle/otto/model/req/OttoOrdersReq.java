package com.somle.otto.model.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Otto订单请求参数类
 * 用于构造查询Otto平台订单的请求参数。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OttoOrdersReq {

    private String fromDate; // 定义返回的订单最早变更日期（ISO 8601）
    private String fromOrderDate; // 只返回该日期之后的订单，2024-12-01，默认0区
    private String toOrderDate; // 只返回该日期之前的订单
    private List<String> fulfillmentStatus; // 订单的履行状态列表
    private Integer limit = 128; // 每次请求返回的订单数量，默认最大128
    private String orderDirection = "ASC"; // 排序方式，默认按升序（ASC）
    private String orderColumnType = "ORDER_LIFECYCLE_DATE"; // 排序依据，默认按订单生命周期日期
    private String mode = "BUCKET"; // 搜索模式，默认BUCKET模式
    private String nextcursor; // 用于分页查询的游标


}

