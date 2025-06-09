package cn.iocoder.yudao.module.tms.controller.admin.logistic.customrule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TmsCustomRuleListRespVO {

    @Schema(description = "海关规则id")
    private Long id;

    @Schema(description = "国家编码")
    private Integer countryCode;

    @Schema(description = "国家描述")
    private String countryName;

    @Schema(description = "产品id")
    private Long productId;

    @Schema(description = "条形码")
    private String fbaBarCode;

}
