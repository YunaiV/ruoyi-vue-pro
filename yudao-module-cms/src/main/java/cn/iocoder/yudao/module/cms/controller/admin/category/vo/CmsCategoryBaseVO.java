package cn.iocoder.yudao.module.cms.controller.admin.category.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class CmsCategoryBaseVO {

    @Schema(description = "Category Name", required = true, example = "Technology")
    @NotBlank(message = "Category name cannot be empty")
    @Size(max = 100, message = "Category name cannot exceed 100 characters")
    private String name;

    @Schema(description = "Category Slug", required = true, example = "tech")
    @NotBlank(message = "Category slug cannot be empty")
    @Size(max = 100, message = "Category slug cannot exceed 100 characters")
    // TODO: Add pattern validation for slug (e.g., alphanumeric, dashes)
    private String slug;

    @Schema(description = "Parent Category ID", example = "1024")
    private Long parentId; // If null or 0, it's a root category

    @Schema(description = "Category Description", example = "All about technology trends.")
    @Size(max = 500, message = "Description length cannot exceed 500 characters")
    private String description;
}
