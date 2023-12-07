package cn.iocoder.yudao.module.trade.controller.admin.brokerage.vo.withdraw;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 驳回申请 Request VO")
@Data
@ToString(callSuper = true)
public class BrokerageWithdrawRejectReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "7161")
    @NotNull(message = "编号不能为空")
    private Integer id;

    @Schema(description = "审核驳回原因", example = "不对")
    @NotEmpty(message = "审核驳回原因不能为空")
    private String auditReason;

}
