package cn.iocoder.yudao.module.promotion.controller.app.article.vo.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "应用 App - 文章分类 Response VO")
@Data
public class AppArticleCategoryRespVO {

    @Schema(description = "分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "技术")
    private String name;

    @Schema(description = "分类图标", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/1.png")
    private String picUrl;

    // TODO 芋艿：下面 2 个字段，后端要存储，前端不用返回；
//    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
//    private Integer status;
//
//    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
//    private Integer sort;

}
