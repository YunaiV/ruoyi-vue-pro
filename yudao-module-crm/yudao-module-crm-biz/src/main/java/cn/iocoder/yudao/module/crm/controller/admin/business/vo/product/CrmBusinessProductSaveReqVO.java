package cn.iocoder.yudao.module.crm.controller.admin.business.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author lzxhqs
 */
@Schema(description = "管理后台 - CRM 商机产品关联表 创建/更新 Request VO")
@Data
public class CrmBusinessProductSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "32129")
    private Long id;

    @Schema(description = "商机ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "30320")
    @NotNull(message = "商机ID不能为空")
    private Integer businessId;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "30320")
    @NotNull(message = "产品ID不能为空")
    private Integer productId;

    @Schema(description = "产品单价", requiredMode = Schema.RequiredMode.REQUIRED, example = "30320")
    @NotNull(message = "产品单价不能为空")
    private BigDecimal price;

    @Schema(description = "销售价格", requiredMode = Schema.RequiredMode.REQUIRED, example = "30320")
    @NotNull(message = "销售价格不能为空")
    private BigDecimal salesPrice;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "30320")
    @NotNull(message = "数量不能为空")
    private BigDecimal num;

    @Schema(description = "折扣", requiredMode = Schema.RequiredMode.REQUIRED, example = "30320")
    @NotNull(message = "折扣不能为空")
    private BigDecimal discount;

    @Schema(description = "小计（折扣后价格）", requiredMode = Schema.RequiredMode.REQUIRED, example = "30320")
    @NotNull(message = "小计（折扣后价格）不能为空")
    private BigDecimal subtotal;

    @Schema(description = "单位", requiredMode = Schema.RequiredMode.REQUIRED, example = "30320")
    @NotNull(message = "单位不能为空")
    private String unit;
}
