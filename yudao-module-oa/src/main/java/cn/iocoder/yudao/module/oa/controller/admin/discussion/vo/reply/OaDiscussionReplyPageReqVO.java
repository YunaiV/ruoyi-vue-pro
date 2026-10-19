package cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.reply;

import cn.iocoder.yudao.framework.common.pojo.SortablePageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - OA 讨论回复分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OaDiscussionReplyPageReqVO extends SortablePageParam {

    @Schema(description = "讨论编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "讨论编号不能为空")
    private Long discussionId;

    @Schema(description = "回复人用户编号", example = "1")
    private Long userId;

}
