package cn.iocoder.yudao.module.product.controller.admin.property.vo.value;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 商品属性值 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductPropertyValueRespVO extends ProductPropertyValueBaseVO {

    @Schema(description = "编号", required = true, example = "10")
    private Long id;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
