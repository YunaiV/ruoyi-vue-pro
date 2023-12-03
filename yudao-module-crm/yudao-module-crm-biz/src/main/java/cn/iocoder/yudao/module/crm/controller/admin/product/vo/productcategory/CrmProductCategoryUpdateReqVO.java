package cn.iocoder.yudao.module.crm.controller.admin.product.vo.productcategory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 产品分类更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmProductCategoryUpdateReqVO extends CrmProductCategoryBaseVO {

    @Schema(description = "主键 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "23902")
    @NotNull(message = "主键 id 不能为空")
    private Long id;

}
