package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 流程实例的创建 Request VO")
@Data
public class BpmProcessInstanceCreateReqVO {

    @Schema(description = "流程定义的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotEmpty(message = "流程定义编号不能为空")
    private String processDefinitionId;

    @Schema(description = "变量实例")
    private Map<String, Object> variables;

    // TODO @hai：assignees 复数
    @Schema(description = "提前指派的审批人", requiredMode = Schema.RequiredMode.REQUIRED, example = "{taskKey1: [1, 2]}")
    private Map<String, List<Long>> assignee;

}
