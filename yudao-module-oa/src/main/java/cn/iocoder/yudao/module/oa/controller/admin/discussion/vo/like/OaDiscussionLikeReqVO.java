package cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.like;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.AssertTrue;

@Schema(description = "管理后台 - OA 讨论点赞 Request VO")
@Data
public class OaDiscussionLikeReqVO {

    @Schema(description = "讨论编号，与回复编号必须且只能填写一个", example = "1024")
    private Long discussionId;

    @Schema(description = "回复编号，与讨论编号必须且只能填写一个", example = "2048")
    private Long replyId;

    @JsonIgnore
    @AssertTrue(message = "讨论编号和回复编号必须且只能填写一个")
    @Schema(hidden = true)
    public boolean isTargetValid() {
        if (discussionId != null) {
            return replyId == null;
        }
        return replyId != null;
    }

}
