package cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - CRM 回款计划新增/修改 Request VO")
@Data
public class CrmReceivablePlanSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Long id;

    @Schema(description = "期数", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "期数不能为空")
    private Integer period;

    @Schema(description = "计划回款金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "9000")
    @NotNull(message = "计划回款金额不能为空")
    private BigDecimal price;

    @Schema(description = "计划回款日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-02-02")
    @NotNull(message = "计划回款日期不能为空")
    private LocalDateTime returnTime;

    @Schema(description = "提前几天提醒", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "提前几天提醒不能为空")
    private Integer remindDays;

    @Schema(description = "提醒日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-02-02")
    @NotNull(message = "提醒日期不能为空")
    private LocalDateTime remindTime;

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "客户编号不能为空")
    private Long customerId;

    @Schema(description = "合同编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "合同编号不能为空")
    private Long contractId;

    @Schema(description = "负责人编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "负责人编号不能为空")
    private Long ownerUserId;

    @Schema(description = "显示顺序")
    private Integer sort;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
