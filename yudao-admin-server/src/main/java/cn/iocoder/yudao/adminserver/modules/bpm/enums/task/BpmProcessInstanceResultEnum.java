package cn.iocoder.yudao.adminserver.modules.bpm.enums.task;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 流程实例的结果
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum BpmProcessInstanceResultEnum {

    PROCESS(1, "处理中"),
    APPROVE(2, "通过"),
    REJECT(3, "不通过"),
    CANCEL(4, "已取消");

    /**
     * 结果
     */
    private final Integer result;
    /**
     * 描述
     */
    private final String desc;

}
