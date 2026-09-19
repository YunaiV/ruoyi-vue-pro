package cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.reply;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Schema(description = "管理后台 - OA 讨论回复新增 Request VO")
@Data
public class OaDiscussionReplyCreateReqVO {

    @Schema(description = "讨论编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "讨论编号不能为空")
    private Long discussionId;

    @Schema(description = "父回复编号", example = "0")
    private Long parentId;

    @Schema(description = "回复内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "已收到，将按时完成。")
    @NotBlank(message = "回复内容不能为空")
    private String content;

    /**
     * 楼内评论对齐源端 255 字限制，根回复使用 TEXT 正文。
     *
     * @return 评论长度是否合法
     */
    @AssertTrue(message = "楼内评论不能超过 255 个字符")
    @JsonIgnore
    public boolean isCommentLengthValid() {
        return parentId == null || parentId == 0L || content == null || content.length() <= 255;
    }

}
