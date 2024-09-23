package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 审批详情 Request VO")
@Data
public class BpmApprovalDetailReqVO {

    @Schema(description = "流程定义的编号", example = "1024")
    private String processDefinitionId;

    @Schema(description = "流程实例的编号", example = "1024")
    private String processInstanceId;
}
