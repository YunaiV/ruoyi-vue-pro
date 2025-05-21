package cn.iocoder.yudao.module.cms.controller.admin.article.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "Admin - CMS Article Page Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CmsArticlePageReqVO extends PageParam {

    @Schema(description = "Article Title", example = "Spring Boot")
    private String title;

    @Schema(description = "Category ID", example = "1")
    private Long categoryId;

    @Schema(description = "Article Status (0: draft, 1: published)", example = "1")
    private Integer status;

    @Schema(description = "Tag ID to filter by (optional)", example = "10")
    private Long tagId; // For filtering articles that have a specific tag
}
