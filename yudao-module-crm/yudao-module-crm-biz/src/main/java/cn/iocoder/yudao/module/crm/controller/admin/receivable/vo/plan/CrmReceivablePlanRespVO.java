package cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - CRM 回款计划 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmReceivablePlanRespVO extends CrmReceivablePlanBaseVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "25153")
    private Long id;

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

    @Schema(description = "回款方式", example = "1") // 来自 Receivable 的 returnType 字段
    private Integer returnType;

}
