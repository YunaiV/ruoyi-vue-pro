package cn.iocoder.yudao.module.oa.enums.leave;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 请假类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaLeaveTypeEnum implements ArrayValuable<Integer> {

    ANNUAL(1, "年假", null),
    PERSONAL(2, "事假", 4),
    SICK(3, "病假", null),
    MARRIAGE(4, "婚假", 10),
    MATERNITY(5, "产假及哺乳假", null),
    PATERNITY(6, "陪产假", 10),
    BEREAVEMENT(7, "丧假", null);

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaLeaveTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;
    /**
     * 单次申请天数上限，为空时不限制；不包含年度累计额度
     */
    private final Integer maxDays;

    public static OaLeaveTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
