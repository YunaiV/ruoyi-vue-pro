package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.returns;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 采购退货单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SrmPurchaseReturnPageReqVO extends PageParam {

    // ========== 主表查询参数 ==========
    @Schema(description = "主表查询参数")
    private MainQuery mainQuery;

    // ========== 明细表查询参数 ==========
    @Schema(description = "明细表查询参数")
    private ItemQuery itemQuery;

    @Data
    public static class MainQuery {

        @Schema(description = "退货单id")
        private Long id;

        @Schema(description = "退货单编号")
        private String code;

        @Schema(description = "审核状态")
        private Integer auditStatus;

        @Schema(description = "审核人id")
        private Long auditorId;

        @Schema(description = "审核时间")
        @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
        private LocalDateTime[] auditTime;

        @Schema(description = "供应商编号")
        private Long supplierId;

        @Schema(description = "结算账户编号")
        private Long accountId;

        @Schema(description = "退货时间")
        @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
        private LocalDateTime[] returnTime;

        @Schema(description = "币种编号")
        private Long currencyId;

        @Schema(description = "价税合计")
        private BigDecimal grossTotalPrice;

        @Schema(description = "合计数量")
        private BigDecimal totalCount;

        @Schema(description = "最终合计价格")
        private BigDecimal totalPrice;

        @Schema(description = "总毛重")
        private BigDecimal totalWeight;

        @Schema(description = "总体积")
        private BigDecimal totalVolume;

        @Schema(description = "已退款金额")
        private BigDecimal refundPrice;

        @Schema(description = "合计产品价格")
        private BigDecimal totalProductPrice;

        @Schema(description = "合计税额")
        private BigDecimal totalGrossPrice;

        @Schema(description = "创建人")
        private Long creator;

        @Schema(description = "出库状态")
        private Integer outboundStatus;
    }

    @Data
    public static class ItemQuery {
        @Schema(description = "入库项id")
        private Long arriveItemId;

        @Schema(description = "入库单code")
        private String arriveCode;

        @Schema(description = "仓库编号")
        private Long warehouseId;

        @Schema(description = "产品编号")
        private Long productId;

        @Schema(description = "产品单位单位")
        private Long productUnitId;

        @Schema(description = "产品单位单价")
        private BigDecimal productPrice;

        @Schema(description = "产品单位名称")
        private String productUnitName;

        @Schema(description = "数量")
        private BigDecimal qty;

        @Schema(description = "总价")
        private BigDecimal totalPrice;

        @Schema(description = "税率")
        private BigDecimal taxRate;

        @Schema(description = "税额")
        private BigDecimal tax;

        @Schema(description = "备注")
        private String remark;

        @Schema(description = "含税单价")
        private BigDecimal grossPrice;

        @Schema(description = "箱率")
        private String containerRate;

        @Schema(description = "申请人id")
        private Long applicantId;

        @Schema(description = "申请部门id")
        private Long applicationDeptId;

        @Schema(description = "报关品名")
        private String declaredType;

        @Schema(description = "报关品名英文")
        private String declaredTypeEn;

        @Schema(description = "产品sku")
        private String productCode;

        @Schema(description = "产品名称")
        private String productName;

        @Schema(description = "出库状态")
        private Integer outboundStatus;
    }
}