package cn.iocoder.yudao.module.oa.controller.admin.meetingroom.vo.booking;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 会议室预定分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaMeetingRoomBookingPageReqVO extends PageParam {

    @Schema(description = "单据编号", example = "HY20260914000001")
    private String no;

    @Schema(description = "会议室名称", example = "101 项目讨论室")
    private String roomName;

    @Schema(description = "会议主题", example = "产品需求评审")
    private String title;

    @Schema(description = "主持人姓名", example = "芋艿")
    private String moderatorName;

    @Schema(description = "申请部门编号", example = "100")
    private Long deptId;

    @Schema(description = "审批状态", example = "2")
    private Integer status;

    @Schema(description = "使用状态", example = "0")
    private Integer useStatus;

    @Schema(description = "会议开始时间", example = "[\"2026-09-14 00:00:00\",\"2026-09-14 23:59:59\"]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] startTime;

    @Schema(description = "会议结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] endTime;

    @Schema(description = "创建人编号", example = "1")
    private String creator;

    @Schema(description = "创建时间", example = "[\"2026-09-14 00:00:00\",\"2026-09-14 23:59:59\"]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
