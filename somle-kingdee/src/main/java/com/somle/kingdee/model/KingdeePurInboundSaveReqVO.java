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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 采购入库单保存请求对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeePurInboundSaveReqVO {

    /**
     * 单据日期，格式：yyyy-MM-dd (必填)
     */
    @NotBlank(message = "单据日期不能为空")
    private String billDate;

    /**
     * 单据编码 (必填)
     */
    @NotBlank(message = "单据编码不能为空")
    private String billNo;

    /**
     * 商品分录 (必填)
     */
    @NotEmpty(message = "商品分录不能为空")
    @Valid
    private List<MaterialEntity> materialEntity;

    /**
     * 供应商ID (必填)
     */
    @NotBlank(message = "供应商ID不能为空")
    private String supplierId;

    /**
     * 供应商编码
     */
    private String supplierNumber;

    /**
     * 销售费用分摊规则；1: 按价税合计分摊（默认）； 2:按数量分摊
     */
    private String allocateRule;

    /**
     * 上传的附件url
     */
    private List<AttachmentsUrl> attachmentsUrl;

    /**
     * 联系信息
     */
    private String contactInfo;

    /**
     * 采购费用明细
     */
    private CostFeeEntity costFeeEntity;

    /**
     * 币别ID
     */
    private String currencyId;

    /**
     * 币别编码
     */
    private String currencyNumber;

    /**
     * 自定义字段
     */
    private Map<String, String> customField;

    /**
     * 抵扣预付款
     */
    private String deductionBalance;

    /**
     * 抵扣预付款分录体
     */
    private List<DeductionEntry> deductionEntry;

    /**
     * 部门ID
     */
    private String deptId;

    /**
     * 部门编码
     */
    private String deptNumber;

    /**
     * 付款抹零
     */
    private BigDecimal diffAmount;

    /**
     * 详细地址
     */
    private String dispatcherAddress;

    /**
     * 城市ID
     */
    private String dispatcherCityId;

    /**
     * 联系国家ID
     */
    private String dispatcherCountryId;

    /**
     * 区域
     */
    private String dispatcherDistrict;

    /**
     * 收货人联系电话
     */
    private String dispatcherPhone;

    /**
     * 省份ID
     */
    private String dispatcherProvinceId;

    /**
     * 结算日期
     */
    private String dueDate;

    /**
     * 业务员ID
     */
    private String empId;

    /**
     * 业务员编码
     */
    private String empNumber;

    /**
     * 汇率
     */
    private BigDecimal exchangeRate;

    /**
     * 单据ID，新增不填，更新时必填
     */
    private String id;

    /**
     * 忽略操作确认提示
     */
    private Boolean ignoreWarn;

    /**
     * 是否暂估
     */
    private Boolean isEstimation;

    /**
     * 操作类型，审核audit、提交submit
     */
    private String operationKey;

    /**
     * 外部单号
     */
    private String outsidePkId;

    /**
     * 收款账户
     */
    private List<PaymentEntry> paymentEntry;

    /**
     * 备注
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
     * 结算供应商ID
     */
    private String settleSupplierId;

    /**
     * 本单成交
     */
    private BigDecimal totalAmount;

    /**
     * 业务类型,1：限购，2：赊购，3：直运采购，4：受托入库
     */
    private String transType;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class MaterialEntity {
        /**
         * 商品ID (必填)
         */
        @NotBlank(message = "商品ID不能为空")
        private String materialId;

        /**
         * 商品编码
         */
        private String materialNumber;

        /**
         * 数量 (必填)
         */
        @NotNull(message = "数量不能为空")
        private BigDecimal qty;

        /**
         * 仓库ID (必填)
         */
        @NotBlank(message = "仓库ID不能为空")
        private String stockId;

        /**
         * 仓库编码
         */
        private String stockNumber;

        /**
         * 单位ID (必填)
         */
        @NotBlank(message = "单位ID不能为空")
        private String unitId;

        /**
         * 实际不含税金额
         */
        private BigDecimal actNonTaxAmount;

        /**
         * 实际含税单价
         */
        private BigDecimal actTaxPrice;

        /**
         * 价税合计
         */
        private BigDecimal allAmount;

        /**
         * 价税合计本位币
         */
        private BigDecimal allAmountFor;

        /**
         * 折后金额
         */
        private BigDecimal amount;

        /**
         * 辅助属性
         */
        private String auxPropId;

        /**
         * 辅助数量
         */
        private BigDecimal auxQty;

        /**
         * 辅助单位
         */
        private String auxUnitId;

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
        private BigDecimal billDisBeforeAmount;

        /**
         * 整单折扣分配额
         */
        private BigDecimal billDisDistribution;

        /**
         * 税率
         */
        private BigDecimal cess;

        /**
         * 行备注
         */
        private String comment;

        /**
         * 入库成本
         */
        private BigDecimal cost;

        /**
         * 本次核销金额
         */
        private BigDecimal curSettleAmount;

        /**
         * 自定义字段
         */
        private Map<String, String> customEntityField;

        /**
         * 折扣额度
         */
        private BigDecimal disAmount;

        /**
         * 折扣单价
         */
        private BigDecimal disPrice;

        /**
         * 折扣率
         */
        private BigDecimal disRate;

        /**
         * 折扣
         */
        private BigDecimal discount;

        /**
         * 优惠分摊金额
         */
        private BigDecimal divideDiffAmount;

        /**
         * 采购费用分摊
         */
        private BigDecimal fee;

        /**
         * 分录id
         */
        private String id;

        /**
         * 是否赠品
         */
        private Integer isFree;

        /**
         * 生产日期
         */
        private String kfDate;

        /**
         * 保质期天数
         */
        private Integer kfPeriod;

        /**
         * 保质期类型：1/天，2/月，3/年
         */
        private String kfType;

        /**
         * 外部商品编码
         */
        private String outsideMaterialNumber;

        /**
         * 外部商品单位
         */
        private String outsideMaterialUnit;

        /**
         * 拣货单分录内码
         */
        private String pickInspectEntryId;

        /**
         * 拣货单内码
         */
        private String pickInspectId;

        /**
         * 价格
         */
        private BigDecimal price;

        /**
         * 生产许可证号
         */
        private String proLicense;

        /**
         * 注册证号
         */
        private String proRegNo;

        /**
         * 产地
         */
        private String propLace;

        /**
         * 序列号
         */
        private String snList;

        /**
         * 序列号流转id
         */
        private String snListId;

        /**
         * 仓位
         */
        private String spId;

        /**
         * 源单类型
         */
        private String srcBillTypeId;

        /**
         * 源单分录ID
         */
        private String srcEntryId;

        /**
         * 源单id
         */
        private String srcInterId;

        /**
         * 采购订单单号
         */
        private String srcOrderBillNo;

        /**
         * 源单行号
         */
        private Integer srcSeq;

        /**
         * 税额
         */
        private BigDecimal taxAmount;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 入库单位成本
         */
        private BigDecimal unitCost;

        /**
         * 有效日期
         */
        private String validDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PaymentEntry {
        /**
         * 收款金额
         */
        private BigDecimal paidAmount;

        /**
         * 备注
         */
        private String payComment;

        /**
         * 收款方式
         */
        private String payTypeId;

        /**
         * 收款账户
         */
        private String settleAccountId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DeductionEntry {
        /**
         * 本次核销金额 (必填)
         */
        @NotBlank(message = "本次核销金额不能为空")
        private String arapCurSettleAmount;

        /**
         * 单据类型 (必填)
         */
        @NotBlank(message = "单据类型不能为空")
        private String arapSrcBillTypeId;

        /**
         * 源单ID (必填)
         */
        @NotBlank(message = "源单ID不能为空")
        private String arapSrcInterId;

        /**
         * 分录ID
         */
        private String id;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CostFeeEntity {
        /**
         * 需要删除的采购费用分录ID
         */
        private String delIds;

        /**
         * 采购费用分录
         */
        private List<CostFeeRows> feeRecords;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CostFeeRows {
        /**
         * 供应商ID
         */
        private String supplierId;

        /**
         * 价税合计
         */
        private String allAmount;

        /**
         * 价税合计本位币
         */
        private String allAmountFor;

        /**
         * 费用
         */
        private String amount;

        /**
         * 费用本位币
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
         * 支出类型
         */
        private String pacctTypeId;

        /**
         * 付款信息单据体
         */
        private List<ReceiveInfoRows> receiveInfoEntity;

        /**
         * 结算供应商id
         */
        private String settleSupplierId;

        /**
         * 结算供应商编码
         */
        private String settleSupplierNumber;

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
    public static class ReceiveInfoRows {
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
    public static class AttachmentsUrl {
        /**
         * 附件类型
         */
        private String panel;

        /**
         * 服务器返回的附件地址
         */
        private String url;
    }
} 