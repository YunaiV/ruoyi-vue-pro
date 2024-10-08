package cn.iocoder.yudao.module.erp.controller.admin.logistic.customrule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - ERP 海关规则新增/修改 Request VO")
@Data
public class ErpCustomRuleSaveReqVO {

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "国家编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "国家编码不能为空")
    private String countryCode;

    @Schema(description = "类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "报关")
    @NotEmpty(message = "类型不能为空")
    private String type;

    @Schema(description = "供应商产品编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "供应商产品编号不能为空")
    private Long supplierProductId;

    @Schema(description = "申报品名（英文）")
    private String declaredTypeEn;

    @Schema(description = "申报品名")
    private String declaredType;

    @Schema(description = "申报金额")
    private Double declaredValue;

    @Schema(description = "申报金额币种")
    private String declaredValueCurrencyCode;

    @Schema(description = "税率")
    private Integer taxRate;

    @Schema(description = "hs编码")
    private String hscode;

    @Schema(description = "物流属性")
    private String logisticAttribute;

}