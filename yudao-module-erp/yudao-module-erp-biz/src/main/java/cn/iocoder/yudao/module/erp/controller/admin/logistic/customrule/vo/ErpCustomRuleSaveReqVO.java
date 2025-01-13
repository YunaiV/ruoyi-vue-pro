package cn.iocoder.yudao.module.erp.controller.admin.logistic.customrule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 海关规则新增/修改 Request VO")
@Data
public class ErpCustomRuleSaveReqVO {

    @Schema(description = "海关规则id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "国家编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "国家编码不能为空")
    private Integer countryCode;


    @Schema(description = "产品id",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "产品id不能为空")
    private Long productId;

    @Schema(description = "申报品名（英文）")
    @NotEmpty(message = "申报品名（英文）不能为空")
    private String declaredTypeEn;

    @Schema(description = "申报品名")
    private String declaredType;

    @Schema(description = "申报金额")
    @NotNull(message = "申报金额不能为空")
    private Double declaredValue;

    @Schema(description = "申报金额币种")
    @NotNull(message = "申报金额币种不能为空")
    private Integer declaredValueCurrencyCode;

    @Schema(description = "税率")
    private BigDecimal taxRate;

    @Schema(description = "hs编码")
    private String hscode;

    @Schema(description = "物流属性")
    private Integer logisticAttribute;

    @Schema(description = "条形码")
    private String fbaBarCode;
}