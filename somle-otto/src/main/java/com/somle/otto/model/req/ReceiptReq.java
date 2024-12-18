package com.somle.otto.model.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// 请求参数类，用于获取收据的请求
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptReq {

    private Integer limit; // 每页返回的收据数量，必须大于等于1，默认值为128

    private String salesOrderId; // 按销售订单ID过滤收据

    private String from; // 从指定日期开始过滤收据，格式为yyyy-mm-dd

    private String to; // 直到指定日期过滤收据，格式为yyyy-mm-dd

    private List<String> receiptTypes; // 按收据类型过滤，支持的值有 "PURCHASE", "REFUND", "PARTIAL_REFUND"

    private String next; // 用于获取下一批收据的游标

    // page 字段已弃用，不再使用
    // private Integer page; // 页码，默认值为1，已弃用
}
