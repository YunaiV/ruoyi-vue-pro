package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 商品 SPU Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductSpuRespVO extends ProductSpuBaseVO {

    @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "111")
    private Long id;

    @Schema(description = "商品价格", requiredMode = Schema.RequiredMode.REQUIRED, example = "1999")
    private Integer price;

    @Schema(description = "商品销量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2000")
    private Integer salesCount;

    @Schema(description = "市场价，单位使用：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "199")
    private Integer marketPrice;

    @Schema(description = "成本价，单位使用：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "19")
    private Integer costPrice;

    @Schema(description = "商品库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "10000")
    private Integer stock;

    @Schema(description = "商品创建时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2023-05-24 00:00:00")
    private LocalDateTime createTime;

    @Schema(description = "商品状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "浏览量", requiredMode = Schema.RequiredMode.REQUIRED, example = "888")
    private Integer browseCount;

}
