package cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.vote;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - OA 讨论投票 Request VO")
@Data
public class OaDiscussionVoteReqVO {

    @Schema(description = "讨论编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "讨论编号不能为空")
    private Long discussionId;

    @Schema(description = "投票选项编号列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1, 2]")
    @NotEmpty(message = "投票选项不能为空")
    private List<@NotNull(message = "投票选项编号不能为空") Long> optionIds;

}
