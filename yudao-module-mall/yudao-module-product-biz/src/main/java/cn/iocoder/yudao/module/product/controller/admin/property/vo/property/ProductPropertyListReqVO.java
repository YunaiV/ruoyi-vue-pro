package cn.iocoder.yudao.module.product.controller.admin.property.vo.property;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

@Schema(title = "管理后台 - 规格名称 List Request VO")
@Data
@ToString(callSuper = true)
public class ProductPropertyListReqVO {

    @Schema(title = "规格名称", example = "颜色")
    private String name;

    @Schema(title = "状态", required = true, example = "1", description = "参见 CommonStatusEnum 枚举")
    private Integer status;

}
