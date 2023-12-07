package cn.iocoder.yudao.module.crm.controller.admin.product.vo.category;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - CRM 产品分类创建/更新 Request VO")
@Data
public class CrmProductCategoryCreateReqVO{

    @Schema(description = "分类编号", example = "23902")
    private Long id;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotNull(message = "分类名称不能为空")
    private String name;

    @Schema(description = "父级编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "4680")
    @NotNull(message = "父级编号不能为空")
    private Long parentId;

}
