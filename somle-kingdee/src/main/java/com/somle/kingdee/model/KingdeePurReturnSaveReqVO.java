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
 * 采购退货单保存请求对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeePurReturnSaveReqVO {

    /**
     * 商品分录 (必填)
     */
    @NotEmpty(message = "商品分录不能为空")
    @Valid
    private List<MaterialEntity> materialEntity;

    /**
     * 供应商编码 (必填，与supplier_id二选一)
     */
    private String supplierNumber;

    /**
     * 供应商ID (必填，与supplier_number二选一)
     */
    private String supplierId;

    /**
     * 应付余额
     */
    private BigDecimal allDebt;

    /**
     * 上传的附件url
     */
    private List<AttachmentsUrl> attachmentsUrl;

    /**
     * 单据日期
     */
    private String billDate;

    /**
     * 整单折扣额
     */
    private BigDecimal billDisAmount;

    /**
     * 整单折扣率
     */
    private BigDecimal billDisRate;

    /**
     * 单据编码
     */
    private String billNo;

    /**
     * 联系信息
     */
    private String contactInfo;

    /**
     * 币别ID
     */
    private String currencyId;

    /**
     * 自定义字段
     */
    private Map<String, String> customField;

    /**
     * 部门ID
     */
    private String deptId;

    /**
     * 部门编码
     */
    private String deptNumber;

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
     * 单据ID，新增不填，更新时必填
     */
    private String id;

    /**
     * 是否忽略告警信息
     */
    private Boolean ignoreWarn;

    /**
     * 是否暂估
     */
    private Boolean isEstimation;

    /**
     * 上次欠款
     */
    private BigDecimal lastDebt;

    /**
     * 操作类型，审核audit、提交submit
     */
    private String operationKey;

    /**
     * 外部单号
     */
    private String outsidePkId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 退货原因
     */
    private String returnReason;

    /**
     * 是否从反算单价
     */
    private Boolean reverseConditionCalculate;

    /**
     * 结算期限id
     */
    private String settingTermId;

    /**
     * 本次应退账款
     */
    private BigDecimal totalAmount;

    /**
     * 退款合计
     */
    private BigDecimal totalInsAmount;

    /**
     * 本单未退
     */
    private BigDecimal totalUnsettleAmount;

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
        private Boolean isFree;

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
         * 源单ID
         */
        private String srcInterId;

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