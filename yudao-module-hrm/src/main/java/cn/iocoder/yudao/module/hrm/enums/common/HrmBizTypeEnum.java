package cn.iocoder.yudao.module.hrm.enums.common;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * HRM 业务类型枚举
 *
 * @author 芋道源码
 */
@Getter
@RequiredArgsConstructor
public enum HrmBizTypeEnum implements ArrayValuable<Integer> {

    RECRUIT_POST(1, "招聘职位"),
    RECRUIT_CANDIDATE(2, "招聘候选人"),
    EMPLOYEE(3, "员工档案"),
    PERFORMANCE_ASSESSMENT(4, "绩效考核"),
    PERFORMANCE_PLAN(5, "绩效计划");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmBizTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;
    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
