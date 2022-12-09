package cn.iocoder.yudao.module.infra.controller.admin.job.vo.job;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(title = "管理后台 - 定时任务 Excel 导出 Request VO", description = "参数和 JobPageReqVO 是一致的")
@Data
public class JobExportReqVO {

    @Schema(title = "任务名称", example = "测试任务", description = "模糊匹配")
    private String name;

    @Schema(title = "任务状态", example = "1", description = "参见 JobStatusEnum 枚举")
    private Integer status;

    @Schema(title = "处理器的名字", example = "UserSessionTimeoutJob", description = "模糊匹配")
    private String handlerName;

}
