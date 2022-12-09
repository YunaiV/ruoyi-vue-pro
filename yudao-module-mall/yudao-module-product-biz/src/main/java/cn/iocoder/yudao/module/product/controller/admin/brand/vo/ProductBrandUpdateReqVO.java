package cn.iocoder.yudao.module.product.controller.admin.brand.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import javax.validation.constraints.*;

@Schema(title = "管理后台 - 商品品牌更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductBrandUpdateReqVO extends ProductBrandBaseVO {

    @Schema(title = "品牌编号", required = true, example = "1")
    @NotNull(message = "品牌编号不能为空")
    private Long id;

}
