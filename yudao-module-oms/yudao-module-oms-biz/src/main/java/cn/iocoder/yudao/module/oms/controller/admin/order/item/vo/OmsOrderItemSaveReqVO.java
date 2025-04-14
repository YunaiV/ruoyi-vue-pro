package cn.iocoder.yudao.module.oms.controller.admin.order.item.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - OMS订单项新增/修改 Request VO")
@Data
public class OmsOrderItemSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "29025")
    private Long id;

    @Schema(description = "销售订单id", requiredMode = Schema.RequiredMode.REQUIRED, example = "30317")
    @NotNull(message = "销售订单id不能为空")
    private Long orderId;

    @Schema(description = "店铺产品编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "店铺产品编码不能为空")
    private String shopProductCode;

    @Schema(description = "店铺产品数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "店铺产品数量不能为空")
    private Integer qty;

    @Schema(description = "单价", requiredMode = Schema.RequiredMode.REQUIRED, example = "1098")
    @NotNull(message = "单价不能为空")
    private BigDecimal price;

}