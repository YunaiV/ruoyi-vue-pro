package cn.iocoder.yudao.module.iot.controller.admin.product.vo.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - IoT 产品分类新增/修改 Request VO")
@Data
public class IotProductCategorySaveReqVO {

    @Schema(description = "分类 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "25284")
    private Long id;

    @Schema(description = "分类名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotEmpty(message = "分类名字不能为空")
    private String name;

    @Schema(description = "分类排序")
    private Integer sort;

    @Schema(description = "分类状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "分类状态不能为空")
    private Integer status;

    @Schema(description = "分类描述", example = "随便")
    private String description;

}