package cn.iocoder.yudao.module.cms.controller.admin.article;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cms.controller.admin.article.vo.*;
import cn.iocoder.yudao.module.cms.convert.article.CmsArticleConvert;
// CmsArticleDO is not directly used in controller methods after VOs are introduced
import cn.iocoder.yudao.module.cms.service.article.CmsArticleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin - CMS Article Management")
@RestController
@RequestMapping("/cms/admin/articles")
@Validated
public class CmsArticleController {

    @Resource
    private CmsArticleService articleService;

    // CmsArticleConvert is not directly used here because service returns RespVOs
    // If service returned DOs, then controller would use the convert.

    @PostMapping("/create")
    @Operation(summary = "Create a new article")
    @PreAuthorize("@ss.hasPermission('cms:article:create')")
    public CommonResult<Long> createArticle(@Valid @RequestBody CmsArticleCreateReqVO createReqVO) {
        Long articleId = articleService.createArticle(createReqVO);
        return success(articleId);
    }

    @PutMapping("/update")
    @Operation(summary = "Update an existing article")
    @PreAuthorize("@ss.hasPermission('cms:article:update')")
    public CommonResult<Boolean> updateArticle(@Valid @RequestBody CmsArticleUpdateReqVO updateReqVO) {
        articleService.updateArticle(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete an article by ID")
    @Parameter(name = "id", description = "Article ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('cms:article:delete')")
    public CommonResult<Boolean> deleteArticle(@RequestParam("id") Long id) {
        articleService.deleteArticle(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "Get an article by ID")
    @Parameter(name = "id", description = "Article ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('cms:article:query')")
    public CommonResult<CmsArticleRespVO> getArticle(@RequestParam("id") Long id) {
        CmsArticleRespVO article = articleService.getArticle(id);
        return success(article);
    }
    
    @GetMapping("/get-by-slug")
    @Operation(summary = "Get an article by slug")
    @Parameter(name = "slug", description = "Article Slug", required = true, example = "my-first-article")
    @PreAuthorize("@ss.hasPermission('cms:article:query')")
    public CommonResult<CmsArticleRespVO> getArticleBySlug(@RequestParam("slug") String slug) {
        CmsArticleRespVO article = articleService.getArticleBySlug(slug);
        return success(article);
    }

    @GetMapping("/page")
    @Operation(summary = "Get article page")
    @PreAuthorize("@ss.hasPermission('cms:article:query')")
    public CommonResult<PageResult<CmsArticleRespVO>> getArticlePage(@Valid CmsArticlePageReqVO pageVO) {
        PageResult<CmsArticleRespVO> pageResult = articleService.getArticlePage(pageVO);
        return success(pageResult);
    }

    @PostMapping("/publish")
    @Operation(summary = "Publish an article")
    @Parameter(name = "id", description = "Article ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('cms:article:publish')") // Custom permission
    public CommonResult<Boolean> publishArticle(@RequestParam("id") Long id) {
        articleService.publishArticle(id);
        return success(true);
    }

    @PostMapping("/unpublish")
    @Operation(summary = "Unpublish an article")
    @Parameter(name = "id", description = "Article ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('cms:article:unpublish')") // Custom permission
    public CommonResult<Boolean> unpublishArticle(@RequestParam("id") Long id) {
        articleService.unpublishArticle(id);
        return success(true);
    }
}
