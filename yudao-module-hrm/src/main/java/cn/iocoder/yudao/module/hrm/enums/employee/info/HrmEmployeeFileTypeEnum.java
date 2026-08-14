package cn.iocoder.yudao.module.hrm.enums.employee.info;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 员工材料附件类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmEmployeeFileTypeEnum implements ArrayValuable<Integer> {

    ID_CARD_ORIGINAL(11, "身份证原件照片"),
    EDUCATION_CERTIFICATE(12, "学历证明"),
    PROFILE_PHOTO(13, "个人证件照"),
    ID_CARD_COPY(14, "身份证复印件"),
    SALARY_BANK_CARD(15, "工资银行卡"),
    SOCIAL_SECURITY_CARD(16, "社保卡"),
    HOUSING_FUND_CARD(17, "公积金卡"),
    AWARD_CERTIFICATE(18, "获奖证书"),
    BASIC_OTHER(19, "其他基本资料"),
    LABOR_CONTRACT(21, "劳动合同"),
    ENTRY_RESUME(22, "入职简历"),
    ENTRY_REGISTRATION(23, "入职登记表"),
    ENTRY_MEDICAL_REPORT(24, "入职体检单"),
    PREVIOUS_LEAVE_CERTIFICATE(25, "上家公司离职证明"),
    REGULAR_APPLICATION(26, "转正申请表"),
    ARCHIVE_OTHER(27, "其他档案资料"),
    LEAVE_APPROVAL(31, "离职审批"),
    LEAVE_CERTIFICATE(32, "离职证明"),
    LEAVE_OTHER(33, "其他离职资料");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmEmployeeFileTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmEmployeeFileTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
