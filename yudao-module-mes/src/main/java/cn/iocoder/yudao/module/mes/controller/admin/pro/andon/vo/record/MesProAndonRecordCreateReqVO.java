package cn.iocoder.yudao.module.mes.controller.admin.pro.andon.vo.record;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - MES 安灯呼叫记录新增 Request VO")
@Data
public class MesProAndonRecordCreateReqVO {

    @Schema(description = "安灯配置编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "安灯配置不能为空")
    private Long configId;

    @Schema(description = "工作站编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "工作站不能为空")
    private Long workstationId;

    @Schema(description = "发起用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "发起用户不能为空")
    private Long userId;

    @Schema(description = "生产工单编号", example = "200")
    private Long workOrderId;

    @Schema(description = "工序编号", example = "300")
    private Long processId;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
