package cn.iocoder.yudao.module.cms.controller.admin.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.LocalDateTime;

@Schema(description = "Admin - CMS Comment Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CmsCommentRespVO extends CmsCommentBaseVO {

    @Schema(description = "Comment ID", required = true, example = "1")
    private Long id;

    @Schema(description = "Creation Time", required = true)
    private LocalDateTime createTime;

    // Potentially enriched fields, to be populated in service layer
    @Schema(description = "Article Title (if enriched)", example = "My Awesome Article")
    private String articleTitle;

    @Schema(description = "User Nickname (if enriched and userId is present)", example = "CommenterUser")
    private String userNickname; // If userId is present, this could be the user's nickname
}
