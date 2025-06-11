package cn.iocoder.yudao.framework.common.util.json.databind;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_FORMATTER;

public class DynamicTimeZoneTimestampLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    public static final ZoneId TO_ZONE_ID= ZoneId.of("UTC");
    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        //TODO动态获取
        String valueAsString = jsonParser.getValueAsString();
        ZoneId formZoneId = ZoneId.of("UTC+8");
        return LocalDateTime.parse(valueAsString, NORM_DATETIME_FORMATTER)
                .atZone(formZoneId)
                .withZoneSameInstant(TO_ZONE_ID)
                .toLocalDateTime();
    }
}
