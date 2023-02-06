package cn.iocoder.yudao.module.system.controller.admin.mail.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 邮件末班 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MailTemplateRespVO extends MailTemplateBaseVO {

    @Schema(description = "编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "参数数组", example = "name,code")
    private List<String> params;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}
