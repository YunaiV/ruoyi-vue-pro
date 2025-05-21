package cn.iocoder.yudao.module.cms.controller.admin.tag.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class CmsTagBaseVO {

    @Schema(description = "Tag Name", required = true, example = "Java")
    @NotBlank(message = "Tag name cannot be empty")
    @Size(max = 100, message = "Tag name cannot exceed 100 characters")
    private String name;

    @Schema(description = "Tag Slug", required = true, example = "java")
    @NotBlank(message = "Tag slug cannot be empty")
    @Size(max = 100, message = "Tag slug cannot exceed 100 characters")
    // TODO: Add pattern validation for slug (e.g., alphanumeric, dashes)
    private String slug;
}
