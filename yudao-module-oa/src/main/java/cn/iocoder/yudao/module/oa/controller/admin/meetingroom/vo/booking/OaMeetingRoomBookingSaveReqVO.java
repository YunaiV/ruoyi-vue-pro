package cn.iocoder.yudao.module.oa.controller.admin.meetingroom.vo.booking;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.meetingroom.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 会议室预定新增/修改 Request VO")
@Data
public class OaMeetingRoomBookingSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "会议室编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "会议室不能为空")
    private Long roomId;

    @Schema(description = "会议主题", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品需求评审")
    @NotBlank(message = "会议主题不能为空")
    @Size(max = 200, message = "会议主题长度不能超过 {max} 个字符")
    private String title;

    @Schema(description = "会议开始时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "1789351200000", type = "integer", format = "int64")
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "会议结束时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "1789354800000", type = "integer", format = "int64")
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @Schema(description = "主持人用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "主持人不能为空")
    private Long moderatorUserId;

    @Schema(description = "参会人用户编号列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1,2]")
    @NotNull(message = "参会人不能为空")
    private List<@NotNull(message = "参会人不能为空") Long> attendeeUserIds;

    @Schema(description = "会议提醒", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "会议提醒不能为空")
    @InEnum(value = OaMeetingRoomReminderTypeEnum.class, message = "会议提醒不正确")
    private Integer reminderType;

    @Schema(description = "会议说明", example = "讨论产品需求和排期")
    @Size(max = 500, message = "会议说明长度不能超过 {max} 个字符")
    private String description;

    @Schema(description = "申请备注", example = "请提前安排会议室")
    @Size(max = 500, message = "申请备注长度不能超过 {max} 个字符")
    private String remark;

    @Schema(description = "附件地址列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[\"https://www.iocoder.cn/meeting.pdf\"]")
    @NotNull(message = "附件地址列表不能为空")
    private List<@NotBlank(message = "附件地址列表不能为空") String> fileUrls;

}
