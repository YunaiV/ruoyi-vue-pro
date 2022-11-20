package cn.iocoder.yudao.framework.jackson.core.databind;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_HOUR_MINUTE_SECOND;

public class LocalTimeSerializable {
    // TODO: 2022/11/15 修改名称

    public static final LocalTimeSerializer INSTANCE = new LocalTimeSerializer(DateTimeFormatter
            .ofPattern(FORMAT_HOUR_MINUTE_SECOND)
            .withZone(ZoneId.systemDefault()));

}
