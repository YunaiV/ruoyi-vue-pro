package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * BPM 多人审批方式的枚举
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum BpmUserTaskApproveMethodEnum implements IntArrayValuable {

    RANDOM(1, "随机挑选一人审批", null),
    RATIO(2, "多人会签(按通过比例)", "${ nrOfCompletedInstances/nrOfInstances >= %s}"), // 会签（按通过比例）
    ANY(3, "多人或签(一人通过或拒绝)", "${ nrOfCompletedInstances > 0 }"), // 或签（通过只需一人，拒绝只需一人）
    SEQUENTIAL(4, "依次审批", "${ nrOfCompletedInstances >= nrOfInstances }"); // 依次审批

    /**
     * 审批方式
     */
    private final Integer method;
    /**
     * 名字
     */
    private final String name;
    /**
     * 完成表达式
     */
    private final String completionCondition;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(BpmUserTaskApproveMethodEnum::getMethod).toArray();

    public static BpmUserTaskApproveMethodEnum valueOf(Integer method) {
        return ArrayUtil.firstMatch(item -> item.getMethod().equals(method), values());
    }

    @Override
    public int[] array() {
        return ARRAYS;
    }
}
