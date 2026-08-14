package cn.iocoder.yudao.module.hrm.enums.employee.experience;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 员工学历枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmEmployeeEducationEnum implements ArrayValuable<Integer> {

    PRIMARY_SCHOOL(1, "小学"),
    JUNIOR_HIGH_SCHOOL(2, "初中"),
    SECONDARY_TECHNICAL_SCHOOL(3, "中专"),
    SECONDARY_VOCATIONAL_SCHOOL(4, "中职"),
    TECHNICAL_SCHOOL(5, "技校"),
    HIGH_SCHOOL(6, "高中"),
    COLLEGE(7, "大专"),
    BACHELOR(8, "本科"),
    MASTER(9, "硕士"),
    DOCTOR(10, "博士"),
    POSTDOCTORAL(11, "博士后"),
    OTHER(12, "其他");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmEmployeeEducationEnum::getEducation).toArray(Integer[]::new);

    /**
     * 学历
     */
    private final Integer education;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmEmployeeEducationEnum valueOf(Integer education) {
        return ArrayUtil.firstMatch(item -> item.getEducation().equals(education), values());
    }

}
