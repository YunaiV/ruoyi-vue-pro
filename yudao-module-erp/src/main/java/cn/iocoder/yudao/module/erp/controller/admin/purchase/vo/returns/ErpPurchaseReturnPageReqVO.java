package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.returns;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 采购退货分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpPurchaseReturnPageReqVO extends PageParam {

    public static final Integer REFUND_STATUS_NONE = 0;
    public static final Integer REFUND_STATUS_PART = 1;
    public static final Integer REFUND_STATUS_ALL = 2;

    @Schema(description = "采购单编号", example = "XS001")
    private String no;

    @Schema(description = "供应商编号", example = "1724")
    private Long supplierId;

    @Schema(description = "退货时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] returnTime;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "退货状态", example = "2")
    private Integer status;

    @Schema(description = "创建者")
    private String creator;

    @Schema(description = "产品编号", example = "1")
    private Long productId;

    @Schema(description = "仓库编号", example = "1")
    private Long warehouseId;

    @Schema(description = "结算账号编号", example = "1")
    private Long accountId;

    @Schema(description = "采购单号", example = "1")
    private String orderNo;

    @Schema(description = "退款状态", example = "1")
    private Integer refundStatus;

    @Schema(description = "是否可退款", example = "true")
    private Boolean refundEnable; // 对应 refundStatus = [0, 1]

}