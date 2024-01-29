package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.rule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 流程任务分配规则的更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmTaskAssignRuleUpdateReqVO extends BpmTaskAssignRuleBaseVO {

    @Schema(description = "任务分配规则的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "任务分配规则的编号不能为空")
    private Long id;

}
