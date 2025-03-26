package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP采购申请单采购 Request VO")
@Data
public class ErpPurchaseRequestOrderReqVO {

    @Schema(description = "供应商编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "供应商编号不能为空")
    private Long supplierId;

    @Schema(description = "期望采购时间")
    private LocalDateTime orderTime;
    // 项目列表
    @NotNull(message = "项目列表不能为空")
    @Schema(description = "项目列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<requestItems> items;

    @Schema(description = "订单币别名称")
    @NotBlank(message = "订单币别名称不能为空")
    private String currencyName;

    @Data
    public static class requestItems {
        // 项目ID
        @NotNull(message = "申请单项ID不能为空")
        @Schema(description = "申请单项ID", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long id;

//        @Schema(description = "供应商产品id")
//        @NotNull(message = "供应商产品id不能为空")
//        private Long supplierProductId;

        //下单数量
//        @NotNull(message = "下单数量不能为空")
        @Schema(description = "下单数量", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer orderQuantity;

    }
}
