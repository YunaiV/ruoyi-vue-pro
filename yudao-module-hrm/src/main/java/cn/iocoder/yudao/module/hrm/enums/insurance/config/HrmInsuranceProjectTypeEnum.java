package cn.iocoder.yudao.module.hrm.enums.insurance.config;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 社保项目类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmInsuranceProjectTypeEnum implements ArrayValuable<Integer> {

    PENSION_INSURANCE(1, "养老保险"),
    MEDICAL_INSURANCE(2, "医疗保险"),
    UNEMPLOYMENT_INSURANCE(3, "失业保险"),
    WORK_INJURY_INSURANCE(4, "工伤保险"),
    MATERNITY_INSURANCE(5, "生育保险"),
    SUPPLEMENTARY_MEDICAL_INSURANCE(6, "补充大病医疗"),
    SUPPLEMENTARY_PENSION_INSURANCE(7, "补充养老"),
    DISABILITY_INSURANCE(8, "残保金"),
    SOCIAL_SECURITY_CUSTOM(9, "社保自定义"),
    PROVIDENT_FUND(10, "公积金"),
    PROVIDENT_FUND_CUSTOM(11, "公积金自定义");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmInsuranceProjectTypeEnum::getType).toArray(Integer[]::new);

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

    public boolean isSocialSecurity() {
        return !isProvidentFund();
    }

    public boolean isProvidentFund() {
        return this == PROVIDENT_FUND || this == PROVIDENT_FUND_CUSTOM;
    }

    public boolean isCustom() {
        return this == SOCIAL_SECURITY_CUSTOM || this == PROVIDENT_FUND_CUSTOM;
    }

    public static HrmInsuranceProjectTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

    public static boolean isSocialSecurity(Integer type) {
        HrmInsuranceProjectTypeEnum typeEnum = valueOf(type);
        return typeEnum != null && typeEnum.isSocialSecurity();
    }

    public static boolean isProvidentFund(Integer type) {
        HrmInsuranceProjectTypeEnum typeEnum = valueOf(type);
        return typeEnum != null && typeEnum.isProvidentFund();
    }

}
