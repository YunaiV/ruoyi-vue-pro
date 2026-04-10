package cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.machine;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - MES 设备资源新增 Request VO")
@Data
public class MesMdWorkstationMachineSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "工作站编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "工作站不能为空")
    private Long workstationId;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "设备不能为空")
    private Long machineryId;

    @Schema(description = "数量", example = "1")
    private Integer quantity;

    @Schema(description = "备注")
    private String remark;

}
