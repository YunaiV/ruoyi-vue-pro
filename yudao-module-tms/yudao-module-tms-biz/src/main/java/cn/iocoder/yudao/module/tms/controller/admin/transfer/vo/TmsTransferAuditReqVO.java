package cn.iocoder.yudao.module.tms.controller.admin.transfer.vo;

import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TmsTransferAuditReqVO {
    @NotNull(message = "调拨ID不能为空")
    @Schema(description = "调拨主表ID")
    @DiffLogField(name = "调拨ID")
    private Long id;

    // 审核/反审核
    @Schema(description = "审核通过/审核撤销")
    @DiffLogField(name = "审核状态")
    @NotNull(message = "审核状态不能为空")
    private Boolean reviewed;
    //通过与否
    @Schema(description = "审核通过/不通过")
    @DiffLogField(name = "审核结果")
    @NotNull(message = "审核通过与否不能为空")
    private Boolean pass;

    //审核意见
    @Schema(description = "审核意见")
    @DiffLogField(name = "审核意见")
    private String auditAdvice;
}
