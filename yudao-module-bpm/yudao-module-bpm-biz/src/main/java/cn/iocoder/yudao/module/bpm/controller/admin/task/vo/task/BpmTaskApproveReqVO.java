package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 通过流程任务的 Request VO")
@Data
public class BpmTaskApproveReqVO {

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotEmpty(message = "任务编号不能为空")
    private String id;

    @Schema(description = "审批意见", example = "不错不错！")
    private String reason;

    @Schema(description = "签名", example = "https://www.iocoder.cn/sign.png")
    private String signPicUrl;

    @Schema(description = "变量实例（动态表单）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, Object> variables;

    @Schema(description = "下一个节点审批人", example = "{nodeId:[1, 2]}")
    private Map<String, List<Long>> nextAssignees; // 为什么是 Map，而不是 List 呢？因为下一个节点可能是多个，例如说并行网关的情况

}
