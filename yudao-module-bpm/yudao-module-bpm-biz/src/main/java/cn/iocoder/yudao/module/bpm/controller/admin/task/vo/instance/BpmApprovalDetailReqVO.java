package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

// TODO @jason：这个可以简化下，使用 @RequestParam。嘿嘿，主要 VO 项不要太多
@Schema(description = "管理后台 - 审批详情 Request VO")
@Data
public class BpmApprovalDetailReqVO {

    @Schema(description = "流程定义的编号", example = "1024")
    private String processDefinitionId;

    @Schema(description = "流程实例的编号", example = "1024")
    private String processInstanceId;

    @AssertTrue(message = "流程定义的编号和流程实例的编号不能同时为空")
    @JsonIgnore
    public boolean isValidProcessParam() {
        return StrUtil.isNotEmpty(processDefinitionId) || StrUtil.isNotEmpty(processInstanceId);
    }

}
