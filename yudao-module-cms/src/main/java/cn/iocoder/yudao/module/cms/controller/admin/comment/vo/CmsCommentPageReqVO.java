package cn.iocoder.yudao.module.cms.controller.admin.comment.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "Admin - CMS Comment Page Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CmsCommentPageReqVO extends PageParam {

    @Schema(description = "Article ID to filter by", example = "101")
    private Long articleId;

    @Schema(description = "User ID to filter by (commenter)", example = "201")
    private Long userId;

    @Schema(description = "Comment Status to filter by (0: pending, 1: approved, 2: spam)", example = "1")
    private Integer status;
}
