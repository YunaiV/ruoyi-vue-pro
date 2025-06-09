package cn.iocoder.yudao.module.tms.controller.admin.logistic.customrule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 根据国别和产品ID集合获得海关规则 Request VO")
@Data
public class TmsCustomRuleListReqVO {

    @Schema(description = "国别ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "CN")
    @NotNull(message = "国别ID不能为空")
    private Long countryCode;

    @Schema(description = "产品ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "产品ID集合不能为空")
    @Size(min = 1, message = "产品ID集合长度不能少于 1")
    private List<Long> productIds;

} 