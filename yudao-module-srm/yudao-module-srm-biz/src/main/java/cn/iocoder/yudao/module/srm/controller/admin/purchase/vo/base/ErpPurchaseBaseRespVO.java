package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.base;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ErpPurchaseBaseRespVO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("id")
    protected Long id;

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED)
    protected Long supplierId;

    @Schema(description = "供应商名称")
    @ExcelProperty("供应商名称")
    protected String supplierName;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    protected String remark;

    @Schema(description = "附件地址")
    @ExcelProperty("附件地址")
    protected String fileUrl;

    @Schema(description = "创建人")
    @ExcelProperty("创建人")
    protected String creator;

    @Schema(description = "创建人部门名称")
    @ExcelProperty("创建人部门名称")
    protected String departmentName;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    protected LocalDateTime createTime;

    @Schema(description = "审核者")
    @ExcelProperty("审核者")
    private String auditor;

    @Schema(description = "审核人名称")
    @ExcelProperty("审核人名称")
    protected String auditorName;

    @Schema(description = "审核时间")
    @ExcelProperty("审核时间")
    protected LocalDateTime auditTime;

    @Schema(description = "审核状态")
    @ExcelProperty("审核状态")
    protected Integer auditStatus;

    // ========== 采购订单金额(钱)开始 ==========
    @Schema(description = "结算账户编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("结算账户编号")
    protected Long accountId;

    @Schema(description = "结算日期")
    @ExcelProperty("结算日期")
    protected LocalDateTime settlementDate;

    @Schema(description = "最终合计价格", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("最终合计价格")
    protected BigDecimal totalPrice;

    @Schema(description = "已退款金额，单位：元", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("已退款金额，单位：元")
    protected BigDecimal refundPrice;

    @Schema(description = "合计产品价格，单位：元", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("合计产品价格，单位：元")
    protected BigDecimal totalProductPrice;

    @Schema(description = "合计税额，单位：元", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("合计税额，单位：元")
    protected BigDecimal totalTaxPrice;

    @Schema(description = "优惠率，百分比", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("优惠率，百分比")
    protected BigDecimal discountPercent;

    @Schema(description = "优惠金额，单位：元", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("优惠金额，单位：元")
    protected BigDecimal discountPrice;

    @Schema(description = "定金金额，单位：元", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("定金金额，单位：元")
    protected BigDecimal otherPrice;
    // ========== 订单产品合计-开始 ==========
    @Schema(description = "合计数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("合计数量")
    protected BigDecimal totalCount;

    @Schema(description = "币别id")
    private Long currencyId;

    @Schema(description = "币别名称")
    private Long currencyName;
    // ========== 采购订单金额(钱)结束 ==========
//    @Schema(description = "汇率,财务管理-币别维护")
//    private BigDecimal exchangeRate;
    // ========== 订单产品合计-结束 ==========
}
