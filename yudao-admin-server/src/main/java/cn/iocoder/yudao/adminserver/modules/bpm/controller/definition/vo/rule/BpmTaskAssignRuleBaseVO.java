package cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.rule;

import lombok.Data;

import java.util.Set;

@Data
public class BpmTaskAssignRuleBaseVO {

    private Integer type;

    private Set<Long> options;

}
