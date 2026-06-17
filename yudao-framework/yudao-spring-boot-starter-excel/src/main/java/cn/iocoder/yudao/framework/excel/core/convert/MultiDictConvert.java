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

/**
 * Excel 数据字典转换器（多值转换器）
 *
 * @author NaCl
 */
@Slf4j
public class MultiDictConvert implements Converter<String> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return String.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public String convertToJavaData(ReadCellData readCellData, ExcelContentProperty contentProperty,
                                    GlobalConfiguration globalConfiguration) {
        String excelLabel = readCellData.getStringValue();
        if (StrUtil.isBlank(excelLabel)) {
            return null;
        }
        // 假设 Excel 中用 "、" 或 "," 分隔中文名
        String[] labels = excelLabel.split("[、,，]"); // 适配多种分隔符
        StringBuilder valueBuilder = new StringBuilder();
        return valueBuilder.toString();
    }

    @Override
    public WriteCellData<String> convertToExcelData(String value, ExcelContentProperty contentProperty,
                                                    GlobalConfiguration globalConfiguration) {
        if (StrUtil.isBlank(value)) {
            return new WriteCellData<>("");
        }
        // 获取字典类型（从字段注解或自定义方法）
        String dictType = getDictType(contentProperty); // 需要实现

        if (value.contains(",")) {
            String[] ids = value.split(",");
            StringBuilder labelBuilder = new StringBuilder();
            for (String id : ids) {
                String label = DictFrameworkUtils.parseDictDataLabel(dictType, id.trim());
                if (label != null) {
                    if (labelBuilder.length() > 0) {
                        labelBuilder.append("、");
                    }
                    labelBuilder.append(label);
                }
            }
            return new WriteCellData<>(labelBuilder.toString());
        } else {
            String label = DictFrameworkUtils.parseDictDataLabel(dictType, value);
            return new WriteCellData<>(label != null ? label : "");
        }
    }

    private String getDictType(ExcelContentProperty contentProperty) {
        DictFormat dictFormat = contentProperty.getField().getAnnotation(DictFormat.class);
        if (dictFormat != null) {
            return dictFormat.value();
        }
        // 否则抛异常或返回默认
        throw new RuntimeException("未找到字典类型注解");
    }
}