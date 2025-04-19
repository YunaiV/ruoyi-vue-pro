package cn.iocoder.yudao.module.oms.controller.admin.shop.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - OMS 平台店铺新增/修改 Request VO")
@Data
public class OmsShopSaveReqVO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "店铺名称")
    private String name;

    @Schema(description = "店铺编码")
    private String code;

    @Schema(description = "平台")
    private String platformCode;

    @Schema(description = "类型")
    private Integer type;

}
