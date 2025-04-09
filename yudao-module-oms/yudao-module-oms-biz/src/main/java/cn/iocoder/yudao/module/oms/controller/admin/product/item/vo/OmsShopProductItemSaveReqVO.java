package cn.iocoder.yudao.module.oms.controller.admin.product.item.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - OMS 店铺产品项新增/修改 Request VO")
@Data
public class OmsShopProductItemSaveReqVO {

    @Schema(description = "店铺产品项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15443")
    private Long id;

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "27593")
    @NotNull(message = "产品编号不能为空")
    private Long productId;

    @Schema(description = "店铺产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "27593")
    @NotNull(message = "店铺产品编号不能为空")
    private Long shopProductId;

}