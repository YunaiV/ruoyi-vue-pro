package cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.req;

import cn.iocoder.yudao.module.system.api.utils.Validation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SrmPurchaseInAuditReqVO {
    @NotNull(groups = {Validation.OnSubmitAudit.class, Validation.OnAudit.class}, message = "入库单ID不能为空")
//    @Size(min = 1, groups = Validation.OnAudit.class, message = "入库单ID审核时候只能传一个")
    @Schema(description = "入库单ID集合")
    private Long inId;

    // 审核/反审核
    @NotNull(groups = Validation.OnAudit.class, message = "审核状态不能为空")
    @Schema(description = "审核通过/审核撤销")
    private Boolean reviewed;
    //通过与否
    @NotNull(groups = Validation.OnAudit.class, message = "审核通过与否不能为空")
    @Schema(description = "审核通过/不通过")
    private Boolean pass;

    //审核意见
    @Schema(description = "审核意见")
    private String reviewComment;

}
