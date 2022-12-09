package cn.iocoder.yudao.module.infra.controller.admin.job.vo.job;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(title = "管理后台 - 定时任务 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class JobRespVO extends JobBaseVO {

    @Schema(title = "任务编号", required = true, example = "1024")
    private Long id;

    @Schema(title = "任务状态", required = true, example = "1")
    private Integer status;

    @Schema(title = "处理器的名字", required = true, example = "sysUserSessionTimeoutJob")
    @NotNull(message = "处理器的名字不能为空")
    private String handlerName;

    @Schema(title = "创建时间", required = true)
    private LocalDateTime createTime;

}
