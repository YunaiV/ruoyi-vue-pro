package cn.iocoder.yudao.module.bpm.enums.task;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 附件类型枚举
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum BpmAttachmentTypeEnum {

    TASK_ATTACHMENT("1", "用户任务附件");

    /**
     * 操作类型
     * <p>
     * 由于 BPM attachment 类型为 String，所以这里就不使用 Integer
     */
    private final String type;
    /**
     * 操作名字
     */
    private final String name;
}
