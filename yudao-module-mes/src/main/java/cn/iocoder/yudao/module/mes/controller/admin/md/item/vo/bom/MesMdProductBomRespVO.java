package cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.bom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 产品BOM Response VO")
@Data
public class MesMdProductBomRespVO {

    @Schema(description = "BOM编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "物料产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long itemId;

    @Schema(description = "BOM物料ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Long bomItemId;

    @Schema(description = "物料使用比例", requiredMode = Schema.RequiredMode.REQUIRED, example = "1.5")
    private BigDecimal quantity;

    @Schema(description = "是否启用", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    // ========== 关联展示字段 ==========

    @Schema(description = "BOM物料编码", example = "ITEM001")
    private String bomItemCode;

    @Schema(description = "BOM物料名称", example = "螺丝")
    private String bomItemName;

    @Schema(description = "BOM物料规格", example = "M6*20")
    private String bomItemSpecification;

    @Schema(description = "计量单位名称", example = "个")
    private String unitMeasureName;

    @Schema(description = "产品物料标识", example = "ITEM")
    private String itemOrProduct;

}
