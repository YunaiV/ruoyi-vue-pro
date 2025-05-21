package cn.iocoder.yudao.module.cms.controller.admin.article.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import javax.validation.constraints.NotNull;

@Schema(description = "Admin - Update CMS Article Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CmsArticleUpdateReqVO extends CmsArticleBaseVO {

    @Schema(description = "Article ID", required = true, example = "1")
    @NotNull(message = "Article ID cannot be empty")
    private Long id;
}
