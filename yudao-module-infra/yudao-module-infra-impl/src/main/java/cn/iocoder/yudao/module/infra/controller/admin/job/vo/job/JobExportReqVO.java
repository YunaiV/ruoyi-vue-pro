package cn.iocoder.yudao.module.infra.controller.admin.job.vo.job;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "管理后台 - 定时任务 Excel 导出 Request VO", description = "参数和 JobPageReqVO 是一致的")
@Data
public class JobExportReqVO {

    @ApiModelProperty(value = "任务名称", example = "测试任务", notes = "模糊匹配")
    private String name;

    @ApiModelProperty(value = "任务状态", example = "1", notes = "参见 JobStatusEnum 枚举")
    private Integer status;

    @ApiModelProperty(value = "处理器的名字", example = "UserSessionTimeoutJob", notes = "模糊匹配")
    private String handlerName;

}
