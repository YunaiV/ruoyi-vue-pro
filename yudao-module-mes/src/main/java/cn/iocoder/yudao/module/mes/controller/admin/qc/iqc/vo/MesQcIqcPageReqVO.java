package cn.iocoder.yudao.module.mes.controller.admin.qc.iqc.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES 来料检验单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesQcIqcPageReqVO extends PageParam {

    @Schema(description = "检验单编号", example = "IQC2025")
    private String code;

    @Schema(description = "供应商 ID", example = "10")
    private Long vendorId;

    @Schema(description = "供应商批次号", example = "VB2025")
    private String vendorBatch;

    @Schema(description = "产品物料 ID", example = "20")
    private Long itemId;

    @Schema(description = "检测结果", example = "1")
    private Integer checkResult;

    @Schema(description = "检测人员用户 ID", example = "1")
    private Long inspectorUserId;

    @Schema(description = "来料日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] receiveDate;

    @Schema(description = "检测日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] inspectDate;

}
