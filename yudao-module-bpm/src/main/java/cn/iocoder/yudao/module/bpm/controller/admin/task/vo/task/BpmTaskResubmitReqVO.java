package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "管理后台 - 退回修改后重新提交请求")
public class BpmTaskResubmitReqVO {

    @NotEmpty(message = "任务编号不能为空")
    private String taskId;

    @Schema(description = "流程表单字段及修改值")
    private Map<String, Object> variables;
}
