package cn.iocoder.yudao.module.system.controller.admin.mail.vo.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 邮件日志 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MailLogRespVO extends MailLogBaseVO {

    @Schema(description = "编号", required = true, example = "31020")
    private Long id;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}
