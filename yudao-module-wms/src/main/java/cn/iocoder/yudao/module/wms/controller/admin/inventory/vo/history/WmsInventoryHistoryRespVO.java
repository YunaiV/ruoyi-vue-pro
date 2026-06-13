package cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.history;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - WMS 库存流水 Response VO")
@Data
public class WmsInventoryHistoryRespVO {

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

    @Schema(description = "库存变化数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10.00")
    private BigDecimal quantity;
    @Schema(description = "变化前库存数量", example = "90.00")
    private BigDecimal beforeQuantity;
    @Schema(description = "变化后库存数量", example = "100.00")
    private BigDecimal afterQuantity;
    @Schema(description = "单价", example = "1000.00")
    private BigDecimal price;
    @Schema(description = "库存变化金额", example = "10000.00")
    private BigDecimal totalPrice;
    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "单据编号", example = "1024")
    private Long orderId;
    @Schema(description = "单据号", example = "RK202605110001")
    private String orderNo;
    @Schema(description = "单据类型", example = "1")
    private Integer orderType;
    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
