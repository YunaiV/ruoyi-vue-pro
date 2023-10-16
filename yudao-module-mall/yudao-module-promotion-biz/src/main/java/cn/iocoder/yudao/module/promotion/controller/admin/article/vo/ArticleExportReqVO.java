package cn.iocoder.yudao.module.promotion.controller.admin.article.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 文章管理 Excel 导出 Request VO，参数和 ArticlePageReqVO 是一致的")
@Data
public class ArticleExportReqVO {

    @Schema(description = "文章分类编号", example = "15458")
    private Long categoryId;

    @Schema(description = "关联商品编号", example = "22378")
    private Long spuId;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "文章作者")
    private String author;

    @Schema(description = "状态", example = "2")
    private Integer status;

    @Schema(description = "是否热门(小程序)")
    private Boolean recommendHot;

    @Schema(description = "是否轮播图(小程序)")
    private Boolean recommendBanner;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
