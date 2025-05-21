package cn.iocoder.yudao.module.product.controller.admin.property.vo.value;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 商品属性值 Response VO")
@Data
public class ProductPropertyValueRespVO {

    @Schema(description = "主键", example = "1024")
    private Long id;

    @Schema(description = "属性项的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "属性项的编号不能为空")
    private Long propertyId;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "红色")
    @NotEmpty(message = "名称名字不能为空")
    private String name;

    @Schema(description = "备注", example = "颜色")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
