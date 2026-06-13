package cn.iocoder.yudao.module.wms.controller.admin.order.check.vo.detail;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - WMS 盘库单明细保存 Request VO")
@Data
public class WmsCheckOrderDetailSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "SKU 不能为空")
    private Long skuId;

    @Schema(description = "库存编号", example = "1024")
    private Long inventoryId;

    @Schema(description = "入库时间")
    private LocalDateTime receiptTime;

    @Schema(description = "账面数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    @NotNull(message = "账面数量不能为空")
    @DecimalMin(value = "0", message = "账面数量不能小于 0")
    private BigDecimal quantity;

    @Schema(description = "实盘数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "98.00")
    @NotNull(message = "实盘数量不能为空")
    @DecimalMin(value = "0", message = "实盘数量不能小于 0")
    private BigDecimal checkQuantity;

    @Schema(description = "单价", example = "1000.00")
    @DecimalMin(value = "0", message = "单价不能小于 0")
    private BigDecimal price;

}
