package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 商品 SPU 分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductSpuPageReqVO extends PageParam {

    @Schema(description = "商品名称", example = "yutou")
    private String name;

    @Schema(description = "商品编码", example = "yudaoyuanma")
    private String code;

    @Schema(description = "分类编号", example = "1")
    private Long categoryId;

    @Schema(description = "商品品牌编号", example = "1")
    private Long brandId;

    @Schema(description = "上下架状态", example = "1")
    private Integer status;

    @Schema(description = "销量最小值", example = "1")
    private Integer salesCountMin;

    @Schema(description = "销量最大值", example = "1024")
    private Integer salesCountMax;

    @Schema(description = "市场价最小值", example = "1")
    private Integer marketPriceMin;

    @Schema(description = "市场价最大值", example = "1024")
    private Integer marketPriceMax;

    @Schema(description = "是否库存告警", example = "true")
    private Boolean alarmStock;

}
