package cn.iocoder.yudao.module.mes.controller.admin.wm.batch.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES 批次管理分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesWmBatchPageReqVO extends PageParam {

    @Schema(description = "批次编码", example = "BATCH20250314001")
    private String code;

    @Schema(description = "物料编号", example = "1")
    private Long itemId;

    @Schema(description = "供应商编号", example = "1")
    private Long vendorId;

    @Schema(description = "客户编号", example = "1")
    private Long clientId;

    @Schema(description = "销售订单编号", example = "SO20250314001")
    private String salesOrderCode;

    @Schema(description = "采购订单编号", example = "PO20250314001")
    private String purchaseOrderCode;

    @Schema(description = "生产工单编号", example = "1")
    private Long workOrderId;

    @Schema(description = "生产任务编号", example = "1")
    private Long taskId;

    @Schema(description = "工作站编号", example = "1")
    private Long workstationId;

    @Schema(description = "工具编号", example = "1")
    private Long toolId;

    @Schema(description = "模具编号", example = "1")
    private Long moldId;

    @Schema(description = "生产批号", example = "LOT20250314001")
    private String lotNumber;

    @Schema(description = "质量状态", example = "1")
    private Integer qualityStatus;

    @Schema(description = "生产日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] produceDate;

    @Schema(description = "有效期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] expireDate;

    @Schema(description = "入库日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] receiptDate;

}