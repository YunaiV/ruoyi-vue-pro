package cn.iocoder.yudao.module.cms.controller.admin.category.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "Admin - CMS Category Page Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CmsCategoryPageReqVO extends PageParam {

    @Schema(description = "Category Name", example = "Technology")
    private String name; // For filtering by name

    // Add other filterable fields if needed, e.g., slug, status (if categories can be disabled)
    // @Schema(description = "Category Slug", example = "tech")
    // private String slug;
}
