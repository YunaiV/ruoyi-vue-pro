package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.storyboard;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - AI 短剧镜头视频提交结果确认 Request VO：提交结果未知时，填写在服务商控制台核对到的任务编号")
@Data
public class AiDramaVideoSubmissionResolveReqVO {

    @Schema(description = "提交记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "提交记录编号不能为空")
    private Long id;

    @Schema(description = "上游视频任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "cgt-2025")
    @NotBlank(message = "任务编号不能为空")
    @Size(max = 128, message = "任务编号不能超过 128 个字符")
    private String taskId;

}
