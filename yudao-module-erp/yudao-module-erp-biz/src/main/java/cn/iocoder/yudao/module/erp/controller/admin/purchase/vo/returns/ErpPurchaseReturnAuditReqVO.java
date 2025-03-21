package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.returns;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ErpPurchaseReturnAuditReqVO {
    @NotNull(groups = {validation.OnSubmitAudit.class, validation.OnAudit.class}, message = "退货单ids不能为空")
    @Size(min = 1, groups = validation.OnAudit.class, message = "退货单id审核时候只能传一个")
    @Schema(description = "退货单")
    private List<Long> ids;

    // 审核/反审核
    @NotNull(groups = validation.OnAudit.class, message = "审核状态不能为空")
    @Schema(description = "审核/反审核")
    private Boolean reviewed;
    //通过与否
    @NotNull(groups = validation.OnAudit.class, message = "审核通过与否不能为空")
    @Schema(description = "审核通过/不通过")
    private Boolean pass;

    //审核意见
    @Schema(description = "审核意见")
    private String reviewComment;

    //退款完成|退款撤销 布尔值
    @NotNull(message = "退款完成|退款撤销 布尔值不能为空")
    @Schema(description = "退款完成|退款撤销 布尔值")
    private Boolean refund;
}
