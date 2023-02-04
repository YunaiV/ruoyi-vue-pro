package cn.iocoder.yudao.module.product.controller.app.spu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "用户 App - 商品 SPU 分页项 Response VO")
@Data
public class AppProductSpuPageItemRespVO {

    @Schema(description = "商品 SPU 编号", required = true, example = "1")
    private Long id;

    @Schema(description = "商品名称", required = true, example = "芋道")
    @NotEmpty(message = "商品名称不能为空")
    private String name;

    @Schema(description = "分类编号", required = true)
    @NotNull(message = "分类编号不能为空")
    private Long categoryId;

    @Schema(description = "商品图片的数组", required = true)
    private List<String> picUrls;

    @Schema(description = " 最小价格，单位使用：分", required = true, example = "1024")
    private Integer minPrice;

    @Schema(description = "最大价格，单位使用：分", required = true, example = "1024")
    private Integer maxPrice;

    // ========== 统计相关字段 =========

    @Schema(description = "商品销量", example = "1024")
    private Integer salesCount;

}
