package cn.iocoder.yudao.module.promotion.controller.admin.articlecategory;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.promotion.controller.admin.articlecategory.vo.*;
import cn.iocoder.yudao.module.promotion.convert.articlecategory.ArticleCategoryConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.articlecategory.ArticleCategoryDO;
import cn.iocoder.yudao.module.promotion.service.articlecategory.ArticleCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - 文章分类")
@RestController
@RequestMapping("/promotion/article-category")
@Validated
public class ArticleCategoryController {

    @Resource
    private ArticleCategoryService articleCategoryService;

    @PostMapping("/create")
    @Operation(summary = "创建文章分类")
    @PreAuthorize("@ss.hasPermission('promotion:article-category:create')")
    public CommonResult<Long> createArticleCategory(@Valid @RequestBody ArticleCategoryCreateReqVO createReqVO) {
        return success(articleCategoryService.createArticleCategory(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新文章分类")
    @PreAuthorize("@ss.hasPermission('promotion:article-category:update')")
    public CommonResult<Boolean> updateArticleCategory(@Valid @RequestBody ArticleCategoryUpdateReqVO updateReqVO) {
        articleCategoryService.updateArticleCategory(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除文章分类")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('promotion:article-category:delete')")
    public CommonResult<Boolean> deleteArticleCategory(@RequestParam("id") Long id) {
        articleCategoryService.deleteArticleCategory(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得文章分类")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('promotion:article-category:query')")
    public CommonResult<ArticleCategoryRespVO> getArticleCategory(@RequestParam("id") Long id) {
        ArticleCategoryDO articleCategory = articleCategoryService.getArticleCategory(id);
        return success(ArticleCategoryConvert.INSTANCE.convert(articleCategory));
    }

    @GetMapping("/list")
    @Operation(summary = "获得文章分类列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('promotion:article-category:query')")
    public CommonResult<List<ArticleCategoryRespVO>> getArticleCategoryList(@RequestParam("ids") Collection<Long> ids) {
        List<ArticleCategoryDO> list = articleCategoryService.getArticleCategoryList(ids);
        return success(ArticleCategoryConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获取文章分类精简信息列表", description = "只包含被开启的文章分类，主要用于前端的下拉选项")
    public CommonResult<List<ArticleCategorySimpleRespVO>> getSimpleDeptList() {
        // 获得部门列表，只要开启状态的
        List<ArticleCategoryDO> list = articleCategoryService.getArticleCategoryListByStatus(CommonStatusEnum.ENABLE.getStatus());
        // 降序排序后，返回给前端
        list.sort(Comparator.comparing(ArticleCategoryDO::getSort).reversed());
        return success(ArticleCategoryConvert.INSTANCE.convertList03(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得文章分类分页")
    @PreAuthorize("@ss.hasPermission('promotion:article-category:query')")
    public CommonResult<PageResult<ArticleCategoryRespVO>> getArticleCategoryPage(@Valid ArticleCategoryPageReqVO pageVO) {
        PageResult<ArticleCategoryDO> pageResult = articleCategoryService.getArticleCategoryPage(pageVO);
        return success(ArticleCategoryConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出文章分类 Excel")
    @PreAuthorize("@ss.hasPermission('promotion:article-category:export')")
    @OperateLog(type = EXPORT)
    public void exportArticleCategoryExcel(@Valid ArticleCategoryExportReqVO exportReqVO,
                                           HttpServletResponse response) throws IOException {
        List<ArticleCategoryDO> list = articleCategoryService.getArticleCategoryList(exportReqVO);
        // 导出 Excel
        List<ArticleCategoryExcelVO> datas = ArticleCategoryConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "文章分类.xls", "数据", ArticleCategoryExcelVO.class, datas);
    }

}
