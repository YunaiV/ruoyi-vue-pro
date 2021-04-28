package cn.iocoder.dashboard.framework.excel.core.convert;


import cn.hutool.core.convert.Convert;
import cn.iocoder.dashboard.framework.dict.core.dto.DictDataRespDTO;
import cn.iocoder.dashboard.framework.dict.core.util.DictUtils;
import cn.iocoder.dashboard.framework.excel.core.annotations.DictFormat;
import cn.iocoder.dashboard.modules.system.enums.dict.SysDictTypeEnum;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import lombok.extern.slf4j.Slf4j;

/**
 * Excel {@link DictDataRespDTO} 数据字典转换器
 *
 * @author 芋道源码
 */
@Slf4j
public class DictConvert implements Converter<Object> {

    @Override
    public Class<?> supportJavaTypeKey() {
        throw new UnsupportedOperationException("暂不支持，也不需要");
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        throw new UnsupportedOperationException("暂不支持，也不需要");
    }

    @Override
    public Object convertToJavaData(CellData cellData, ExcelContentProperty contentProperty,
                                    GlobalConfiguration globalConfiguration) {
        // 使用字典解析
        SysDictTypeEnum type = getType(contentProperty);
        String label = cellData.getStringValue();
        DictDataRespDTO dictData = DictUtils.parseDictDataFromCache(type.getValue(), label);
        if (dictData == null) {
            log.error("[convertToJavaData][type({}) 解析不掉 label({})]", type, label);
            return null;
        }
        // 将 String 的 value 转换成对应的属性
        Class<?> fieldClazz = contentProperty.getField().getType();
        return Convert.convert(fieldClazz, dictData.getValue());
    }

    @Override
    public CellData<String> convertToExcelData(Object object, ExcelContentProperty contentProperty,
                                               GlobalConfiguration globalConfiguration) {
        // 空时，返回空
        if (object == null) {
            return new CellData<>("");
        }

        // 使用字典格式化
        SysDictTypeEnum type = getType(contentProperty);
        String value = String.valueOf(object);
        DictDataRespDTO dictData = DictUtils.getDictDataFromCache(type.getValue(), value);
        if (dictData == null) {
            log.error("[convertToExcelData][type({}) 转换不了 label({})]", type, value);
            return new CellData<>("");
        }
        // 生成 Excel 小表格
        return new CellData<>(dictData.getLabel());
    }

    private static SysDictTypeEnum getType(ExcelContentProperty contentProperty) {
        return contentProperty.getField().getAnnotation(DictFormat.class).value();
    }

}
