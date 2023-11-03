package cn.iocoder.yudao.module.promotion.controller.app.article.vo.article;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "应用 App - 文章 Response VO")
@Data
public class AppArticleRespVO {

    @Schema(description = "文章编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "文章标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道源码 - 促销模块")
    private String title;

    @Schema(description = "文章作者", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道源码")
    private String author;

    @Schema(description = "分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long categoryId;

    @Schema(description = "图文封面", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/1.png")
    private String picUrl;

    @Schema(description = "文章简介", requiredMode = Schema.RequiredMode.REQUIRED, example = "我是简介")
    private String introduction;

    @Schema(description = "文章内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "我是详细")
    private String description;

    @Schema(description = "发布时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "浏览量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer browseCount;

    @Schema(description = "关联的商品 SPU 编号", example = "1024")
    private Long spuId;

}
