package cn.iocoder.yudao.module.product.controller.admin.property.vo.value;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import javax.validation.constraints.*;

@Schema(title = "管理后台 - 规格值更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductPropertyValueUpdateReqVO extends ProductPropertyValueBaseVO {

    @Schema(title = "主键", required = true, example = "1024")
    @NotNull(message = "主键不能为空")
    private Long id;

}
