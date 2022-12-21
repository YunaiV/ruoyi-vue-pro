package cn.iocoder.yudao.module.product.controller.admin.property.vo.property;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

@Schema(description = "管理后台 - 规格名称 List Request VO")
@Data
@ToString(callSuper = true)
public class ProductPropertyListReqVO {

    @Schema(description = "规格名称", example = "颜色")
    private String name;

    @Schema(description = "状态,参见 CommonStatusEnum 枚举", required = true, example = "1")
    private Integer status;

}
