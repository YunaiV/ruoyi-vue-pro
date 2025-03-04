package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

//审核状态请求类
@Data
@Builder
public class ErpPurchaseRequestAuditStatusReqVO {
    // 请求ID
    @NotNull(message = "申请单ID不能为空")
    @Schema(description = "申请单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long requestId;

    // 项目列表
    @NotNull(message = "项目列表不能为空")
    @Schema(description = "项目列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<requestItems> items;

    // 审核状态
    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean reviewed;

    //审核意见
//    @NotNull(message = "审核意见不能为空")
    @Schema(description = "审核意见")
    private String reviewComment;

    // 内部静态类，用于表示项目
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
