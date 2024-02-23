package cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - CRM 回款计划 Response VO")
@Data
public class CrmReceivablePlanRespVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "25153")
    private Long id;

    @Schema(description = "期数", example = "1")
    private Integer period;

    @Schema(description = "回款计划编号", example = "19852")
    private Long receivableId;

    @Schema(description = "计划回款金额", example = "29675")
    private Integer price;

    @Schema(description = "计划回款日期")
    private LocalDateTime returnTime;

    @Schema(description = "提前几天提醒")
    private Integer remindDays;

    @Schema(description = "提醒日期")
    private LocalDateTime remindTime;

    @Schema(description = "客户名称", example = "18026")
    private Long customerId;

    @Schema(description = "合同编号", example = "3473")
    private Long contractId;

    @Schema(description = "负责人编号", example = "17828")
    private Long ownerUserId;

    @Schema(description = "显示顺序")
    private Integer sort;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "客户名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "test")
    private String customerName;

    @Schema(description = "合同编号", example = "Q110")
    private String contractNo;

    @Schema(description = "负责人", example = "test")
    private String ownerUserName;

    @Schema(description = "创建人", example = "25682")
    private String creator;

    @Schema(description = "创建人名字", example = "test")
    private String creatorName;

    @Schema(description = "完成状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Boolean finishStatus;

    @Schema(description = "回款方式", example = "1")
    private Integer returnType; // 来自 Receivable 的 returnType 字段

}
