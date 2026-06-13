package cn.iocoder.yudao.module.system.controller.admin.notify.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 站内信模版的精简 Response VO")
@Data
public class NotifyTemplateSimpleRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "模版名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "系统通知")
    private String name;

    @Schema(description = "模版编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "notify_001")
    private String code;

}
