package cn.iocoder.yudao.framework.excel.core.convert;

import java.time.LocalDateTime;
import java.time.ZoneId;

import cn.hutool.core.date.DatePattern;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

/**
 * @author 高巍
 * @since 2025-06-11 21:15:29
 */
public class DynamicTimeZoneLocalDateTimeConvert implements Converter<LocalDateTime> {

    private final ZoneId userZone;
    private final ZoneId systemZone;

    public static DynamicTimeZoneLocalDateTimeConvert build(ZoneId userZone, ZoneId systemZone) {
        return new DynamicTimeZoneLocalDateTimeConvert(userZone, systemZone);
    }

    public DynamicTimeZoneLocalDateTimeConvert(ZoneId userZone, ZoneId systemZone) {
        this.userZone = userZone;
        this.systemZone = systemZone;
    }

    @Override
    public Class<?> supportJavaTypeKey() {
        return LocalDateTime.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public LocalDateTime convertToJavaData(ReadCellData readCellData, ExcelContentProperty contentProperty,
                                           GlobalConfiguration globalConfiguration) {
        String valueAsString = readCellData.getStringValue();
        return LocalDateTime.parse(valueAsString, DatePattern.NORM_DATETIME_FORMATTER)
                .atZone(userZone)
                .withZoneSameInstant(systemZone)
                .toLocalDateTime();
    }

    @Override
    public WriteCellData<String> convertToExcelData(LocalDateTime value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        String timeAsString = value
                .atZone(systemZone)
                .withZoneSameInstant(userZone)
                .toLocalDateTime()
                .format(DatePattern.NORM_DATETIME_FORMATTER);
        return new WriteCellData<>(timeAsString);
    }
}
