package com.somle.kingdee.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@Validated
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeePurOrderSaveReqVO {
    /**
     * 单据日期，格式：yyyy-MM-dd
     * 必填
     */
    @NotBlank(message = "单据日期不能为空")
    private String billDate;

    /**
     * 单据ID，新增不填，更新时必填
     */
    private String id;

    /**
     * 商品分录
     * 必填
     */
    @NotEmpty(message = "商品分录不能为空")
    @Valid
    private List<MaterialEntity> materialEntity;

    /**
     * 供应商ID
     * 必填
     */
    @NotBlank(message = "供应商ID不能为空")
    private String supplierId;

    /**
     * 供应商编码
     * 必填
     */
    @NotBlank(message = "供应商编码不能为空")
    private String supplierNumber;

    /**
     * 销售费用分摊规则；1: 按价税合计分摊（默认）； 2:按数量分摊
     */
    private String allocateRule;

    /**
     * 上传的附件url
     */
    private List<Attachments> attachmentsUrl;

    /**
     * 整单折扣额
     */
    private String billDisAmount;

    /**
     * 整单折扣率%
     */
    private String billDisRate;

    /**
     * 单据编码
     */
    private String billNo;

    /**
     * 供应商发货地址-详细地址
     */
    private String contactAddress;

    /**
     * 供应商发货地址-市ID
     */
    private String contactCityId;

    /**
     * 供应商发货地址-国家ID
     */
    private String contactCountryId;

    /**
     * 供应商发货地址-区ID
     */
    private String contactDistrictId;

    /**
     * 供应商发货地址-联系人
     */
    private String contactLinkman;

    /**
     * 供应商发货地址-联系方式
     */
    private String contactPhone;

    /**
     * 供应商发货地址-省ID
     */
    private String contactProvinceId;

    /**
     * 采购费用
     */
    private String costFee;

    /**
     * 采购费用明细
     */
    private CostFeeEntity costFeeEntity;

    /**
     * 币别
     */
    private String currencyId;

    /**
     * 自定义字段
     */
    private Map<String, String> customField;

    /**
     * 收货地址-详细地址
     */
    private String dispatcherAddress;

    /**
     * 收货地址-市ID
     */
    private String dispatcherCityId;

    /**
     * 收货地址国家id
     */
    private String dispatcherCountryId;

    /**
     * 收货地址-区ID
     */
    private String dispatcherDistrictId;

    /**
     * 收货地址人
     */
    private String dispatcherLinkman;

    /**
     * 收货地址联系电话
     */
    private String dispatcherPhone;

    /**
     * 收货地址-省ID
     */
    private String dispatcherProvinceId;

    /**
     * 结算日期
     */
    private String dueDate;

    /**
     * 业务员
     */
    private String empId;

    /**
     * 业务员编码
     */
    private String empNumber;

    /**
     * 是否忽略告警信息
     */
    private Boolean ignoreWarn;

    /**
     * 操作类型，审核audit、提交submit
     */
    private String operationKey;

    /**
     * 外部单号
     */
    private String outsidePkId;

    /**
     * 付款账户
     */
    private List<PaymentEntry> paymentEntry;

    /**
     * 收货地址
     */
    private String receviceDelivery;

    /**
     * 单据备注
     */
    private String remark;

    /**
     * 是否从反算单价
     */
    private Boolean reverseConditionCalculate;

    /**
     * 结算期限id
     */
    private String settingTermId;

    /**
     * 结算供应商
     */
    private String settleSupplierId;

    /**
     * 本次应付账款
     */
    private Double totalAmount;

    /**
     * 本次付款
     */
    private Double totalInsAmount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class MaterialEntity {
        /**
         * 交货日期
         * 必填
         */
        @NotBlank(message = "交货日期不能为空")
        private String deliveryDate;

        /**
         * 商品ID
         * 必填
         */
        @NotBlank(message = "商品ID不能为空")
        private String materialId;

        /**
         * 商品编码
         * 必填
         */
        @NotBlank(message = "商品编码不能为空")
        private String materialNumber;

        /**
         * 数量
         * 必填
         */
        @NotNull(message = "数量不能为空")
        private Double qty;

        /**
         * 单位ID
         * 必填
         */
        @NotBlank(message = "单位ID不能为空")
        private String unitId;

        /**
         * 单位编码
         * 必填
         */
        @NotBlank(message = "单位编码不能为空")
        private String unitNumber;

        /**
         * 实际不含税金额
         */
        private String actNonTaxAmount;

        /**
         * 实际不含税单价
         */
        private String actNonTaxPrice;

        /**
         * 实际含税单价
         */
        private String actTaxPrice;

        /**
         * 价税合计
         */
        private String allAmount;

        /**
         * 折后金额
         */
        private String amount;

        /**
         * 辅助属性
         */
        private String auxPropId;

        /**
         * 条形码
         */
        private String barcode;

        /**
         * 批次号
         */
        private String batchNo;

        /**
         * 整单折前价税合计
         */
        private String billDisBeforeAmount;

        /**
         * 整单折扣分配额
         */
        private String billDisDistribution;

        /**
         * 商品增值税税率
         */
        private String cess;

        /**
         * 行备注
         */
        private String comment;

        /**
         * 自定义字段
         */
        private Map<String, String> customEntityField;

        /**
         * 折扣额
         */
        private String disAmount;

        /**
         * 折后单价
         */
        private String disPrice;

        /**
         * 折扣率
         */
        private String disRate;

        /**
         * 折扣
         */
        private String discount;

        /**
         * 商品分录id
         */
        private String id;

        /**
         * 是否赠品
         */
        private Boolean isFree;

        /**
         * 外部商品编码
         */
        private String outsideMaterialNumber;

        /**
         * 外部商品单位
         */
        private String outsideMaterialUnit;

        /**
         * 折前金额
         */
        private String preDisAmount;

        /**
         * 单价
         */
        private String price;

        /**
         * 生产许可证号
         */
        private String proLicense;

        /**
         * 产地
         */
        private String proPlace;

        /**
         * 注册证号
         */
        private String proRegNo;

        /**
         * 仓位
         */
        private String spId;

        /**
         * 源单类型
         */
        private String srcBillTypeId;

        /**
         * 源单分录id
         */
        private String srcEntryId;

        /**
         * 源单id
         */
        private String srcInterId;

        /**
         * 仓库
         */
        private String stockId;

        /**
         * 仓库编码
         */
        private String stockNumber;

        /**
         * 税额
         */
        private String taxAmount;

        /**
         * 含税单价
         */
        private String taxPrice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PaymentEntry {
        /**
         * 付款ID
         */
        private String id;

        /**
         * 收款金额
         */
        private String payAmount;

        /**
         * 备注
         */
        private String payComment;

        /**
         * 支付方式id
         */
        private String payTypeId;

        /**
         * 序号
         */
        private Integer seq;

        /**
         * 收款账户id
         */
        private String settleAccountId;

        /**
         * 交易号/票据号
         */
        private String transNumber;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CostFeeEntity {
        /**
         * 需要删除的销售费用分录ID
         */
        private String delIds;

        /**
         * 销售费用分录
         */
        private List<FeeRecord> feeRecords;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class FeeRecord {
        /**
         * 应付金额
         */
        private String allAmount;

        /**
         * 应付金额本位币
         */
        private String allAmountFor;

        /**
         * 不含税金额
         */
        private String amount;

        /**
         * 不含税金额本位币
         */
        private String amountFor;

        /**
         * 税率
         */
        private String cess;

        /**
         * 备注
         */
        private String comment;

        /**
         * 币别id
         */
        private String currencyId;

        /**
         * 汇率
         */
        private String exchangeRate;

        /**
         * 销售费用分录ID
         */
        private String id;

        /**
         * 生成的其他应付单号
         */
        private String linkedBillNo;

        /**
         * 支出类别
         */
        private String pacctTypeId;

        /**
         * 付款信息单据体
         */
        private List<ReceiveInfoEntity> receiveInfoEntity;

        /**
         * 结算供应商
         */
        private String settleSupplierId;

        /**
         * 供应商
         */
        private String supplierId;

        /**
         * 税额
         */
        private String taxAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ReceiveInfoEntity {
        /**
         * 支出类别id
         */
        private String payTypeId;

        /**
         * 付款金额
         */
        private String receiveAmount;

        /**
         * 付款账户id
         */
        private String settleAccountId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Attachments {
        /**
         * 附件地址
         */
        private String url;
    }
} 