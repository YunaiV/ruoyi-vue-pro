package cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.item;

import cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.sku.WmsItemSkuSaveReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - WMS 商品创建/更新 Request VO")
@Data
public class WmsItemSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "商品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "ITEM001")
    @NotBlank(message = "商品编号不能为空")
    @Size(max = 20, message = "商品编号长度不能超过 20 个字符")
    private String code;

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "华为 nova flip")
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 60, message = "商品名称长度不能超过 60 个字符")
    private String name;

    @Schema(description = "商品分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "商品分类不能为空")
    private Long categoryId;

    @Schema(description = "单位", example = "台")
    @Size(max = 20, message = "单位长度不能超过 20 个字符")
    private String unit;

    @Schema(description = "商品品牌编号", example = "1")
    private Long brandId;

    @Schema(description = "备注", example = "备注")
    @Size(max = 255, message = "备注长度不能超过 255 个字符")
    private String remark;

    @Schema(description = "规格列表")
    @Valid
    @NotEmpty(message = "至少包含一个商品规格")
    private List<WmsItemSkuSaveReqVO> skus;

}
