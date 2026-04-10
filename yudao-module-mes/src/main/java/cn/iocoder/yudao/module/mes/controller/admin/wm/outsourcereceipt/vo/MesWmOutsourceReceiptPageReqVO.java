package cn.iocoder.yudao.module.mes.controller.admin.wm.outsourcereceipt.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES 外协入库单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesWmOutsourceReceiptPageReqVO extends PageParam {

    @Schema(description = "入库单编码", example = "OR2026030001")
    private String code;

    @Schema(description = "入库单名称", example = "外协加工入库单")
    private String name;

    @Schema(description = "供应商编号", example = "1")
    private Long vendorId;

    @Schema(description = "入库日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] receiptDate;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "外协工单编码", example = "WO2026030001")
    private String workOrderCode;

}
