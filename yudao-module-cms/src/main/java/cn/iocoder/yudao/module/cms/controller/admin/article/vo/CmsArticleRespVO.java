package cn.iocoder.yudao.module.cms.controller.admin.article.vo;

import cn.iocoder.yudao.module.cms.controller.admin.tag.vo.CmsTagSimpleRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Admin - CMS Article Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CmsArticleRespVO extends CmsArticleBaseVO {

    @Schema(description = "Article ID", required = true, example = "1")
    private Long id;

    @Schema(description = "View Count", required = true, example = "100")
    private Integer views;

    @Schema(description = "Creation Time", required = true)
    private LocalDateTime createTime;

    // Enriched fields
    @Schema(description = "Category Name", example = "Technology")
    private String categoryName;

    @Schema(description = "List of associated tags")
    private List<CmsTagSimpleRespVO> tags; // Using SimpleRespVO for tags
}
