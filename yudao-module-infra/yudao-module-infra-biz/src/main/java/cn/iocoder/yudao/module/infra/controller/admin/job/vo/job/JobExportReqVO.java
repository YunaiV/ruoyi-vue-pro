package cn.iocoder.yudao.module.infra.controller.admin.job.vo.job;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 定时任务 Excel 导出 Request VO-参数和 JobPageReqVO 是一致的")
@Data
public class JobExportReqVO {

    @Schema(description = "任务名称-模糊匹配", example = "测试任务")
    private String name;

    @Schema(description = "任务状态-参见 JobStatusEnum 枚举", example = "1")
    private Integer status;

    @Schema(description = "处理器的名字-模糊匹配", example = "UserSessionTimeoutJob")
    private String handlerName;

}
