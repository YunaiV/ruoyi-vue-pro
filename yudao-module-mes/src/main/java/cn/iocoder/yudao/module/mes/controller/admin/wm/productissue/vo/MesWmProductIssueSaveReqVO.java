package cn.iocoder.yudao.module.mes.controller.admin.wm.productissue.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 领料出库单新增/修改 Request VO")
@Data
public class MesWmProductIssueSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "领料单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "ISSUE20250226001")
    @NotBlank(message = "领料单编号不能为空")
    private String code;

    @Schema(description = "领料单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "生产领料")
    @NotBlank(message = "领料单名称不能为空")
    private String name;

    @Schema(description = "生产工单 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "生产工单 ID 不能为空")
    private Long workOrderId;

    @Schema(description = "工作站 ID", example = "1")
    private Long workstationId;

    @Schema(description = "需求时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "需求时间不能为空")
    private LocalDateTime requiredTime;

    @Schema(description = "备注", example = "备注")
    private String remark;

    }
