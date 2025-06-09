package cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.vo;

import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TmsFirstMileRequestAuditReqVO {

    @NotNull(message = "头程申请单ID不能为空")
    @Schema(description = "头程申请单ID")
    @DiffLogField(name = "头程申请单ID")
    private Long requestId;

    // 审核/反审核
    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核通过/审核撤销")
    @DiffLogField(name = "审核状态")
    private Boolean reviewed;
    //通过与否
    @NotNull(message = "审核通过与否不能为空")
    @Schema(description = "审核通过/不通过")
    @DiffLogField(name = "审核结果")
    private Boolean pass;

    //审核意见
    @Schema(description = "审核意见")
    @DiffLogField(name = "审核意见")
    private String auditAdvice;
}
