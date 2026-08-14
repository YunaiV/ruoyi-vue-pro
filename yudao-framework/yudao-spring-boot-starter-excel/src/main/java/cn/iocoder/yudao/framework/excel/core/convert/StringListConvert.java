package cn.iocoder.yudao.framework.excel.core.convert;

import cn.hutool.core.collection.CollUtil;
import cn.idev.excel.converters.Converter;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;

import java.util.List;

/**
 * Excel 字符串列表转换器
 *
 * @author 芋道源码
 */
public class StringListConvert implements Converter<List<String>> {

    @Override
    public WriteCellData<String> convertToExcelData(List<String> value,
            ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        return new WriteCellData<>(CollUtil.isEmpty(value) ? "" : String.join("/", value));
    }

}
