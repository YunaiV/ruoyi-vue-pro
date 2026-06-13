package cn.iocoder.yudao.module.bpm.enums.definition;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * BPM 条件表达式操作符枚举
 *
 * @author Lesan
 */
@RequiredArgsConstructor
@Getter
public enum BpmConditionOpCodeEnum {

    EQ("==", "等于", " var:getOrDefault(%s, null) == %s "),
    NE("!=", "不等于", " var:getOrDefault(%s, null) != %s "),
    GT(">", "大于", " var:getOrDefault(%s, null) > %s "),
    GE(">=", "大于等于", " var:getOrDefault(%s, null) >= %s "),
    LT("<", "小于", " var:getOrDefault(%s, null) < %s "),
    LE("<=", "小于等于", " var:getOrDefault(%s, null) <= %s "),

    CONTAINS("contain", "包含", " var:contains(%s, %s) "),
    NOT_CONTAINS("!contain", "不包含", " !var:contains(%s, %s) ");

    private final String code;
    private final String des;
    private final String symbol;

    public static BpmConditionOpCodeEnum fromCode(String code) {
        for (BpmConditionOpCodeEnum op : BpmConditionOpCodeEnum.values()) {
            if (op.code.equalsIgnoreCase(code)) {
                return op;
            }
        }
        throw new IllegalArgumentException("未知操作符: " + code);
    }

}
