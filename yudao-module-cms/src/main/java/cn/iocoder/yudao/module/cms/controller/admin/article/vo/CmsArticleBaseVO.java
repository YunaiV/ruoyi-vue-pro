package cn.iocoder.yudao.module.cms.controller.admin.article.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CmsArticleBaseVO {

    @Schema(description = "Article Title", required = true, example = "Understanding Spring Boot")
    @NotBlank(message = "Article title cannot be empty")
    @Size(max = 255, message = "Article title cannot exceed 255 characters")
    private String title;

    @Schema(description = "Article Slug", required = true, example = "understanding-spring-boot")
    @NotBlank(message = "Article slug cannot be empty")
    @Size(max = 255, message = "Article slug cannot exceed 255 characters")
    // TODO: Add pattern validation for slug
    private String slug;

    @Schema(description = "Article Content", required = true, example = "Spring Boot is a powerful framework...")
    @NotBlank(message = "Article content cannot be empty")
    private String content;

    @Schema(description = "Author ID (User ID)", example = "1024")
    // @NotNull(message = "Author ID cannot be empty") // Or set by backend
    private Long authorId;

    @Schema(description = "Category ID", required = true, example = "1")
    @NotNull(message = "Category ID cannot be empty")
    private Long categoryId;

    @Schema(description = "Article Status (0: draft, 1: published)", example = "0")
    // @NotNull(message = "Status cannot be empty") // Can be defaulted in service
    private Integer status;

    @Schema(description = "Published At (optional, set when status is published)", example = "2023-10-26T10:00:00")
    private LocalDateTime publishedAt;

    @Schema(description = "Cover Image URL", example = "https://example.com/cover.jpg")
    @Size(max = 1024, message = "Cover image URL cannot exceed 1024 characters")
    private String coverImageUrl;

    @Schema(description = "Meta Description (for SEO)", example = "A comprehensive guide to Spring Boot.")
    @Size(max = 500, message = "Meta description cannot exceed 500 characters")
    private String metaDescription;

    @Schema(description = "Meta Keywords (for SEO, comma-separated)", example = "Spring Boot, Java, Backend")
    @Size(max = 255, message = "Meta keywords cannot exceed 255 characters")
    private String metaKeywords;

    @Schema(description = "List of Tag IDs associated with the article")
    // @NotEmpty(message = "At least one tag must be selected") // Business rule: can be optional
    private List<Long> tagIds;
}
