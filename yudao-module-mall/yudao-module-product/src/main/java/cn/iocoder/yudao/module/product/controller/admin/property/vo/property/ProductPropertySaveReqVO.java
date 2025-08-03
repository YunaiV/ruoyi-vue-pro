package cn.iocoder.yudao.module.product.controller.admin.property.vo.property;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Schema(description = "管理后台 - 属性项新增/更新 Request VO")
@Data
public class ProductPropertySaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "颜色")
    @NotBlank(message = "名称不能为空")
    private String name;

    @Schema(description = "备注", example = "颜色")
    private String remark;

}
