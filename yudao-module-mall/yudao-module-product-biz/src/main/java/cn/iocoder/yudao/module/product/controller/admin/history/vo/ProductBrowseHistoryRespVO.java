package cn.iocoder.yudao.module.product.controller.admin.history.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "管理后台 - 商品浏览记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProductBrowseHistoryRespVO {

    @Schema(description = "编号", requiredMode = REQUIRED, example = "1")
    private Long id;

    @Schema(description = "商品 SPU 编号", requiredMode = REQUIRED, example = "29502")
    private Long spuId;

    // ========== 商品相关字段 ==========

    @Schema(description = "商品 SPU 名称", example = "赵六")
    private String spuName;

    @Schema(description = "商品封面图", example = "https://domain/pic.png")
    private String picUrl;

    @Schema(description = "商品单价", example = "100")
    private Integer price;

    @Schema(description = "商品销量", example = "100")
    private Integer salesCount;

    @Schema(description = "库存", example = "100")
    private Integer stock;

}