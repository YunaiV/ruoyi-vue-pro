package cn.iocoder.yudao.module.mes.controller.admin.wm.productsales.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES 销售出库单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesWmProductSalesPageReqVO extends PageParam {

    @Schema(description = "出库单号", example = "PS2026030001")
    private String code;

    @Schema(description = "出库单名称", example = "产品出库单")
    private String name;

    @Schema(description = "客户编号", example = "1")
    private Long clientId;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "计划发货日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] shipmentDate;

}
