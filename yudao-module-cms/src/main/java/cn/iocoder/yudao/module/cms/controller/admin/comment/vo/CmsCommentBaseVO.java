package cn.iocoder.yudao.module.cms.controller.admin.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.Email;

@Data
public class CmsCommentBaseVO {

    @Schema(description = "Article ID", required = true, example = "101")
    @NotNull(message = "Article ID cannot be empty")
    private Long articleId;

    @Schema(description = "User ID (if commented by a logged-in user)", example = "201")
    private Long userId;

    @Schema(description = "Author Name (if guest or preferred display name)", example = "John Doe")
    @Size(max = 100, message = "Author name cannot exceed 100 characters")
    private String authorName;

    @Schema(description = "Author Email (if guest)", example = "john.doe@example.com")
    @Email(message = "Please provide a valid email address")
    @Size(max = 255, message = "Author email cannot exceed 255 characters")
    private String authorEmail;

    @Schema(description = "Comment Content", required = true, example = "Great article!")
    @NotBlank(message = "Comment content cannot be empty")
    @Size(max = 2000, message = "Comment content cannot exceed 2000 characters")
    private String content;

    @Schema(description = "Parent Comment ID (for replies)", example = "301")
    private Long parentId;

    @Schema(description = "Comment Status (0: pending, 1: approved, 2: spam)", required = true, example = "0")
    @NotNull(message = "Status cannot be empty")
    // TODO: Consider an Enum for status and a custom validator
    private Integer status;
}
