package cn.iocoder.yudao.module.cms.controller.admin.category.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Admin - CMS Category Simple Response VO")
@Data
public class CmsCategorySimpleRespVO {

    @Schema(description = "Category ID", required = true, example = "1")
    private Long id;

    @Schema(description = "Category Name", required = true, example = "Technology")
    private String name;

    @Schema(description = "Parent Category ID", example = "1024")
    private Long parentId;
}
