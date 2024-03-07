package cn.iocoder.yudao.module.bpm.enums.task;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 流程任务 -- 加签类型枚举类型
 */
@Getter
@AllArgsConstructor
public enum BpmTaskAddSignTypeEnum {

    /**
     * 向前加签，需要前置任务审批完成，才回到原审批人
     */
    BEFORE("before", "向前加签"),
    /**
     * 向后加签，需要后置任务全部审批完，才会通过原审批人节点
     */
    AFTER("after", "向后加签"),
    /**
     * 创建后置加签时的过度状态，用于控制向后加签生成的任务状态
     */
    AFTER_CHILDREN_TASK("afterChildrenTask", "向后加签生成的子任务");

    private final String type;

    private final String desc;

    public static String formatDesc(String type) {
        for (BpmTaskAddSignTypeEnum value : values()) {
            if (value.type.equals(type)) {
                return value.desc;
            }
        }
        return null;
    }

}
    