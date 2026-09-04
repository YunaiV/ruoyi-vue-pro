package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - PMS 工作项评论新增/修改 Request VO")
@Data
public class PmsWorkItemCommentSaveReqVO {

    @Schema(description = "评论编号", example = "1024")
    private Long id;

    @Schema(description = "工作项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "工作项编号不能为空")
    private Long workItemId;

    @Schema(description = "主评论编号，回复时传入", example = "1024")
    private Long mainId;

    @Schema(description = "回复对象用户编号", example = "2048")
    private Long replyUserId;

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "已处理")
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 2000, message = "评论内容不能超过 2000 个字符")
    private String content;

}
