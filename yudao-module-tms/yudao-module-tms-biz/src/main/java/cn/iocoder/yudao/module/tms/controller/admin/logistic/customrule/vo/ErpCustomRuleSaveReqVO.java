package cn.iocoder.yudao.module.tms.controller.admin.logistic.customrule.vo;

import cn.iocoder.yudao.module.system.api.utils.Validation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;

@Schema(description = "管理后台 - ERP 海关规则新增 Request VO")
@Data
public class ErpCustomRuleSaveReqVO {

    @Schema(description = "海关规则id", requiredMode = Schema.RequiredMode.REQUIRED)
    @Null(groups = Validation.OnCreate.class, message = "创建时，海关规则id必须为空")
    private Long id;

    @Schema(description = "国家编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "国家编码不能为空")
    @Size(min = 1, max = 1, message = "更新时，国家编码的数量必须为 1", groups = Validation.OnUpdate.class)
    private Set<Integer> countryCode;

    @Schema(description = "产品id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "产品id不能为空")
    private Long productId;

    @Schema(description = "申报金额")
    @NotNull(message = "申报金额不能为空")
    @Min(value = 0, message = "申报金额不能小于0")
    private Double declaredValue;

    @Schema(description = "申报金额币种")
    @NotNull(message = "申报金额币种不能为空")
    private Integer declaredValueCurrencyCode;

    @Schema(description = "物流属性")
    private Integer logisticAttribute;

    @Schema(description = "条形码")
    @Pattern(regexp = "^[^\\r\\n]*$", message = "条形码不能包含换行符")
    @Pattern(regexp = "^\\S.*\\S$", message = "条形码开头和结尾不能是空格")
    private String fbaBarCode;
}