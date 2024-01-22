package cn.iocoder.yudao.module.promotion.controller.admin.article.vo.article;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 文章管理 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class ArticleBaseVO {

    @Schema(description = "文章分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15458")
    @NotNull(message = "文章分类编号不能为空")
    private Long categoryId;

    @Schema(description = "关联商品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "22378")
    @NotNull(message = "关联商品不能为空")
    private Long spuId;

    @Schema(description = "文章标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "这是一个标题")
    @NotNull(message = "文章标题不能为空")
    private String title;

    @Schema(description = "文章作者", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String author;

    @Schema(description = "文章封面图片地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn")
    @NotNull(message = "文章封面图片地址不能为空")
    private String picUrl;

    @Schema(description = "文章简介", requiredMode = Schema.RequiredMode.REQUIRED, example = "这是一个简介")
    private String introduction;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "是否热门(小程序)", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否热门(小程序)不能为空")
    private Boolean recommendHot;

    @Schema(description = "是否轮播图(小程序)", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否轮播图(小程序)不能为空")
    private Boolean recommendBanner;

    @Schema(description = "文章内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "这是文章内容")
    @NotNull(message = "文章内容不能为空")
    private String content;

}
