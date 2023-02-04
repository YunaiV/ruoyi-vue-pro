package cn.iocoder.yudao.module.infra.controller.admin.job.vo.job;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 定时任务分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class JobPageReqVO extends PageParam {

    @Schema(description = "任务名称,模糊匹配", example = "测试任务")
    private String name;

    @Schema(description = "任务状态,参见 JobStatusEnum 枚举", example = "1")
    private Integer status;

    @Schema(description = "处理器的名字,模糊匹配", example = "sysUserSessionTimeoutJob")
    private String handlerName;

}
