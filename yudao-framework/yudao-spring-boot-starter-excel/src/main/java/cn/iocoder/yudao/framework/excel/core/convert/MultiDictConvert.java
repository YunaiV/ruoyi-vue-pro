package cn.iocoder.yudao.framework.excel.core.convert;

import cn.hutool.core.util.StrUtil;
import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Excel 数据字典转换器（多值转换器）
 *
 * @author NaCl
 */
@Slf4j
public class MultiDictConvert implements Converter<Object> {

    /**
     * Excel 中多值之间的分隔符（展示用中文顿号）
     */
    private static final String EXCEL_SEPARATOR = "、";

    /**
     * 数据库存储多值之间的分隔符（英文逗号）
     */
    private static final String DB_SEPARATOR = ",";

    @Override
    public Class<?> supportJavaTypeKey() {
        throw new UnsupportedOperationException("暂不支持，也不需要");
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        throw new UnsupportedOperationException("暂不支持，也不需要");
    }

    @Override
    public Object convertToJavaData(ReadCellData readCellData, ExcelContentProperty contentProperty,
                                    GlobalConfiguration globalConfiguration) {
        String excelValue = readCellData.getStringValue();
        // 空值处理
        if (StrUtil.isBlank(excelValue)) {
            return getEmptyValue(contentProperty.getField());
        }

        String type = getDictType(contentProperty);
        // 按 Excel 分隔符拆分（支持中文顿号、英文逗号、中文逗号）
        String[] labels = excelValue.split("[、,，]");
        List<String> valueList = new ArrayList<>();
        for (String label : labels) {
            String trimmed = label.trim();
            if (StrUtil.isBlank(trimmed)) {
                continue;
            }
            String value = DictFrameworkUtils.parseDictDataValue(type, trimmed);
            if (value != null) {
                valueList.add(value);
            } else {
                log.error("[convertToJavaData][type({}) 无法解析字典标签({})]", type, trimmed);
            }
        }

        // 按字段类型返回对应格式
        return convertToFieldType(valueList, contentProperty.getField());
    }

    @Override
    public WriteCellData<String> convertToExcelData(Object object, ExcelContentProperty contentProperty,
                                                    GlobalConfiguration globalConfiguration) {
        // 空值处理
        if (object == null) {
            return new WriteCellData<>("");
        }

        String type = getDictType(contentProperty);
        // 将字段值转换为字符串列表
        List<String> valueList = convertFieldToList(object);

        if (valueList.isEmpty()) {
            return new WriteCellData<>("");
        }

        // 逐个转换为字典标签
        List<String> labelList = new ArrayList<>();
        for (String value : valueList) {
            if (StrUtil.isBlank(value)) {
                continue;
            }
            String label = DictFrameworkUtils.parseDictDataLabel(type, value.trim());
            if (label != null) {
                labelList.add(label);
            } else {
                log.error("[convertToExcelData][type({}) 无法解析字典值({})]", type, value);
            }
        }

        if (labelList.isEmpty()) {
            return new WriteCellData<>("");
        }
        // 用中文顿号拼接
        return new WriteCellData<>(String.join(EXCEL_SEPARATOR, labelList));
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取字段上的 @DictFormat 注解值
     */
    private String getDictType(ExcelContentProperty contentProperty) {
        DictFormat annotation = contentProperty.getField().getAnnotation(DictFormat.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    String.format("字段 %s 缺少 @DictFormat 注解", contentProperty.getField().getName()));
        }
        return annotation.value();
    }

    /**
     * 根据字段类型返回对应的空值
     */
    private Object getEmptyValue(Field field) {
        Class<?> type = field.getType();
        if (type == String.class) {
            return "";
        } else if (type.isArray() && type.getComponentType() == String.class) {
            return new String[0];
        } else if (Collection.class.isAssignableFrom(type)) {
            return new ArrayList<>();
        }
        return null;
    }

    /**
     * 将 List<String> 转换为字段对应的类型
     */
    private Object convertToFieldType(List<String> valueList, Field field) {
        Class<?> type = field.getType();
        if (type == String.class) {
            // String 类型：用英文逗号拼接
            return String.join(DB_SEPARATOR, valueList);
        } else if (type.isArray() && type.getComponentType() == String.class) {
            // String[] 类型
            return valueList.toArray(new String[0]);
        } else if (type == List.class) {
            // List<String> 类型
            return new ArrayList<>(valueList);
        } else if (type == java.util.Set.class) {
            // Set<String> 类型
            return new java.util.LinkedHashSet<>(valueList);
        }
        // 其他类型默认返回拼接字符串
        return String.join(DB_SEPARATOR, valueList);
    }

    /**
     * 将字段值转换为字符串列表（支持多种类型）
     */
    @SuppressWarnings("unchecked")
    private List<String> convertFieldToList(Object object) {
        if (object == null) {
            return new ArrayList<>();
        }
        if (object instanceof String) {
            String str = (String) object;
            if (StrUtil.isBlank(str)) {
                return new ArrayList<>();
            }
            // 按英文逗号拆分
            return Arrays.stream(str.split(DB_SEPARATOR))
                    .map(String::trim)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toList());
        } else if (object instanceof Collection) {
            Collection<?> collection = (Collection<?>) object;
            List<String> result = new ArrayList<>();
            for (Object item : collection) {
                if (item != null) {
                    result.add(item.toString());
                }
            }
            return result;
        } else if (object.getClass().isArray()) {
            Object[] array = (Object[]) object;
            List<String> result = new ArrayList<>();
            for (Object item : array) {
                if (item != null) {
                    result.add(item.toString());
                }
            }
            return result;
        }
        // 其他类型直接转字符串
        //Java 8
        return new ArrayList<>(Arrays.asList(object.toString()));
        //Java 17
        //return new ArrayList<>(List.of(object.toString()));
    }
}