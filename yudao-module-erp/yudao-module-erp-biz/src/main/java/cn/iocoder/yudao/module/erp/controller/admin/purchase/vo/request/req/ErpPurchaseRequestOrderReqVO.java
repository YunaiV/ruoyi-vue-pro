package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - ERP采购申请单采购 Request VO")
@Data
public class ErpPurchaseRequestOrderReqVO {
    // 项目列表
    @NotNull(message = "项目列表不能为空")
    @Schema(description = "项目列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<requestItems> items;

    @Data
    public static class requestItems {
        // 项目ID
        @NotNull(message = "申请单项ID不能为空")
        @Schema(description = "申请单项ID", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long id;

        @NotNull(message = "下单数量不能为空")
        @Schema(description = "下单数量", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer orderCount;
    }
}
