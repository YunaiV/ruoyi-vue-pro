package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(title = "管理后台 - 商品 SPU 精简 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductSpuSimpleRespVO extends ProductSpuBaseVO {

    @Schema(title = "主键", required = true, example = "1")
    private Long id;

    @Schema(title = "商品名称", required = true, example = "芋道")
    private String name;

    @Schema(title = " 最小价格，单位使用：分", required = true, example = "1024")
    private Integer minPrice;

    @Schema(title = "最大价格，单位使用：分", required = true, example = "1024")
    private Integer maxPrice;

}
