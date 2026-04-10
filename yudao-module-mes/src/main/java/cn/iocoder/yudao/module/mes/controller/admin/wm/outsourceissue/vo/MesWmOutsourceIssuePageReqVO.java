package cn.iocoder.yudao.module.mes.controller.admin.wm.outsourceissue.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES 外协发料单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesWmOutsourceIssuePageReqVO extends PageParam {

    @Schema(description = "发料单编号", example = "WOS202603020001")
    private String code;

    @Schema(description = "发料单名称", example = "外协发料单001")
    private String name;

    @Schema(description = "供应商ID", example = "1")
    private Long vendorId;

    @Schema(description = "生产工单ID", example = "1")
    private Long workOrderId;

    @Schema(description = "发料日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] issueDate;

}
