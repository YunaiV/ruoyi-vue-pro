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
    APPROVE_BY_RATIO(3, "多人会签(按比例投票)"), // 会签（按比例投票）
    ANY_APPROVE_ALL_REJECT(4, "多人会签(通过只需一人,拒绝需要全员)"), // 会签（通过只需一人，拒绝需要全员）
    ANY_APPROVE(5, "多人或签(一名审批人通过即可)"), // 或签（通过只需一人，拒绝只需一人）
    SEQUENTIAL_APPROVE(6, "依次审批"); // 依次审批

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
