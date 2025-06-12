package cn.iocoder.yudao.module.tms.controller.admin.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TmsProductRespVO {

    @Schema(description = "产品编号")
    private Long id;

    @Schema(description = "产品名称")
    private String name;

    @Schema(description = "产品sku")
    private String code;

    @Schema(description = "产品基础重量（kg）")
    private BigDecimal weight;
}
