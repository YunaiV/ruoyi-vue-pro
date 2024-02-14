package cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 销售订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpSaleOrderPageReqVO extends PageParam {

    /**
     * 出库状态 - 无
     */
    public static final Integer OUT_STATUS_NONE = 0;
    /**
     * 出库状态 - 部分
     */
    public static final Integer OUT_STATUS_PART = 1;
    /**
     * 出库状态 - 全部
     */
    public static final Integer OUT_STATUS_ALL = 2;

    /**
     * 退货状态 - 无
     */
    public static final Integer RETURN_STATUS_NONE = 0;
    /**
     * 退货状态 - 部分
     */
    public static final Integer RETURN_STATUS_PART = 1;
    /**
     * 退货状态 - 全部
     */
    public static final Integer RETURN_STATUS_ALL = 2;

    @Schema(description = "销售单编号", example = "XS001")
    private String no;

    @Schema(description = "客户编号", example = "1724")
    private Long customerId;

    @Schema(description = "下单时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] orderTime;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "销售状态", example = "2")
    private Integer status;

    @Schema(description = "创建者")
    private String creator;

    @Schema(description = "产品编号", example = "1")
    private Long productId;

    @Schema(description = "出库状态", example = "2")
    private Integer outStatus;

    @Schema(description = "退货状态", example = "2")
    private Integer returnStatus;

    @Schema(description = "是否可出库", example = "true")
    private Boolean outEnable;

    @Schema(description = "是否可退货", example = "true")
    private Boolean returnEnable;

}