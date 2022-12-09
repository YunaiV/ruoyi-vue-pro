package cn.iocoder.yudao.module.infra.controller.admin.job.vo.job;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(title = "管理后台 - 定时任务分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class JobPageReqVO extends PageParam {

    @Schema(title = "任务名称", example = "测试任务", description = "模糊匹配")
    private String name;

    @Schema(title = "任务状态", example = "1", description = "参见 JobStatusEnum 枚举")
    private Integer status;

    @Schema(title = "处理器的名字", example = "sysUserSessionTimeoutJob", description = "模糊匹配")
    private String handlerName;

}
