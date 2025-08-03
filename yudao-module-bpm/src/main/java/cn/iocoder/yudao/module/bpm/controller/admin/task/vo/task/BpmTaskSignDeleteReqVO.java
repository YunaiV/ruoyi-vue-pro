package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Schema(description = "管理后台 - 加签任务的删除（减签） Request VO")
@Data
public class BpmTaskSignDeleteReqVO {

    @Schema(description = "被减签的任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "任务编号不能为空")
    private String id;

    @Schema(description = "加签原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "需要减签")
    @NotEmpty(message = "加签原因不能为空")
    private String reason;

}
