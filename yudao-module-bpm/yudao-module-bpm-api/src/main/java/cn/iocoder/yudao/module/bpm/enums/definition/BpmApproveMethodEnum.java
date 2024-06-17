package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.hutool.core.util.ArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * BPM 多人审批方式的枚举
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum BpmApproveMethodEnum {

    RANDOM_SELECT_ONE_APPROVE(1, "随机挑选一人审批"),
    APPROVE_BY_RATIO(2, "多人会签(按通过比例)"), // 会签（按通过比例）
    ANY_APPROVE(3, "多人或签(一人通过或拒绝)"), // 或签（通过只需一人，拒绝只需一人）
    SEQUENTIAL_APPROVE(4, "依次审批"); // 依次审批

    /**
     * 审批方式
     */
    private final Integer method;
    /**
     * 名字
     */
    private final String name;

    public static BpmApproveMethodEnum valueOf(Integer method) {
        return ArrayUtil.firstMatch(item -> item.getMethod().equals(method), values());
    }

}
