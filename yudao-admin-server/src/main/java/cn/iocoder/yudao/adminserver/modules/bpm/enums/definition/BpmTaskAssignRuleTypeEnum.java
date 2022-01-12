package cn.iocoder.yudao.adminserver.modules.bpm.enums.definition;

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

    ROLE(10, "指定角色"),

    DEPT(20, "指定部门"),
    DEPT_LEADER(21, "指定部门的负责人"),

    USER(30, "指定用户"),

    USER_GROUP(40, "指定用户组"), // TODO 芋艿：预留，暂未实现

    SCRIPT(50, "指定脚本"), // 例如说，发起人所在部门的领导、发起人所在部门的领导的领导
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
