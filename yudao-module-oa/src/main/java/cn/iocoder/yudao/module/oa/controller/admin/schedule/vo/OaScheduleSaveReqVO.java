package cn.iocoder.yudao.module.oa.controller.admin.schedule.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.schedule.OaPriorityEnum;
import cn.iocoder.yudao.module.oa.enums.schedule.OaScheduleTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - OA 日程新增/修改 Request VO")
@Data
public class OaScheduleSaveReqVO {

    @Schema(description = "日程编号", example = "1024")
    private Long id;

    @Schema(description = "日程类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "日程类型不能为空")
    @InEnum(value = OaScheduleTypeEnum.class, message = "日程类型必须是 {value}")
    private Integer type;

    @Schema(description = "优先级", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "优先级不能为空")
    @InEnum(value = OaPriorityEnum.class, message = "优先级必须是 {value}")
    private Integer priority;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "周会")
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题长度不能超过 255 个字符")
    private String title;

    @Schema(description = "描述", example = "下周完成盘点差异核实")
    @Size(max = 1000, message = "描述长度不能超过 1000 个字符")
    private String description;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789380000000")
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @Schema(description = "是否提醒", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "是否提醒不能为空")
    private Boolean remind;

    @Schema(description = "参与人用户编号列表", example = "[1, 2]")
    private List<@NotNull(message = "参与人编号不能为空") Long> participantUserIds;

    @JsonIgnore
    @AssertTrue(message = "日程结束时间必须晚于开始时间")
    public boolean isTimeRangeValid() {
        return startTime == null || endTime == null || startTime.isBefore(endTime);
    }

}
