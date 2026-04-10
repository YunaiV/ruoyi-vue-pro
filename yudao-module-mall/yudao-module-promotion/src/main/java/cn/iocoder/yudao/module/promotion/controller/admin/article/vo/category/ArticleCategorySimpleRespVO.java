package cn.iocoder.yudao.module.promotion.controller.admin.article.vo.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 文章分类精简信息 Response VO")
@Data
public class ArticleCategorySimpleRespVO {

    @Schema(description = "文章分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "19490")
    private Long id;

    @Schema(description = "文章分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "秒杀")
    private String name;

}
