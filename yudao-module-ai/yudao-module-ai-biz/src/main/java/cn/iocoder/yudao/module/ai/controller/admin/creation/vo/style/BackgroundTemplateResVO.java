package cn.iocoder.yudao.module.ai.controller.admin.creation.vo.style;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 商品替换背景模版 VO")
@Data
public class BackgroundTemplateResVO {

    @Schema(description = "一级分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "汽车、通用")
    private String levelFirst;

    @Schema(description = "二级分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "色彩背景")
    private String levelSecond;

    @Schema(description = "背景模版名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "彩虹渐变")
    private String name;

    @Schema(description = "背景模版提示词", requiredMode = Schema.RequiredMode.REQUIRED, example = "背景为彩虹渐变墙面，亮暖色，清新简约风格，喷涂效果")
    private String prompt;

    @Schema(description = "背景模版商品示例", requiredMode = Schema.RequiredMode.REQUIRED, example = "小熊玩偶")
    private String demProduct;

    @Schema(description = "背景模版商品示例效果", requiredMode = Schema.RequiredMode.REQUIRED, example = "url")
    private String demUrl;
}

