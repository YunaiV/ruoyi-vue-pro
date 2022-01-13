package cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.rule;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BpmTaskAssignRuleCreateReqVO extends BpmTaskAssignRuleBaseVO {

    private String modelId;

    private String taskDefinitionKey;

}
