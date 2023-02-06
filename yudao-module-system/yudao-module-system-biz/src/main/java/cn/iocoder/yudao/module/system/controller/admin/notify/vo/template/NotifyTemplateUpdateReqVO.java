package cn.iocoder.yudao.module.system.controller.admin.notify.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 站内信模版更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NotifyTemplateUpdateReqVO extends NotifyTemplateBaseVO {

    @Schema(description = "ID", required = true, example = "1024")
    @NotNull(message = "ID 不能为空")
    private Long id;

}
