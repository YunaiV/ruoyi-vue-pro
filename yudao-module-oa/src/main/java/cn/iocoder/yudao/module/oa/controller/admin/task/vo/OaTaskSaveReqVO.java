package cn.iocoder.yudao.module.oa.controller.admin.task.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.task.OaTaskTypeEnum;
import cn.iocoder.yudao.module.oa.enums.task.OaTaskStatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - OA 任务新增/修改 Request VO")
@Data
public class OaTaskSaveReqVO {

    @Schema(description = "任务编号", example = "1024")
    private Long id;

    @Schema(description = "任务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "任务类型不能为空")
    @InEnum(value = OaTaskTypeEnum.class, message = "任务类型必须是 {value}")
    private Integer type;

    @Schema(description = "任务状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "任务状态不能为空")
    @InEnum(value = OaTaskStatusEnum.class, message = "任务状态必须是 {value}")
    private Integer status;

    @Schema(description = "是否置顶", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "是否置顶不能为空")
    private Boolean top;

    @Schema(description = "是否取消", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "是否取消不能为空")
    private Boolean canceled;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "完成 OA 任务管理迁移")
    @NotBlank(message = "任务标题不能为空")
    @Size(max = 255, message = "任务标题长度不能超过 255 个字符")
    private String title;

    @Schema(description = "任务描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "完成本周办公用品盘点并提交结果")
    @NotBlank(message = "任务描述不能为空")
    @Size(max = 2000, message = "任务描述长度不能超过 2000 个字符")
    private String description;

    @Schema(description = "任务评价", example = "已按期完成，盘点结果准确")
    @Size(max = 1000, message = "任务评价长度不能超过 1000 个字符")
    private String comment;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789380000000")
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @Schema(description = "接收人用户编号列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1, 2]")
    @NotEmpty(message = "任务接收人不能为空")
    private List<@NotNull(message = "接收人编号不能为空") Long> receiverUserIds;

    @JsonIgnore
    @AssertTrue(message = "任务结束时间必须晚于开始时间")
    public boolean isTimeRangeValid() {
        return startTime == null || endTime == null || startTime.isBefore(endTime);
    }

}
