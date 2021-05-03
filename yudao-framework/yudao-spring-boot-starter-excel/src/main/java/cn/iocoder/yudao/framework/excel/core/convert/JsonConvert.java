package cn.iocoder.yudao.framework.excel.core.convert;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

/**
 * Excel Json 转换器
 *
 * @author 芋道源码
 */
public class JsonConvert implements Converter<Object> {

    @Override
    public Class<?> supportJavaTypeKey() {
        throw new UnsupportedOperationException("暂不支持，也不需要");
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        throw new UnsupportedOperationException("暂不支持，也不需要");
    }

    @Override
    public Object convertToJavaData(CellData cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        throw new UnsupportedOperationException("暂不支持，也不需要");
    }

    @Override
    public CellData<String> convertToExcelData(Object value, ExcelContentProperty contentProperty,
                                               GlobalConfiguration globalConfiguration) {
        // 生成 Excel 小表格
        return new CellData<>(JsonUtils.toJsonString(value));
    }

}
