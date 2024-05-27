package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.hutool.core.util.ArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

// TODO @芋艿：审批方式的名字，可能要看下；
/**
 * BPM 审批方式的枚举
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum BpmApproveMethodEnum {

    SINGLE_PERSON_APPROVE(1, "单人审批"),
    ALL_APPROVE(2, "多人会签(需所有审批人同意)"), // 会签
    ANY_OF_APPROVE(3, "多人或签(一名审批人同意即可)"), // 或签
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
