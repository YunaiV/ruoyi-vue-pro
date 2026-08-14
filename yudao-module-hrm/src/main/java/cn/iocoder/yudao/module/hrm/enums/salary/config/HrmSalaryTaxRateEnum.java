package cn.iocoder.yudao.module.hrm.enums.salary.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * HRM 薪资个人所得税税率档位枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmSalaryTaxRateEnum {

    // ==================== 工资薪金所得税 ====================

    SALARY_LEVEL_1(HrmSalaryTaxTypeEnum.SALARY.getType(), new BigDecimal("36000"), 3, BigDecimal.ZERO),
    SALARY_LEVEL_2(HrmSalaryTaxTypeEnum.SALARY.getType(), new BigDecimal("144000"), 10, new BigDecimal("2520")),
    SALARY_LEVEL_3(HrmSalaryTaxTypeEnum.SALARY.getType(), new BigDecimal("300000"), 20, new BigDecimal("16920")),
    SALARY_LEVEL_4(HrmSalaryTaxTypeEnum.SALARY.getType(), new BigDecimal("420000"), 25, new BigDecimal("31920")),
    SALARY_LEVEL_5(HrmSalaryTaxTypeEnum.SALARY.getType(), new BigDecimal("660000"), 30, new BigDecimal("52920")),
    SALARY_LEVEL_6(HrmSalaryTaxTypeEnum.SALARY.getType(), new BigDecimal("960000"), 35, new BigDecimal("85920")),
    SALARY_LEVEL_7(HrmSalaryTaxTypeEnum.SALARY.getType(), null, 45, new BigDecimal("181920")),

    // ==================== 劳务报酬所得税 ====================

    REMUNERATION_LEVEL_1(HrmSalaryTaxTypeEnum.REMUNERATION.getType(), new BigDecimal("20000"), 20, BigDecimal.ZERO),
    REMUNERATION_LEVEL_2(HrmSalaryTaxTypeEnum.REMUNERATION.getType(), new BigDecimal("50000"), 30,
            new BigDecimal("2000")),
    REMUNERATION_LEVEL_3(HrmSalaryTaxTypeEnum.REMUNERATION.getType(), null, 40, new BigDecimal("7000"));

    /**
     * 计税类型
     */
    private final Integer taxType;
    /**
     * 应纳税所得额上限；为空表示不设上限
     */
    private final BigDecimal taxableUpperLimit;
    /**
     * 税率，单位：百分比
     */
    private final Integer rate;
    /**
     * 速算扣除数
     */
    private final BigDecimal quickDeduction;

    /**
     * 获得应纳税所得额对应的税率档位
     *
     * @param taxType 计税类型
     * @param taxable 应纳税所得额
     * @return 税率档位
     */
    public static HrmSalaryTaxRateEnum valueOf(Integer taxType, BigDecimal taxable) {
        for (HrmSalaryTaxRateEnum taxRate : values()) {
            if (!taxRate.getTaxType().equals(taxType)) {
                continue;
            }
            if (taxRate.getTaxableUpperLimit() == null
                    || taxable.compareTo(taxRate.getTaxableUpperLimit()) <= 0) {
                return taxRate;
            }
        }
        throw new IllegalArgumentException("不存在匹配的薪资个人所得税税率档位");
    }

}
