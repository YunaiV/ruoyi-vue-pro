package cn.iocoder.yudao.module.cms.controller.admin.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotNull;

@Schema(description = "Admin - Update CMS Comment Request VO (primarily for status)")
@Data
public class CmsCommentUpdateReqVO {

    @Schema(description = "Comment ID", required = true, example = "1")
    @NotNull(message = "Comment ID cannot be empty")
    private Long id;

    @Schema(description = "New Comment Status (0: pending, 1: approved, 2: spam)", required = true, example = "1")
    @NotNull(message = "Status cannot be empty")
    // TODO: Consider an Enum for status and a custom validator
    private Integer status;

    // Admin might also be allowed to edit content, if so, add:
    // @Schema(description = "Comment Content", example = "Updated thoughts on the article.")
    // @Size(max = 2000, message = "Comment content cannot exceed 2000 characters")
    // private String content;
}
