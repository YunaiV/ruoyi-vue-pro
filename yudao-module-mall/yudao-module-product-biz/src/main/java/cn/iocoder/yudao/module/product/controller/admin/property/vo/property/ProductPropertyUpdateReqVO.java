package cn.iocoder.yudao.module.product.controller.admin.property.vo.property;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 规格名称更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductPropertyUpdateReqVO extends ProductPropertyBaseVO {

    @Schema(description = "主键", required = true, example = "1")
    @NotNull(message = "主键不能为空")
    private Long id;

}
