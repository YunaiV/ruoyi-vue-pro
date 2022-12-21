package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 商品 SPU 精简 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductSpuSimpleRespVO extends ProductSpuBaseVO {

    @Schema(description = "主键", required = true, example = "1")
    private Long id;

    @Schema(description = "商品名称", required = true, example = "芋道")
    private String name;

    @Schema(description = " 最小价格，单位使用：分", required = true, example = "1024")
    private Integer minPrice;

    @Schema(description = "最大价格，单位使用：分", required = true, example = "1024")
    private Integer maxPrice;

}
