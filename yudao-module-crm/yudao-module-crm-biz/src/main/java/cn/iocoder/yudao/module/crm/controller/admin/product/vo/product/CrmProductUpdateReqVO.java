package cn.iocoder.yudao.module.crm.controller.admin.product.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.*;

@Schema(description = "管理后台 - 产品更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmProductUpdateReqVO extends CrmProductBaseVO {

    @Schema(description = "主键id", requiredMode = Schema.RequiredMode.REQUIRED, example = "20529")
    @NotNull(message = "主键id不能为空")
    private Long id;

}
