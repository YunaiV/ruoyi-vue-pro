package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Set;

// TODO @海洋：类名，应该是 create 哈
@Schema(description = "管理后台 - 加签流程任务的 Request VO")
@Data
public class BpmTaskAddSignReqVO {

    @Schema(description = "需要加签的任务 ID")
    @NotEmpty(message = "任务编号不能为空")
    private String id;

    @Schema(description = "加签的用户 ID")
    @NotEmpty(message = "加签用户 ID 不能为空")
    private Set<Long> userIdList;

    @Schema(description = "加签类型，before 向前加签，after 向后加签")
    @NotEmpty(message = "加签类型不能为空")
    private String type;

    @Schema(description = "加签原因")
    @NotEmpty(message = "加签原因不能为空")
    private String reason;

}
