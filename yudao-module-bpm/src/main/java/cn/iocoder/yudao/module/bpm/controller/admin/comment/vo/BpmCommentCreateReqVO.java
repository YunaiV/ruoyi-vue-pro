package cn.iocoder.yudao.module.bpm.controller.admin.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - 流程评论创建 Request VO")
@Data
public class BpmCommentCreateReqVO {

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotEmpty(message = "任务编号不能为空")
    private String taskId;

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "请关注报销发票附件")
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过 500 个字符")
    private String message;

}
