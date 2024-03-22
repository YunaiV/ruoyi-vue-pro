package cn.iocoder.yudao.module.bpm.framework.flowable.core.enums;

import cn.hutool.core.util.ArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * BPM 任务的候选人策略枚举
 *
 * 例如说：分配给指定人审批
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum BpmTaskCandidateStrategyEnum {

    ROLE(10, "角色"),
    DEPT_MEMBER(20, "部门的成员"), // 包括负责人
    DEPT_LEADER(21, "部门的负责人"),
    POST(22, "岗位"),
    USER(30, "用户"),
    START_USER_SELECT(35, "发起人自选"), // 申请人自己，可在提交申请时选择此节点的审批人
    USER_GROUP(40, "用户组"),
    EXPRESSION(60, "流程表达式"), // 表达式 ExpressionManager
    ;

    /**
     * 类型
     */
    private final Integer strategy;
    /**
     * 描述
     */
    private final String description;

    public static BpmTaskCandidateStrategyEnum valueOf(Integer strategy) {
        return ArrayUtil.firstMatch(o -> o.getStrategy().equals(strategy), values());
    }

}
