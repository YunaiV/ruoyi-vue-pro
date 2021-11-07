package cn.iocoder.yudao.adminserver.modules.bpm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 流程状态
 */
@Getter
@AllArgsConstructor
public enum FlowStatusEnum {

    HANDLE(1, "处理中"),

    PASS(2, "审批通过"),

    REJECTED(3, "审批不通过");

    /**
     * 状态
     */
    private final Integer status;


    /**
     * 描述
     */
    private final String desc;
}
