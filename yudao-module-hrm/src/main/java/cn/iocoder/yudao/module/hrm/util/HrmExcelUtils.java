package cn.iocoder.yudao.module.hrm.util;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.number.MoneyUtils.priceScale;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.defaultIfNull;

/**
 * HRM Excel 工具类
 *
 * @author 芋道源码
 */
public class HrmExcelUtils {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("9999999.99");

    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("yyyy/M/d"),
            DateTimeFormatter.ofPattern("yyyy.M.d"),
            DateTimeFormatter.BASIC_ISO_DATE);

    /**
     * 获得并清理单元格文本
     *
     * @param row Excel 行
     * @param index 列下标
     * @return 单元格文本
     */
    public static String getCell(Map<Integer, String> row, int index) {
        return StrUtil.trim(MapUtil.getStr(row, index, StrUtil.EMPTY));
    }

    /**
     * 解析日期
     *
     * @param value 导入值
     * @param defaultValue 空值时的默认日期
     * @param required 是否必填
     * @param fieldName 字段名称
     * @return 日期
     */
    public static LocalDate parseDate(
            String value, LocalDate defaultValue, boolean required, String fieldName) {
        if (StrUtil.isBlank(value)) {
            if (required) {
                throw new IllegalArgumentException("请填写" + fieldName);
            }
            return defaultValue;
        }
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(value.trim(), formatter);
            } catch (DateTimeParseException ignored) {
                // 尝试下一个支持的日期格式
            }
        }
        throw new IllegalArgumentException(fieldName + "格式不正确，请使用 2026-07-01");
    }

    /**
     * 解析金额
     *
     * @param value 导入值
     * @param defaultValue 空值时的默认金额
     * @return 标准化后的金额
     */
    public static BigDecimal parseAmount(String value, BigDecimal defaultValue) {
        if (StrUtil.isBlank(value)) {
            return defaultIfNull(defaultValue, BigDecimal.ZERO);
        }
        String normalizedValue = StrUtil.removeAll(value, ",");
        if (!NumberUtil.isNumber(normalizedValue)) {
            throw new IllegalArgumentException("工资数据格式不正确");
        }
        BigDecimal amount = NumberUtil.toBigDecimal(normalizedValue);
        if (amount.abs().compareTo(MAX_AMOUNT) > 0) {
            throw new IllegalArgumentException("工资数据格式不正确");
        }
        return priceScale(amount);
    }

    private HrmExcelUtils() {
    }

}
