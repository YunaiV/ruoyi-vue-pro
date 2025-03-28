package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.request.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

//审核状态请求类
@Schema(description = "管理后台 - ERP采购申请单审核状态 Request VO")
@Data
@Builder
public class SrmPurchaseRequestAuditReqVO {
    // 请求ID
    @NotNull(message = "申请单ID不能为空")
    @Schema(description = "申请单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long requestId;

    // 项目列表
    @NotNull(message = "项目列表不能为空")
    @Schema(description = "项目列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<requestItems> items;

    // 审核/反审核
    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核/反审核", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean reviewed;
    //通过与否
    @NotNull(message = "通过与否不能为空")
    @Schema(description = "通过/不通过", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean pass;

    //审核意见
    @Schema(description = "审核意见")
    private String reviewComment;

    @Data
    public static class requestItems {
        // 项目ID
        @NotNull(message = "项目ID不能为空")
        @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long id;
        //批准数量
        @NotNull(message = "批准数量不能为空")
        @Schema(description = "批准数量", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer approveCount;
    }
}
