package cn.iocoder.yudao.module.mes.controller.admin.wm.productissue.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES 领料出库单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class MesWmProductIssuePageReqVO extends PageParam {

    @Schema(description = "领料单编号", example = "ISSUE20250226001")
    private String code;

    @Schema(description = "领料单名称", example = "生产领料")
    private String name;

    @Schema(description = "工作站ID", example = "1")
    private Long workstationId;

    @Schema(description = "生产工单ID", example = "1")
    private Long workOrderId;

    @Schema(description = "生产任务ID", example = "1")
    private Long taskId;

    @Schema(description = "客户ID", example = "1")
    private Long clientId;

    @Schema(description = "状态", example = "10")
    private Integer status;

    @Schema(description = "领料日期范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] issueDate;

}
