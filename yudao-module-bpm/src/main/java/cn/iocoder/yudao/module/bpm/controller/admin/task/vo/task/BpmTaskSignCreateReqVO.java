package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Set;

@Schema(description = "管理后台 - 加签任务的创建（加签） Request VO")
@Data
public class BpmTaskSignCreateReqVO {

    @Schema(description = "需要加签的任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "任务编号不能为空")
    private String id;

    @Schema(description = "加签的用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "888")
    @NotEmpty(message = "加签用户不能为空")
    private Set<Long> userIds;

    @Schema(description = "加签类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "before")
    @NotEmpty(message = "加签类型不能为空")
    private String type; // 参见 BpmTaskSignTypeEnum 枚举

    @Schema(description = "加签原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "需要加签")
    @NotEmpty(message = "加签原因不能为空")
    private String reason;

}
