package cn.iocoder.yudao.module.product.controller.admin.property.vo.value;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 商品属性值更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductPropertyValueUpdateReqVO extends ProductPropertyValueBaseVO {

    @Schema(description = "主键", required = true, example = "1024")
    @NotNull(message = "主键不能为空")
    private Long id;

}
