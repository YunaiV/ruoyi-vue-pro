package cn.iocoder.yudao.module.system.controller.admin.sms.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 短信模板的精简 Response VO")
@Data
public class SmsTemplateSimpleRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "验证码")
    private String name;

    @Schema(description = "模板编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "sms_001")
    private String code;

}
