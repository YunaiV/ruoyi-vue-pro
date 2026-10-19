package cn.iocoder.yudao.module.oa.controller.admin.workreport.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.workreport.OaWorkReportStatusEnum;
import cn.iocoder.yudao.module.oa.enums.workreport.OaWorkReportTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - OA 我的工作汇报分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaWorkReportPageReqVO extends PageParam {

    @Schema(description = "汇报类型", example = "1")
    @InEnum(value = OaWorkReportTypeEnum.class, message = "汇报类型必须是 {value}")
    private Integer type;

    @Schema(description = "汇报状态", example = "1")
    @InEnum(value = OaWorkReportStatusEnum.class, message = "汇报状态必须是 {value}")
    private Integer status;

    @Schema(description = "汇报单号", example = "HB202609140001")
    private String no;

    @Schema(description = "部门编号", example = "1024")
    private Long deptId;

    @Schema(description = "汇报开始时间范围", example = "[\"2026-09-14 00:00:00\", \"2026-09-14 23:59:59\"]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] periodTime;

    @Schema(description = "开始日期", example = "2026-09-14 09:00:00")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "结束日期", example = "2026-09-14 18:00:00")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

    @Schema(description = "创建时间", example = "[\"2026-09-14 00:00:00\", \"2026-09-14 23:59:59\"]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
