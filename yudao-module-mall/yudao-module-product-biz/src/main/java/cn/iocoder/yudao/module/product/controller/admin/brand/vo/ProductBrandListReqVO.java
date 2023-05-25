package cn.iocoder.yudao.module.product.controller.admin.brand.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 商品品牌分页 Request VO")
@Data
public class ProductBrandListReqVO {

    @Schema(description = "品牌名称", example = "苹果")
    private String name;

}
