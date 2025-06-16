package cn.iocoder.yudao.framework.common.util.json.databind;

import cn.iocoder.yudao.framework.common.enums.TimeZoneEnum;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDateTime;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_FORMATTER;

public class StringLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {
    public static final StringLocalDateTimeSerializer INSTANCE;

    static {
        INSTANCE = new StringLocalDateTimeSerializer();
    }

    private StringLocalDateTimeSerializer() {
    }


    @Override
    public void serialize(LocalDateTime localDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        //        LocalDateTime转换为字符串
        String utcTime = localDateTime
                .atZone(TimeZoneEnum.UTC_ZONE_ID)
                .toLocalDateTime()
                .format(NORM_DATETIME_FORMATTER);
        jsonGenerator.writeString(utcTime);
    }
}
