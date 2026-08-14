package cn.iocoder.yudao.module.hrm.enums.employee.config;

import cn.hutool.core.util.ArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * HRM 员工档案字段枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmEmployeeArchiveFieldEnum {

    NAME("name", "员工姓名", "基本信息", true, true, false),
    COUNTRY("country", "国家或地区", "基本信息", true, true, false),
    NATION("nation", "民族", "基本信息", true, true, false),
    ID_TYPE("idType", "证件类型", "基本信息", true, true, false),
    ID_NUMBER("idNumber", "证件号码", "基本信息", true, true, false),
    SEX("sex", "性别", "基本信息", true, true, false),
    NATIVE_PLACE("nativePlace", "籍贯", "基本信息", true, true, false),
    BIRTHDAY("birthday", "出生日期", "基本信息", true, true, false),
    AGE("age", "年龄", "基本信息", true, false, true),
    HIGHEST_EDUCATION("highestEducation", "最高学历", "基本信息", true, true, false),

    MOBILE("mobile", "手机号", "通讯信息", true, true, false),
    EMAIL("email", "邮箱", "通讯信息", true, true, false),
    ADDRESS("address", "户籍地址", "通讯信息", true, true, false);

    /**
     * 字段名称
     */
    private final String name;
    /**
     * 字段标题
     */
    private final String title;
    /**
     * 字段分组名称
     */
    private final String groupName;
    /**
     * 默认是否显示
     */
    private final Boolean defaultVisible;
    /**
     * 默认是否允许员工编辑
     */
    private final Boolean defaultEditable;
    /**
     * 是否锁定编辑
     */
    private final Boolean editableLocked;

    public static HrmEmployeeArchiveFieldEnum valueOfName(String name) {
        return ArrayUtil.firstMatch(item -> item.getName().equals(name), values());
    }

}
