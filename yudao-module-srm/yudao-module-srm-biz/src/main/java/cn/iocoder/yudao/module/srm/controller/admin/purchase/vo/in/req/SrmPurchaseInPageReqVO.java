package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.req;

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
public class SrmPurchaseInPageReqVO extends PageParam {

    public static final Integer PAYMENT_STATUS_NONE = 0;
    public static final Integer PAYMENT_STATUS_PART = 1;
    public static final Integer PAYMENT_STATUS_ALL = 2;

    @Schema(description = "采购单编号-单据编号")
    private String no;

    @Schema(description = "单据日期")
    private LocalDateTime[] noTime;

//    @Schema(description = "汇率,财务管理-币别维护",example = "5.8")
//    private BigDecimal exchangeRate;

    @Schema(description = "应付款余额")
    private BigDecimal payableBalance;

    @Schema(description = "审核人ID")
    private String auditorId;

    @Schema(description = "审核时间")
    private LocalDateTime[] auditTime;

    @Schema(description = "入库审核状态")
    private Integer auditStatus;

    @Schema(description = "入库时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] inTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "入库状态")
    private Integer status;

    @Schema(description = "创建者")
    private String creator;

    @Schema(description = "产品编号")
    private Long productId;

    @Schema(description = "仓库编号")
    private Long warehouseId;

    @Schema(description = "结算账号编号")
    private Long accountId;

    @Schema(description = "结算日期")
    private LocalDateTime settlementDate;

    @Schema(description = "付款状态")
    private Integer payStatus;

    @Schema(description = "是否可付款")
    private Boolean paymentEnable; // 对应 paymentStatus = [0, 1]

    @Schema(description = "采购单号")
    private String orderNo;

    //reconciliationStatus
    @Schema(description = "是否对账")
    private Boolean reconciliationEnable;

}