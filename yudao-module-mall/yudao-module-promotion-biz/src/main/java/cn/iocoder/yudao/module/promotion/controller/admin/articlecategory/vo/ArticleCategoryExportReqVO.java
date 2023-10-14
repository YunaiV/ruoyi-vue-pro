package cn.iocoder.yudao.module.promotion.controller.admin.articlecategory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 文章分类 Excel 导出 Request VO，参数和 ArticleCategoryPageReqVO 是一致的")
@Data
public class ArticleCategoryExportReqVO {

    @Schema(description = "文章分类名称", example = "秒杀")
    private String name;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
