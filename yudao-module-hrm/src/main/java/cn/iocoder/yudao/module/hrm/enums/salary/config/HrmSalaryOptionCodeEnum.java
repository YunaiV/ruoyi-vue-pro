package cn.iocoder.yudao.module.hrm.enums.salary.config;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * HRM 预置薪资项目编码枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmSalaryOptionCodeEnum implements ArrayValuable<Integer> {

    BASIC_SALARY(10101, "基本工资"),
    POST_SALARY(10102, "岗位工资"),
    MEAL_ALLOWANCE(20104, "餐补"),

    OVERTIME_PAY(180101, "加班工资"),
    LATE_DEDUCTION(190101, "迟到扣款"),
    EARLY_DEDUCTION(190102, "早退扣款"),
    ABSENTEEISM_DEDUCTION(190103, "旷工扣款"),
    LEAVE_DEDUCTION(190104, "假期扣款"),
    MISSING_CARD_DEDUCTION(190105, "缺卡扣款"),
    COMPREHENSIVE_DEDUCTION(190106, "综合扣款"),
    ATTENDANCE_DEDUCTION_TOTAL(200101, "考勤扣款合计"),

    PERSONAL_INSURANCE(100101, "个人社保"),
    PERSONAL_PROVIDENT_FUND(100102, "个人公积金"),
    CORPORATE_INSURANCE(110101, "公司社保"),
    CORPORATE_PROVIDENT_FUND(120101, "公司公积金"),

    EXPECTED_PAY(210101, "应发工资"),
    TAXABLE(220101, "应税工资"),
    PERSONAL_TAX(230101, "个人所得税"),
    REAL_PAY(240101, "实发工资"),

    LAST_MONTH_CUMULATIVE_INCOME(250101, "累计收入额（截至上月）"),
    LAST_MONTH_CUMULATIVE_DEDUCT_EXPENSE(250102, "累计减除费用（截至上月）"),
    LAST_MONTH_CUMULATIVE_SPECIAL_DEDUCTION(250103, "累计专项扣除（截至上月）"),
    LAST_MONTH_PREPAID_TAX(250105, "累计已预缴税额"),

    CHILD_EDUCATION_DEDUCTION(260101, "累计子女教育"),
    HOUSE_RENT_DEDUCTION(260102, "累计住房租金"),
    HOUSE_LOAN_DEDUCTION(260103, "累计住房贷款利息"),
    ELDERLY_SUPPORT_DEDUCTION(260104, "累计赡养老人"),
    CONTINUING_EDUCATION_DEDUCTION(260105, "累计继续教育"),

    CURRENT_CUMULATIVE_INCOME(270101, "累计收入额"),
    CURRENT_CUMULATIVE_DEDUCT_EXPENSE(270102, "累计减除费用"),
    CURRENT_CUMULATIVE_SPECIAL_DEDUCTION(270103, "累计专项扣除"),
    CURRENT_CUMULATIVE_ADDITIONAL_DEDUCTION(270104, "累计专项附加扣除"),
    CURRENT_CUMULATIVE_TAXABLE(270105, "累计应纳税所得额"),
    CURRENT_CUMULATIVE_TAX(270106, "累计应纳税额");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmSalaryOptionCodeEnum::getCode).toArray(Integer[]::new);

    /**
     * 系统计算的薪资项编码集合
     */
    public static final Set<Integer> COMPUTED_CODES = Collections.unmodifiableSet(SetUtils.asSet(
            EXPECTED_PAY.getCode(), TAXABLE.getCode(), PERSONAL_TAX.getCode(), REAL_PAY.getCode(),
            CURRENT_CUMULATIVE_INCOME.getCode(), CURRENT_CUMULATIVE_DEDUCT_EXPENSE.getCode(),
            CURRENT_CUMULATIVE_SPECIAL_DEDUCTION.getCode(), CURRENT_CUMULATIVE_ADDITIONAL_DEDUCTION.getCode(),
            CURRENT_CUMULATIVE_TAXABLE.getCode(), CURRENT_CUMULATIVE_TAX.getCode()));

    /**
     * 默认工资条模板的薪资项编码
     */
    public static final List<Integer> SALARY_SLIP_DEFAULT_CODES = Collections.unmodifiableList(Arrays.asList(
            BASIC_SALARY.getCode(), POST_SALARY.getCode(), EXPECTED_PAY.getCode(),
            PERSONAL_TAX.getCode(), REAL_PAY.getCode()));

    /**
     * 默认工资条模板基本项编码
     */
    public static final Set<Integer> SALARY_SLIP_BASIC_CODES = Collections.unmodifiableSet(SetUtils.asSet(
            EXPECTED_PAY.getCode(), PERSONAL_TAX.getCode(), REAL_PAY.getCode()));

    /**
     * 上月累计薪资项编码集合
     */
    public static final List<Integer> LAST_MONTH_CUMULATIVE_CODES = Collections.unmodifiableList(Arrays.asList(
            LAST_MONTH_CUMULATIVE_INCOME.getCode(), LAST_MONTH_CUMULATIVE_DEDUCT_EXPENSE.getCode(),
            LAST_MONTH_CUMULATIVE_SPECIAL_DEDUCTION.getCode(), LAST_MONTH_PREPAID_TAX.getCode()));

    /**
     * 员工薪资信息导入排除的父薪资项编码集合
     */
    public static final Set<Integer> EMPLOYEE_INFO_IMPORT_EXCLUDED_PARENT_CODES =
            Collections.unmodifiableSet(SetUtils.asSet(180, 190, 200, 210, 220, 230, 240, 250, 260));

    /**
     * 编码
     */
    private final Integer code;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmSalaryOptionCodeEnum valueOf(Integer code) {
        return ArrayUtil.firstMatch(item -> item.getCode().equals(code), values());
    }

}
