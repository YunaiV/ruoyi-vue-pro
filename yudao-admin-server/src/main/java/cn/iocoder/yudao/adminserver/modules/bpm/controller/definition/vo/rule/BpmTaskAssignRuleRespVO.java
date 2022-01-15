package cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.rule;

import lombok.Data;

@Data
public class BpmTaskAssignRuleRespVO extends BpmTaskAssignRuleBaseVO {

    private Long id;

    private String modelId;

    private String processDefinitionId;

    private String taskDefinitionKey;
    private String taskDefinitionName;

}
