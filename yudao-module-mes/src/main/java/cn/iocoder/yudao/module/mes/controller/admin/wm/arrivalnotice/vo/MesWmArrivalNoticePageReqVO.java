package cn.iocoder.yudao.module.mes.controller.admin.wm.arrivalnotice.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES 到货通知单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesWmArrivalNoticePageReqVO extends PageParam {

    @Schema(description = "通知单编码", example = "AN2026020001")
    private String code;

    @Schema(description = "通知单名称", example = "钢板到货")
    private String name;

    @Schema(description = "采购订单编号", example = "PO20260101")
    private String purchaseOrderCode;

    @Schema(description = "供应商编号", example = "1")
    private Long vendorId;

    @Schema(description = "到货日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] arrivalDate;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
