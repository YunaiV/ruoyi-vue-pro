package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.req;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 采购到货单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SrmPurchaseInPageReqVO extends PageParam {

    // ========== 主表查询参数 ==========
    @Schema(description = "主表查询参数")
    private MainQuery mainQuery;

    // ========== 明细表查询参数 ==========
    @Schema(description = "明细表查询参数")
    private ItemQuery itemQuery;

    @Schema(description = "主表查询参数")
    @Data
    public static class MainQuery {
        // ========== 基础信息 ==========
        @Schema(description = "到货单编号")
        private Long id;

        @Schema(description = "到货单号")
        private String code;

        // ========== 供应商信息 ==========
        @Schema(description = "供应商编号")
        private Long supplierId;

        @Schema(description = "收货地址")
        private String address;

        // ========== 结算信息 ==========
        @Schema(description = "结算账户编号")
        private Long accountId;

        @Schema(description = "币种编号")
        private Long currencyId;

        // ========== 时间信息 ==========
        @Schema(description = "单据日期")
        @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
        private LocalDateTime[] billTime;

        @Schema(description = "到货时间")
        @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
        private LocalDateTime[] arriveTime;

        // ========== 审核信息 ==========
        @Schema(description = "审核人编号")
        private String auditorId;

        @Schema(description = "审核时间")
        @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
        private LocalDateTime[] auditTime;

        // ========== 状态信息 ==========
        @Schema(description = "入库状态")
        private Integer inboundStatus;

        @Schema(description = "审核状态")
        private Integer auditStatus;

        // ========== 时间范围 ==========
        @Schema(description = "创建时间")
        @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
        private LocalDateTime[] createTime;
    }

    @Schema(description = "明细表查询参数")
    @Data
    public static class ItemQuery {
        // ========== 产品信息 ==========
        @Schema(description = "产品编号")
        private Long productId;

        @Schema(description = "产品名称")
        private String productName;

        @Schema(description = "产品单位编号")
        private Long productUnitId;

        @Schema(description = "报关品名")
        private String declaredType;

        @Schema(description = "报关品名英文")
        private String declaredTypeEn;

        @Schema(description = "条码")
        private String productCode;

        // ========== 仓库信息 ==========
        @Schema(description = "仓库编号")
        private Long warehouseId;

        // ========== 订单信息 ==========
        @Schema(description = "采购订单编号")
        private String orderCode;

        @Schema(description = "采购订单项编号")
        private Long orderItemId;

        @Schema(description = "单据来源")
        private String source;

        // ========== 申请人信息 ==========
        @Schema(description = "申请人编号")
        private Long applicantId;

        @Schema(description = "申请部门编号")
        private Long applicationDeptId;

        // ========== 状态信息 ==========
        @Schema(description = "入库状态")
        private Integer inboundStatus;

        @Schema(description = "付款状态")
        private Integer payStatus;
    }
}