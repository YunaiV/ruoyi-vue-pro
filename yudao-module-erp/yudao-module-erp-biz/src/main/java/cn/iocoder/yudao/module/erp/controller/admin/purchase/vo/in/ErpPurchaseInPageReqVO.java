package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 采购入库分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpPurchaseInPageReqVO extends PageParam {

    public static final Integer PAYMENT_STATUS_NONE = 0;
    public static final Integer PAYMENT_STATUS_PART = 1;
    public static final Integer PAYMENT_STATUS_ALL = 2;

    @Schema(description = "采购单编号-单据编号", example = "CGDD-20250108-000027")
    private String no;

    @Schema(description = "单据日期", example = "2024-10-12")
    private LocalDateTime noTime;

    @Schema(description = "汇率,财务管理-币别维护",example = "5.8")
    private BigDecimal exchangeRate;

    @Schema(description = "应付款余额",example = "12")
    private BigDecimal payableBalance;

    @Schema(description = "审核人ID")
    private String auditorId;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "入库审核状态")
    private Integer auditorStatus;

    @Schema(description = "入库时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] inTime;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "入库状态", example = "2")
    private Integer status;

    @Schema(description = "创建者")
    private String creator;

    @Schema(description = "产品编号", example = "1")
    private Long productId;

    @Schema(description = "仓库编号", example = "1")
    private Long warehouseId;

    @Schema(description = "结算账号编号", example = "1")
    private Long accountId;

    @Schema(description = "结算日期", example = "2025-1-1")
    private LocalDateTime settlementDate;

    @Schema(description = "付款状态", example = "1")
    private Integer payStatus;

    @Schema(description = "是否可付款", example = "true")
    private Boolean paymentEnable; // 对应 paymentStatus = [0, 1]

    @Schema(description = "采购单号", example = "1")
    private String orderNo;



}