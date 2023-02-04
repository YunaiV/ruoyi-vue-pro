package cn.iocoder.yudao.module.product.controller.admin.property.vo.property;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 属性项更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductPropertyUpdateReqVO extends ProductPropertyBaseVO {

    @Schema(description = "主键", required = true, example = "1")
    @NotNull(message = "主键不能为空")
    private Long id;

}
