package cn.iocoder.yudao.module.product.controller.admin.property.vo.value;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(title = "管理后台 - 规格值创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductPropertyValueCreateReqVO extends ProductPropertyValueBaseVO {

}
