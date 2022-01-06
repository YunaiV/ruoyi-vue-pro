package cn.iocoder.yudao.adminserver.modules.bpm.enums.task;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 流程实例的结果
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum ProcessInstanceStatusEnum {

    RUNNING(1, "进行中"),
    FINISH(2, "已完成");

    /**
     * 状态
     */
    private final Integer result;
    /**
     * 描述
     */
    private final String desc;

}
