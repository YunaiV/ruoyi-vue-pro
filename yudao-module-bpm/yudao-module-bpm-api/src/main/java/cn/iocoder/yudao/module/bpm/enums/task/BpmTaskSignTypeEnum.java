package cn.iocoder.yudao.module.bpm.enums.task;

import cn.hutool.core.util.ArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 流程任务的加签类型枚举
 *
 * @author kehaiyou
 */
@Getter
@AllArgsConstructor
public enum BpmTaskSignTypeEnum {

    /**
     * 向前加签，需要前置任务审批完成，才回到原审批人
     */
    BEFORE("before", "向前加签"),
    /**
     * 向后加签，需要后置任务全部审批完，才会通过原审批人节点
     */
    AFTER("after", "向后加签");

    /**
     * 类型
     */
    private final String type;
    /**
     * 名字
     */
    private final String name;

    public static String nameOfType(String type) {
        for (BpmTaskSignTypeEnum value : values()) {
            if (value.type.equals(type)) {
                return value.name;
            }
        }
        return null;
    }

    public static BpmTaskSignTypeEnum of(String type) {
        return ArrayUtil.firstMatch(value -> value.getType().equals(type), values());
    }

}
