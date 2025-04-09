package com.somle.home24.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

//发票类
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Home24InvoiceDTO {
    private String currencyIsoCode;  // 货币ISO代码
    private LocalDateTime dateCreated;  // 发票创建时间
    private List<Detail> details;  // 发票详细信息
    private LocalDateTime dueDate;  // 发票到期时间
    private LocalDateTime endTime;  // 发票结束时间
    private String id;  // 发票唯一标识
    private int invoiceId;  // 发票ID
    private LocalDateTime issueDate;  // 发票发放时间
    private IssuingUser issuingUser;  // 发票发放用户信息
    private Payment payment;  // 支付信息
    private PaymentInfo paymentInfo;  // 支付信息详细
    private ShopAddress shopAddress;  // 商店地址信息
    private String shopCorporateName;  // 商店企业名称
    private int shopId;  // 商店ID
    private String shopModel;  // 商店模式
    private String shopName;  // 商店名称
    private String shopTaxNumber;  // 商店税号
    private LocalDateTime startTime;  // 发票开始时间
    private String state;  // 发票状态
    private Summary summary;  // 发票的摘要信息
    private double totalAmountExclTaxes;  // 不含税总金额
    private double totalAmountInclTaxes;  // 含税总金额
    private double totalChargedAmount;  // 总收费金额
    private List<Tax> totalTaxes;  // 总税额
    private String type;  // 发票类型

    @Data
    public static class Detail {
        private double amountExclTaxes;  // 不含税金额
        private String description;  // 描述
        private int quantity;  // 数量
        private List<Tax> taxes;  // 税务信息
    }

    @Data
    public static class Tax {
        private double amount;  // 税额
        private String code;  // 税务代码
    }

    @Data
    public static class IssuingUser {
        private String information;  // 用户信息
        private String type;  // 用户类型（例如 BATCH）
    }

    @Data
    public static class Payment {
        private String reference;  // 参考信息
        private String state;  // 支付状态（例如 PAID）
        private LocalDateTime transactionDate;  // 交易日期
    }

    @Data
    public static class PaymentInfo {
        private String type;  // 支付类型（例如 IBAN）
        private String bankName;  // 银行名称
        private String bankStreet;  // 银行地址
        private String bic;  // BIC代码
        private String iban;  // IBAN代码
        private String owner;  // 账户持有人
    }

    @Data
    public static class ShopAddress {
        private String city;  // 城市
        private String civility;  // 称谓（例如 "Mrs"）
        private String country;  // 国家
        private String firstname;  // 名字
        private String lastname;  // 姓氏
        private String phone;  // 电话号码
        private String street1;  // 街道地址
        private String zipCode;  // 邮政编码
    }

    @Data
    public static class Summary {
        private double amountTransferred;  // 转账金额
        private double amountTransferredToOperator;  // 转账给运营商的金额
        private double totalCommissionsExclTax;  // 总佣金（不含税）
        private double totalCommissionsInclTax;  // 总佣金（含税）
        private double totalFeeExclTax;  // 总费用（不含税）
        private double totalFeeInclTax;  // 总费用（含税）
        private double totalNonPayableOrdersExclTax;  // 总非可支付订单（不含税）
        private double totalNonPayableOrdersInclTax;  // 总非可支付订单（含税）
        private double totalPayableOrdersExclTax;  // 总可支付订单（不含税）
        private double totalPayableOrdersInclTax;  // 总可支付订单（含税）
    }
}
