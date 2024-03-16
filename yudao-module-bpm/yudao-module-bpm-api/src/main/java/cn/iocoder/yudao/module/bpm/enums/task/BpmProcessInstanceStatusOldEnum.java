package cn.iocoder.yudao.module.bpm.enums.task;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 流程实例的状态
 *
 * @author 芋道源码
 */
@Deprecated
@Getter
@AllArgsConstructor
public enum BpmProcessInstanceStatusOldEnum {

    RUNNING(1, "进行中"),
    FINISH(2, "已完成");

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 描述
     */
    private final String desc;

}
