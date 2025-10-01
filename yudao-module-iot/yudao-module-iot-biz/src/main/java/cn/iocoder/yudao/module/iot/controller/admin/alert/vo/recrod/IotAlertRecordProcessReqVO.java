package cn.iocoder.yudao.module.iot.controller.admin.alert.vo.recrod;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - IoT 告警记录处理 Request VO")
@Data
public class IotAlertRecordProcessReqVO {

    @Schema(description = "记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "记录编号不能为空")
    private Long id;

    @Schema(description = "处理结果（备注）", requiredMode = Schema.RequiredMode.REQUIRED, example = "已处理告警，问题已解决")
    private String processRemark;

} 