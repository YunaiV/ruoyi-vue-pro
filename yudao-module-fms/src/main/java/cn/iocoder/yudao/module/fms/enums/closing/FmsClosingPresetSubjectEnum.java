package cn.iocoder.yudao.module.fms.enums.closing;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * FMS 预置结转科目编码枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum FmsClosingPresetSubjectEnum {

    PRIOR_YEAR_ADJUSTMENT("6000", "以前年度损益调整"),
    PRIOR_YEAR_ADJUSTMENT_CLOSING("310415", "未分配利润"),
    CURRENT_YEAR_PROFIT("3103", "本年利润"),

    UNPAID_VAT("22210104", "转出未交增值税"),
    OUTPUT_VAT("222102", "销项税额"),
    VAT_PAYABLE("222101", "应交增值税"),
    CONSUMPTION_TAX("222121", "应交消费税"),

    EDUCATION_SURCHARGE("540310", "教育费附加"),
    EDUCATION_SURCHARGE_PAYABLE("222113", "应交教育费附加"),
    CITY_MAINTENANCE_TAX("540303", "城市维护建设税"),
    CITY_MAINTENANCE_TAX_PAYABLE("222117", "应交城市维护建设税"),
    LOCAL_EDUCATION_SURCHARGE("540313", "地方教育费附加"),
    LOCAL_EDUCATION_SURCHARGE_PAYABLE("222114", "应交地方教育费附加"),

    INCOME_TAX_EXPENSE("5801", "所得税费用"),
    INCOME_TAX_PAYABLE("222111", "应交所得税");

    /**
     * 科目编码
     */
    private final String code;
    /**
     * 科目名称
     */
    private final String name;
}
