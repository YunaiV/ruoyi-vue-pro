package cn.iocoder.yudao.framework.excel.core.convert;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import org.apache.fesod.sheet.converters.Converter;
import org.apache.fesod.sheet.enums.CellDataTypeEnum;
import org.apache.fesod.sheet.metadata.GlobalConfiguration;
import org.apache.fesod.sheet.metadata.data.WriteCellData;
import org.apache.fesod.sheet.metadata.property.ExcelContentProperty;

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
    public WriteCellData<String> convertToExcelData(Object value, ExcelContentProperty contentProperty,
                                                    GlobalConfiguration globalConfiguration) {
        // 生成 Excel 小表格
        return new WriteCellData<>(JsonUtils.toJsonString(value));
    }

}
