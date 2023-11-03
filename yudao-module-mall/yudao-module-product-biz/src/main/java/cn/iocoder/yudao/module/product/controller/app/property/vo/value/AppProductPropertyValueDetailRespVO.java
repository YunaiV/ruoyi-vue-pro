package cn.iocoder.yudao.module.product.controller.app.property.vo.value;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 商品属性值的明细 Response VO")
@Data
public class AppProductPropertyValueDetailRespVO {

    @Schema(description = "属性的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long propertyId;

    @Schema(description = "属性的名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "颜色")
    private String propertyName;

    @Schema(description = "属性值的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long valueId;

    @Schema(description = "属性值的名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "红色")
    private String valueName;

}
