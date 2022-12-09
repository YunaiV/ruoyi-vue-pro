package cn.iocoder.yudao.module.trade.controller.app.base.property;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(title = "用户 App - 规格 + 规格值 Response VO")
@Data
public class AppProductPropertyValueDetailRespVO {

    @Schema(title = "属性的编号", required = true, example = "1")
    private Long propertyId;

    @Schema(title = "属性的名称", required = true, example = "颜色")
    private String propertyName;

    @Schema(title = "属性值的编号", required = true, example = "1024")
    private Long valueId;

    @Schema(title = "属性值的名称", required = true, example = "红色")
    private String valueName;

}
