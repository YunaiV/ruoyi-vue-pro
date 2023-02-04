package cn.iocoder.yudao.module.system.controller.admin.notify.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;

@Schema(description = "管理后台 - 站内信模版 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NotifyTemplateRespVO extends NotifyTemplateBaseVO {

    @Schema(description = "ID", required = true, example = "1024")
    private Long id;

    @Schema(description = "参数数组", example = "name,code")
    private List<String> params;

    @Schema(description = "创建时间", required = true)
    private Date createTime;

}
