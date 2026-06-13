package cn.iocoder.yudao.module.wms.controller.admin.order.shipment.vo.detail;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - WMS 出库单明细 Response VO")
@Data
public class WmsShipmentOrderDetailRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "出库单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long orderId;

    @Schema(description = "商品编号", example = "2048")
    private Long itemId;

    @Schema(description = "商品编号", example = "SPU-APPLE")
    private String itemCode;

    @Schema(description = "商品名称", example = "红富士苹果")
    private String itemName;

    @Schema(description = "商品单位", example = "箱")
    private String unit;

    @Schema(description = "SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long skuId;

    @Schema(description = "规格编号", example = "SKU-APPLE-10KG")
    private String skuCode;

    @Schema(description = "规格名称", example = "10kg 箱装")
    private String skuName;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long warehouseId;

    @Schema(description = "仓库名称", example = "北京仓")
    private String warehouseName;

    @Schema(description = "出库数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    private BigDecimal quantity;

    @Schema(description = "单价", example = "100.00")
    private BigDecimal price;

    @Schema(description = "行金额", example = "1000.00")
    private BigDecimal totalPrice;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
