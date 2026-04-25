package cn.iocoder.yudao.module.product.controller.app.image.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Schema(description = "用户 App - 服装图片编辑 Request VO")
@Data
public class AppProductImageEditReqVO {

    @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "商品 SPU 编号不能为空")
    private Long spuId;

    @Schema(description = "编辑操作列表（type: change_color/change_pattern/adjust_fit/change_background）",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Map<String, Object>> edits;

    @Schema(description = "身体参数（用于虚拟试衣）：height_cm / weight_kg / chest_cm 等")
    private Map<String, Object> bodyMeasurements;

    @Schema(description = "批量操作的 SPU 编号列表（批处理时使用）")
    private List<Long> batchSpuIds;

}
