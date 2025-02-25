package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.req;

import cn.iocoder.yudao.module.erp.controller.admin.tools.validation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP采购申请单子新增/修改 Request VO")
@Data
public class ErpPurchaseRequestItemsSaveReqVO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @Null(groups = validation.OnCreate.class, message = "创建时，子项id必须为空")
    @NotNull(groups = validation.OnUpdate.class, message = "更新时，子项id不能为空")
    private Long id;

    @Schema(description = "商品id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品id不能为空")
    private Long productId;

    @Schema(description = "仓库id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long warehouseId;

    @Schema(description = "申请数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "申请数量不能为空")
    @Positive(message = "申请数量必须为正数")
    private Integer count;

    @Schema(description = "批准数量", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @PositiveOrZero(message = "批准数量不能为负数")
    @Null(groups = validation.OnCreate.class, message = "创建时，批准数量必须为空")
    private Integer approveCount;

    @Schema(description = "含税单价", requiredMode = Schema.RequiredMode.REQUIRED)
    @Positive(message = "含税单价必须为正数")
    private BigDecimal actTaxPrice;

    @Schema(description = "价税合计", requiredMode = Schema.RequiredMode.REQUIRED)
    @DecimalMin(value = "0.0", message = "价税合计必须大于0")
    private BigDecimal allAmount;

    @Schema(description = "参考单价", requiredMode = Schema.RequiredMode.REQUIRED)
    @DecimalMin(value = "0.0", message = "参考单价必须大于0")
    private BigDecimal referenceUnitPrice;

    //是否计算得到？待确认
    @Schema(description = "税额，单位：元", requiredMode = Schema.RequiredMode.REQUIRED)
    @DecimalMin(value = "0.0", message = "税额必须大于0")
    private BigDecimal taxPrice;

    @Schema(description = "增值税税率，百分比", requiredMode = Schema.RequiredMode.REQUIRED)
    @DecimalMin(value = "0.0", message = "税率不能为负")
    @DecimalMax(value = "1.0", message = "税率不能超过100%")
    private BigDecimal taxPercent;
}
