package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * BPM 用户任务拒绝处理类型枚举
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum BpmUserTaskRejectHandlerType implements IntArrayValuable {

    FINISH_PROCESS_INSTANCE(1, "终止流程"),
    RETURN_USER_TASK(2, "驳回到指定任务节点");

    private final Integer type;
    private final String name;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(BpmUserTaskRejectHandlerType::getType).toArray();

    public static BpmUserTaskRejectHandlerType typeOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

    @Override
    public int[] array() {
        return ARRAYS;
    }
}
