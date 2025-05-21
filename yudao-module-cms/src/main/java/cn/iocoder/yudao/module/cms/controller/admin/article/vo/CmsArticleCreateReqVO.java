package cn.iocoder.yudao.module.cms.controller.admin.article.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "Admin - Create CMS Article Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CmsArticleCreateReqVO extends CmsArticleBaseVO {
    // Inherits all fields from CmsArticleBaseVO
    // No additional fields specific to creation that are not in base, for now.
}
