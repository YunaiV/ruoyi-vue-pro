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
    AFTER("after", "向后加签");

    private final String type;

    private final String desc; // TODO 芋艿：desc

    public static String formatDesc(String type) {
        for (BpmTaskAddSignTypeEnum value : values()) {
            if (value.type.equals(type)) {
                return value.desc;
            }
        }
        return null;
    }

}
