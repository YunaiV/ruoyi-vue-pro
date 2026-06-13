package cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.sku;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - WMS 商品 SKU 分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsItemSkuPageReqVO extends PageParam {

    @Schema(description = "商品编号", example = "I00000001")
    private String itemCode;

    @Schema(description = "商品名称", example = "华为 nova flip")
    private String itemName;

    @Schema(description = "商品分类编号", example = "1")
    private Long categoryId;

    @Schema(description = "商品品牌编号", example = "1")
    private Long brandId;

    @Schema(description = "规格编号", example = "S00000001")
    private String code;

    @Schema(description = "规格名称", example = "黑色")
    private String name;

    @Schema(description = "条码", example = "12345678")
    private String barCode;

}
