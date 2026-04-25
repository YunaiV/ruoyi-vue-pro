package cn.iocoder.yudao.module.trade.controller.app.wholesale.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "用户 App - 批发订单处理 Request VO")
@Data
public class AppWholesaleOrderProcessReqVO {

    @Schema(description = "订单项列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "订单项不能为空")
    @Valid
    private List<Item> items;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "收件地址编号")
    private Long addressId;

    @Data
    public static class Item {

        @Schema(description = "SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "SKU 编号不能为空")
        private Long skuId;

        @Schema(description = "采购数量", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "采购数量不能为空")
        @Min(value = 1, message = "采购数量至少 1")
        private Integer count;

    }

}
