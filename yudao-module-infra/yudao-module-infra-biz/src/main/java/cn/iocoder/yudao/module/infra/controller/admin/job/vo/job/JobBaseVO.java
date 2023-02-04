package cn.iocoder.yudao.module.infra.controller.admin.job.vo.job;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
* 定时任务 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class JobBaseVO {

    @Schema(description = "任务名称", required = true, example = "测试任务")
    @NotNull(message = "任务名称不能为空")
    private String name;

    @Schema(description = "处理器的参数", example = "yudao")
    private String handlerParam;

    @Schema(description = "CRON 表达式", required = true, example = "0/10 * * * * ? *")
    @NotNull(message = "CRON 表达式不能为空")
    private String cronExpression;

    @Schema(description = "重试次数", required = true, example = "3")
    @NotNull(message = "重试次数不能为空")
    private Integer retryCount;

    @Schema(description = "重试间隔", required = true, example = "1000")
    @NotNull(message = "重试间隔不能为空")
    private Integer retryInterval;

    @Schema(description = "监控超时时间", example = "1000")
    private Integer monitorTimeout;

}
