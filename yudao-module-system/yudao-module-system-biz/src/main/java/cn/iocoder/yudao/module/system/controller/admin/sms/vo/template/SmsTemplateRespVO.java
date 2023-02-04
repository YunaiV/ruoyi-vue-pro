package cn.iocoder.yudao.module.system.controller.admin.sms.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 短信模板 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SmsTemplateRespVO extends SmsTemplateBaseVO {

    @Schema(description = "编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "短信渠道编码", required = true, example = "ALIYUN")
    private String channelCode;

    @Schema(description = "参数数组", example = "name,code")
    private List<String> params;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}
