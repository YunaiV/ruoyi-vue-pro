package cn.iocoder.yudao.adminserver.modules.bpm.enums.task;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 流程实例的删除原因
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum BpmProcessInstanceDeleteReasonEnum {

    REJECT_TASK("驳回任务");

    private final String reason;

}
