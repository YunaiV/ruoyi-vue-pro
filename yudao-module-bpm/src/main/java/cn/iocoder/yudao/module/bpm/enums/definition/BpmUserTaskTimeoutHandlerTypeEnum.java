package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 用户任务超时处理类型枚举
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum BpmUserTaskTimeoutHandlerTypeEnum implements ArrayValuable<Integer> {

    /**
     * 自动提醒：给未处理的审批人发送超时提醒，可配置最大提醒次数
     */
    REMINDER(1,"自动提醒"),
    /**
     * 自动同意：未处理的任务，由系统自动审批通过
     */
    APPROVE(2, "自动同意"),
    /**
     * 自动拒绝：未处理的任务，由系统自动审批不通过
     */
    REJECT(3, "自动拒绝"),
    /**
     * 自动跳转：跳转到配置的审批节点，目前仅 SIMPLE 设计器支持
     */
    AUTO_JUMP(4, "自动跳转");

    private final Integer type;
    private final String name;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BpmUserTaskTimeoutHandlerTypeEnum::getType).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
