package cn.iocoder.yudao.module.bpm.enums.task;

import cn.hutool.core.util.ArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 流程实例的表单字段查询方式枚举
 *
 * 用于流程实例分页的 formFieldsParams 参数，值为 {op, value} 格式；值为标量时，按全等匹配，不使用该枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum BpmProcessInstanceFormFieldQueryTypeEnum {

    /**
     * 模糊匹配，值为字符串
     */
    LIKE("like", "模糊匹配"),
    /**
     * 数字范围，值为 [最小值, 最大值]
     */
    NUMBER_RANGE("numberRange", "数字范围"),
    /**
     * 日期区间，值为 [开始日期, 结束日期]，格式为 yyyy-MM-dd
     */
    DATE_RANGE("dateRange", "日期区间");

    /**
     * 查询方式，对应 op 参数
     */
    private final String type;
    /**
     * 名字
     */
    private final String name;

    public static BpmProcessInstanceFormFieldQueryTypeEnum of(String type) {
        return ArrayUtil.firstMatch(value -> value.getType().equals(type), values());
    }

}
