package cn.iocoder.yudao.module.cms.controller.admin.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "Admin - Create CMS Comment Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CmsCommentCreateReqVO extends CmsCommentBaseVO {
    // Inherits all fields from CmsCommentBaseVO
    // Admin might set initial status, or it defaults in service layer.
    // User ID might be set if admin is creating on behalf of a user.
}
