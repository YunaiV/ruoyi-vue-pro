package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order;

import cn.iocoder.yudao.module.erp.controller.admin.tools.validation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ErpPurchaseOrderAuditReqVO {

    // 采购单列表
    @NotNull(message = "采购单列表不能为空")
    @Schema(description = "采购单列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<@Valid requestItems> items;

    // 审核/反审核
    @NotNull(groups = validation.OnAudit.class,message = "审核状态不能为空")
    @Schema(description = "审核/反审核", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean reviewed;
    //通过与否
    @NotNull(groups = validation.OnAudit.class,message = "审核通过与否不能为空")
    @Schema(description = "审核通过/不通过", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean pass;

    //审核意见
    @Schema(description = "审核意见")
    private String reviewComment;

    //开关
    @Schema(description = "开关")
    @NotNull(groups = validation.OnSwitch.class,message = "开关不能为空")
    private Boolean open;

    @Data
    public static class requestItems {
        // 采购单ID
        @NotNull(message = "采购项ID不能为空")
        @Schema(description = "采购项ID", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long id;
        //批准数量
        @NotNull(groups = validation.OnAudit.class,message = "批准数量不能为空")
        @Schema(description = "批准数量", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer approveCount;
    }
}
