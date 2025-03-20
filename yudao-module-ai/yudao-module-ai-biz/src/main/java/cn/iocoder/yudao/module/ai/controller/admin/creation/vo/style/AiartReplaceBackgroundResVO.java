package cn.iocoder.yudao.module.ai.controller.admin.creation.vo.style;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 商品替换背景结果 VO")
@Data
public class AiartReplaceBackgroundResVO {

    @Schema(description = "结果url", requiredMode = Schema.RequiredMode.REQUIRED, example = "根据参数不同不一样")
    private String resultImage;

    @Schema(description = "唯一请求 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "由服务端生成")
    private String requestId;

    @Schema(description = "MaskUrl", requiredMode = Schema.RequiredMode.REQUIRED, example = "如果 MaskUrl 未传，则返回使用内置商品分割算法得到的 Mask 结果")
    private String maskImage;
}
