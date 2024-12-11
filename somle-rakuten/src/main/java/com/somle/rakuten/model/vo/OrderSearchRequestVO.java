package com.somle.rakuten.model.vo;


import java.time.LocalDateTime;
import java.util.List;

import com.somle.rakuten.model.PaginationRequestModel;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderSearchRequestVO {

    private List<Integer> orderProgressList;            // 订单状态列表，包含状态代码（如：100: 注单确认待、200: 处理中的订单等）
    private List<Integer> subStatusIdList;              // 子状态ID列表，多个ID可以同时指定，[-1]表示未设置子状态的订单
    @NotNull
    private Integer dateType;                           // 期间搜索类型，指定是基于哪个日期字段来进行搜索（如：1: 订单日、2: 订单确认日等）
    @NotNull
    private LocalDateTime startDatetime;                // 搜索开始时间，要求是过去730天内的日期
    @NotNull
    private LocalDateTime endDatetime;                  // 搜索结束时间，起始时间的63天内
    private List<Integer> orderTypeList;                // 销售类型列表，指定要查询的销售方式（如：1: 普通购买、4: 定期购买等）
    
    private Integer settlementMethod;                   // 支付方式，指定订单的支付方式（如：1: 信用卡、2: 货到付款等）
    
    private String deliveryName;                        // 配送方式，如：宅配便
    
    private Integer shippingDateBlankFlag;              // 是否指定了发货日期，1表示未指定发货日期的订单
    
    private Integer shippingNumberBlankFlag;            // 是否指定了运输单号，1表示未指定单号的订单
    
    private Integer searchKeywordType;                  // 搜索关键字类型，指定搜索的字段（如：1: 商品名、2: 商品编号等）
    private String searchKeyword;                       // 搜索的关键字，可以是商品名、订单号等
    
    private Integer mailSendType;                       // 邮件发送类型，指定是PC还是移动设备上的订单邮件
    
    private String ordererMailAddress;                  // 订单者的邮件地址，用于完全匹配
    
    private Integer phoneNumberType;                    // 电话号码类型，指定是订单者还是收货人的电话号码
    
    private String phoneNumber;                         // 完全匹配的电话号码
    
    private String reserveNumber;                       // 申请编号，用于完全匹配
    
    private Integer purchaseSiteType;                   // 购买站点类型，指定是PC、移动设备还是智能手机的订单
    
    private Integer asurakuFlag;                        // 是否为次日达订单，1表示次日达
    
    private Integer couponUseFlag;                      // 是否使用了优惠券，1表示使用了优惠券
    
    private Integer drugFlag;                           // 是否包含医药品，1表示包含医药品的订单
    
    private Integer overseasFlag;                       // 是否为海外购物车订单，1表示是海外购物车
    
    private Integer oneDayOperationFlag;                // 是否为当天出货订单，1表示当天出货的订单
    private PaginationRequestModel paginationRequestModel; // 分页请求模型，用于处理分页查询
}