package cn.iocoder.yudao.module.promotion.controller.app.article.vo.article;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "应用 App - 文章 Response VO")
@Data
public class AppArticleRespVO {

    @Schema(description = "文章编号", required = true, example = "1")
    private Long id;

    @Schema(description = "文章标题", required = true, example = "芋道源码 - 促销模块")
    private String title;

    @Schema(description = "文章作者", required = true, example = "芋道源码")
    private String author;

    @Schema(description = "分类编号", required = true, example = "2048")
    private Long categoryId;

    @Schema(description = "图文封面", required = true, example = "https://www.iocoder.cn/1.png")
    private String picUrl;

    @Schema(description = "文章简介", required = true, example = "我是简介")
    private String introduction;

    @Schema(description = "文章内容", required = true, example = "我是详细")
    private String description;

    @Schema(description = "发布时间", required = true)
    private LocalDateTime createTime;

    @Schema(description = "浏览量", required = true, example = "1024")
    private Integer browseCount;

    @Schema(description = "关联的商品 SPU 编号", example = "1024")
    private Long spuId;

// TODO 芋艿：下面 2 个字段，后端要存储，前端不用返回；
//    @Schema(description = "是否热卖推荐", required = true, example = "true")
//    private Boolean recommendHot;
//
//    @Schema(description = "是否 Banner 推荐", required = true, example = "true")
//    private Boolean recommendBanner;

}
