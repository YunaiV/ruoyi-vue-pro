package cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.receivable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

// TODO 芋艿：导出的 VO，可以考虑使用 @Excel 注解，实现导出功能
@Schema(description = "管理后台 - CRM 回款 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmReceivableRespVO extends CrmReceivableBaseVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "25787")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "客户名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "test")
    private String customerName;

    @Schema(description = "审批状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer auditStatus;

    @Schema(description = "合同编号", example = "Q110")
    private String contractNo;

    @Schema(description = "负责人", example = "test")
    private String ownerUserName;

    @Schema(description = "创建人", example = "25682")
    private String creator;

    @Schema(description = "创建人名字", example = "test")
    private String creatorName;

}
