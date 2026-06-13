package cn.iocoder.yudao.module.wms.controller.admin.inventory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - WMS 库存统计 Response VO")
@Data
public class WmsInventoryRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "商品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long itemId;
    @Schema(description = "商品编码", example = "ITEM001")
    private String itemCode;
    @Schema(description = "商品名称", example = "红富士苹果")
    private String itemName;
    @Schema(description = "商品单位", example = "箱")
    private String unit;

    @Schema(description = "商品 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "4096")
    private Long skuId;
    @Schema(description = "规格编号", example = "SKU001")
    private String skuCode;
    @Schema(description = "规格名称", example = "10kg 箱装")
    private String skuName;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8192")
    private Long warehouseId;
    @Schema(description = "仓库名称", example = "成品仓")
    private String warehouseName;

    @Schema(description = "库存数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    private BigDecimal quantity;
    @Schema(description = "备注", example = "备注")
    private String remark;
    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
