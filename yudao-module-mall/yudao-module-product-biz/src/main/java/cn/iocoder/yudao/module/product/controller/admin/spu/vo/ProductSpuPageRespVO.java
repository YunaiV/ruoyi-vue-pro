package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

// TODO @puhui999：可以直接继承 ProductSpuRespVO 么？一般情况下，多返回一些字段问题不大的；另外，是不是使用 ProductSpuRespVO 替代
@Schema(description = "管理后台 - 商品 SPU 分页 response VO")
@Data
public class ProductSpuPageRespVO {

    @Schema(description = "spuId", example = "1")
    private Long id;
    @Schema(description = "商品封面图", example = "1")
    private String picUrl;
    @Schema(description = "商品名称", example = "1")
    private String name;
    @Schema(description = "商品价格", example = "1")
    private Integer price;
    @Schema(description = "商品销量", example = "1")
    private Integer salesCount;
    @Schema(description = "商品排序", example = "1")
    private Integer stock;
    @Schema(description = "商品封面图", example = "1")
    private Integer sort;
    @Schema(description = "商品创建时间", example = "1")
    private LocalDateTime createTime;
    @Schema(description = "商品状态", example = "1")
    private Integer status;

}
