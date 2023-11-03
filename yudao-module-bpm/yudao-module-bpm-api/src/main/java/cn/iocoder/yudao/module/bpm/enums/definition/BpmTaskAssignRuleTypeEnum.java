package cn.iocoder.yudao.module.bpm.enums.definition;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * BPM 任务分配规则的类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum BpmTaskAssignRuleTypeEnum {

    ROLE(10, "角色"),
    DEPT_MEMBER(20, "部门的成员"), // 包括负责人
    DEPT_LEADER(21, "部门的负责人"),
    POST(22, "岗位"),
    USER(30, "用户"),
    USER_GROUP(40, "用户组"),
    SCRIPT(50, "自定义脚本"), // 例如说，发起人所在部门的领导、发起人所在部门的领导的领导
    ;

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 描述
     */
    private final String desc;

}
