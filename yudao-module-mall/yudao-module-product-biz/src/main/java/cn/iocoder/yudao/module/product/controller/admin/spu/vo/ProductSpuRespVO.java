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

    @Schema(description = "主键", required = true, example = "1")
    private Long id;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    // ========== SKU 相关字段 =========

    @Schema(description = "库存", required = true, example = "true")
    private Integer totalStock;

    @Schema(description = " 最小价格，单位使用：分", required = true, example = "1024")
    private Integer minPrice;

    @Schema(description = "最大价格，单位使用：分", required = true, example = "1024")
    private Integer maxPrice;

    @Schema(description = "商品销量", example = "1024")
    private Integer salesCount;

    // ========== 统计相关字段 =========

    @Schema(description = "点击量", example = "1024")
    private Integer clickCount;
}
