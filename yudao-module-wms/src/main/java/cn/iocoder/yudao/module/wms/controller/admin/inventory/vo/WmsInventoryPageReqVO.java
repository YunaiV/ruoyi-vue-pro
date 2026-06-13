package cn.iocoder.yudao.module.wms.controller.admin.inventory.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

@Schema(description = "管理后台 - WMS 库存统计分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsInventoryPageReqVO extends PageParam {

    /**
     * 按仓库维度统计
     */
    public static final String TYPE_WAREHOUSE = "warehouse";

    /**
     * 按商品维度统计
     */
    public static final String TYPE_ITEM = "item";

    @Schema(description = "统计维度", requiredMode = Schema.RequiredMode.REQUIRED, example = "warehouse")
    @NotBlank(message = "统计维度不能为空")
    private String type;

    @Schema(description = "商品编号", example = "ITEM001")
    private String itemCode;

    @Schema(description = "商品名称", example = "红富士苹果")
    private String itemName;

    @Schema(description = "商品 SKU 编号", example = "1024")
    private Long skuId;

    @Schema(description = "规格编号", example = "SKU001")
    private String skuCode;

    @Schema(description = "规格名称", example = "10kg 箱装")
    private String skuName;

    @Schema(description = "仓库编号", example = "2048")
    private Long warehouseId;

    @Schema(description = "最小库存数量", example = "0.01")
    private BigDecimal minQuantity;

    @Schema(description = "是否只查询正库存", example = "true")
    private Boolean onlyPositiveQuantity;

}
