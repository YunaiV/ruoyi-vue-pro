package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request;

import cn.iocoder.yudao.module.erp.controller.admin.tools.validation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP采购申请单子新增/修改 Request VO")
@Data
public class ErpPurchaseRequestItemsSaveReqVO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @Null(groups = validation.OnCreate.class, message = "创建时，id必须为空")
    @NotNull(groups = validation.OnUpdate.class, message = "更新时，id不能为空")
    private Long id;

    @Schema(description = "商品id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品id不能为空")
    private Long productId;

//    @Schema(description = "申请单id", requiredMode = Schema.RequiredMode.REQUIRED)
//    @Null(groups = validation.OnCreate.class, message = "创建时，申请单id必须为空")
//    @NotNull(groups = validation.OnUpdate.class, message = "更新时，申请单id不能为空")
//    private Long requestId;

    @Schema(description = "申请数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "申请数量不能为空")
    @Positive(message = "申请数量必须为正数")
    private Integer count;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long warehouseId;

    @Schema(description = "批准数量", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "8")
    @PositiveOrZero(message = "批准数量不能为负数")
    private Integer approveCount;

    @Schema(description = "含税单价", requiredMode = Schema.RequiredMode.REQUIRED, example = "50.5")
    @Positive(message = "含税单价必须为正数")
    private BigDecimal actTaxPrice;

    @Schema(description = "价税合计", requiredMode = Schema.RequiredMode.REQUIRED, example = "505.5")
    @DecimalMin(value = "0.0", message = "价税合计必须大于0")
    private BigDecimal allAmount;

    @Schema(description = "参考单价", requiredMode = Schema.RequiredMode.REQUIRED, example = "50.5")
    @DecimalMin(value = "0.0", message = "参考单价必须大于0")
    private BigDecimal referenceUnitPrice;

    @Schema(description = "税额，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "5.5")
    @DecimalMin(value = "0.0", message = "税额必须大于0")
    private BigDecimal taxPrice;

    @Schema(description = "税率，百分比", requiredMode = Schema.RequiredMode.REQUIRED, example = "0.13")
    @DecimalMin(value = "0.0", message = "税率不能为负")
    @DecimalMax(value = "1.0", message = "税率不能超过100%")
    private BigDecimal taxPercent;
}
