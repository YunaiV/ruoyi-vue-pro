package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.req;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 采购订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SrmPurchaseOrderPageReqVO extends PageParam {

    @Schema(description = "采购状态")
    private Integer status;

    @Schema(description = "供应商编号")
    private Long supplierId;

    @Schema(description = "结算账户编号")
    private Long accountId;

    @Schema(description = "采购时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] orderTime;

    @Schema(description = "合计数量")
    private BigDecimal totalCount;

    @Schema(description = "合计价格，单位：元")
    private BigDecimal totalPrice;

    @Schema(description = "合计产品价格，单位：元")
    private BigDecimal totalProductPrice;

    @Schema(description = "合计税额，单位：元")
    private BigDecimal totalTaxPrice;

    @Schema(description = "优惠率，百分比")
    private BigDecimal discountPercent;

    @Schema(description = "优惠金额，单位：元")
    private BigDecimal discountPrice;

    @Schema(description = "定金金额，单位：元")
    private BigDecimal depositPrice;

    @Schema(description = "附件地址")
    private String fileUrl;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "采购入库数量")
    private BigDecimal totalInCount;

    @Schema(description = "采购退货数量")
    private BigDecimal totalReturnCount;

    @Schema(description = "单据日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] billTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "结算日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] settlementDate;

    @Schema(description = "审核人id")
    private Long auditorId;

    @Schema(description = "审核时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] auditTime;

    @Schema(description = "财务主体id")
    private Long purchaseCompanyId;

    @Schema(description = "x码")
    private String xCode;

    @Schema(description = "箱率")
    private String containerRate;

    @Schema(description = "仓库id")
    private Long warehouseId;

    @Schema(description = "开关状态")
    private Integer offStatus;

    @Schema(description = "执行状态")
    private Integer executeStatus;

    @Schema(description = "入库状态")
    private Integer inStatus;

    @Schema(description = "付款状态")
    private Integer payStatus;

    @Schema(description = "审核状态")
    private Integer auditStatus;

    @Schema(description = "收货地址")
    private String address;

    @Schema(description = "付款条款")
    private String paymentTerms;

    @Schema(description = "采购状态")
    private Integer orderStatus;

    //    @Schema(description = "验货单json")
    //    private String inspectionJson;
    //
    //    @Schema(description = "完工单json")
    //    private String completionJson;
    //    @Schema(description = "总验货通过数量")
    //    private Integer totalInspectionPassCount;

    //子表分割线
    @Schema(description = "采购申请单No")
    private String erpPurchaseRequestItemNo;

    @Schema(description = "产品id")
    private Long productId;

    @Schema(description = "产品sku")
    private String barCode;

    @Schema(description = "产品名称")
    private String productName;

    @Schema(description = "产品单位名称")
    private String productUnitName;

    @Schema(description = "申请人id")
    private Long applicantId;

    @Schema(description = "申请人部门id")
    private Long applicationDeptId;
}