package cn.iocoder.yudao.framework.excel.core.convert;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Excel 多值数据字典转换器
 *
 * 数据库存储值使用半角逗号分隔，例如 {@code 1,2}
 * Excel 展示使用顿号分隔，例如 {@code 男、女}
 * 使用时，需要在字段上同时配置
 * {@code @ExcelProperty(converter = MultiDictConvert.class)} 和 {@link DictFormat}
 *
 * @author NaCl
 */
@Slf4j
public class MultiDictConvert implements Converter<Object> {

    private static final String EXCEL_SEPARATOR = "、";
    private static final String DB_SEPARATOR = ",";
    private static final String EXCEL_SEPARATOR_REGEX = "[、,，]";

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
        // 空时，返回空值
        Field field = contentProperty.getField();
        String labels = readCellData.getStringValue();
        if (StrUtil.isBlank(labels)) {
            return convertToFieldValue(field, new ArrayList<>());
        }

        // 使用字典解析
        String type = getType(contentProperty);
        String[] labelArray = labels.split(EXCEL_SEPARATOR_REGEX);
        List<String> values = new ArrayList<>(labelArray.length);
        for (String item : labelArray) {
            String label = item.trim();
            if (StrUtil.isBlank(label)) {
                continue;
            }
            String value = DictFrameworkUtils.parseDictDataValue(type, label);
            if (value == null) {
                log.error("[convertToJavaData][type({}) 解析不掉 label({})]", type, label);
                return null;
            }
            values.add(value);
        }
        // 将 String 的 value 转换成对应的属性
        return convertToFieldValue(field, values);
    }

    @Override
    public WriteCellData<String> convertToExcelData(Object object, ExcelContentProperty contentProperty,
                                                    GlobalConfiguration globalConfiguration) {
        // 空时，返回空
        if (object == null) {
            return new WriteCellData<>("");
        }

        // 使用字典格式化
        String type = getType(contentProperty);
        List<String> values = convertToStringList(object);
        List<String> labels = new ArrayList<>(values.size());
        for (String value : values) {
            String label = DictFrameworkUtils.parseDictDataLabel(type, value);
            if (label == null) {
                log.error("[convertToExcelData][type({}) 转换不了 value({})]", type, value);
                return new WriteCellData<>("");
            }
            labels.add(label);
        }
        // 生成 Excel 小表格
        return new WriteCellData<>(String.join(EXCEL_SEPARATOR, labels));
    }

    private static Object convertToFieldValue(Field field, List<String> values) {
        Class<?> fieldClazz = field.getType();
        if (String.class == fieldClazz) {
            return String.join(DB_SEPARATOR, values);
        }
        if (fieldClazz.isArray()) {
            return convertToArray(fieldClazz.getComponentType(), values);
        }
        if (Collection.class.isAssignableFrom(fieldClazz)) {
            return convertToCollection(field, values);
        }
        return Convert.convert(fieldClazz, String.join(DB_SEPARATOR, values));
    }

    private static Object convertToArray(Class<?> componentType, List<String> values) {
        Object array = Array.newInstance(componentType, values.size());
        for (int i = 0; i < values.size(); i++) {
            Array.set(array, i, Convert.convert(componentType, values.get(i)));
        }
        return array;
    }

    private static Collection<?> convertToCollection(Field field, List<String> values) {
        Class<?> elementClazz = getCollectionElementClazz(field);
        Collection<Object> result = createCollection(field.getType());
        for (String value : values) {
            result.add(Convert.convert(elementClazz, value));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Collection<Object> createCollection(Class<?> fieldClazz) {
        if (!fieldClazz.isInterface() && !Modifier.isAbstract(fieldClazz.getModifiers())) {
            try {
                return (Collection<Object>) fieldClazz.getDeclaredConstructor().newInstance();
            } catch (Exception ignored) {
                // 使用默认集合实现
            }
        }
        return Set.class.isAssignableFrom(fieldClazz) ? new LinkedHashSet<>() : new ArrayList<>();
    }

    private static Class<?> getCollectionElementClazz(Field field) {
        Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType)) {
            return String.class;
        }
        Type actualType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
        if (actualType instanceof Class<?>) {
            return (Class<?>) actualType;
        }
        if (actualType instanceof ParameterizedType
                && ((ParameterizedType) actualType).getRawType() instanceof Class<?>) {
            return (Class<?>) ((ParameterizedType) actualType).getRawType();
        }
        return String.class;
    }

    private static List<String> convertToStringList(Object object) {
        List<String> values = new ArrayList<>();
        if (object instanceof String) {
            String[] valueArray = ((String) object).split(DB_SEPARATOR);
            for (String value : valueArray) {
                addStringValue(values, value);
            }
            return values;
        }
        if (object instanceof Collection<?>) {
            for (Object item : (Collection<?>) object) {
                addStringValue(values, item);
            }
            return values;
        }
        if (object.getClass().isArray()) {
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++) {
                addStringValue(values, Array.get(object, i));
            }
            return values;
        }
        addStringValue(values, object);
        return values;
    }

    private static void addStringValue(List<String> values, Object value) {
        if (value == null) {
            return;
        }
        String str = String.valueOf(value).trim();
        if (StrUtil.isBlank(str)) {
            return;
        }
        values.add(str);
    }

    private static String getType(ExcelContentProperty contentProperty) {
        return contentProperty.getField().getAnnotation(DictFormat.class).value();
    }

}
