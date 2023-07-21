package cn.iocoder.yudao.framework.excel.core.convert;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额转换器
 *
 * 金额单位：分
 *
 * @author 芋道源码
 */
public class MoneyConvert implements Converter<Integer> {

    @Override
    public Class<?> supportJavaTypeKey() {
        throw new UnsupportedOperationException("暂不支持，也不需要");
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        throw new UnsupportedOperationException("暂不支持，也不需要");
    }

    @Override
    public WriteCellData<String> convertToExcelData(Integer value, ExcelContentProperty contentProperty,
                                                    GlobalConfiguration globalConfiguration) {
        BigDecimal result = BigDecimal.valueOf(value)
                .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
        return new WriteCellData<>(result.toString());
    }

}
