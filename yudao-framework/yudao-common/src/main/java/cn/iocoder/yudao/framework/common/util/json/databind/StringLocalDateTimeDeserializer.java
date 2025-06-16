package cn.iocoder.yudao.framework.common.util.json.databind;

import cn.iocoder.yudao.framework.common.enums.TimeZoneEnum;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_FORMATTER;


public class StringLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    public static final StringLocalDateTimeDeserializer INSTANCE;

    private static final LocalDateTimeDeserializer ARRAY_LOCAL_DATE_TIME_DESERIALIZER = LocalDateTimeDeserializer.INSTANCE;

    static {
        INSTANCE = new StringLocalDateTimeDeserializer();
    }

    private StringLocalDateTimeDeserializer() {
    }

    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        //适配原本的数组格式
        if (jsonParser.isExpectedStartArrayToken()) {
            return ARRAY_LOCAL_DATE_TIME_DESERIALIZER.deserialize(jsonParser, deserializationContext);
        }
        String valueAsString = jsonParser.getValueAsString();
        return LocalDateTime.parse(valueAsString, NORM_DATETIME_FORMATTER)
                .atZone(TimeZoneEnum.UTC_ZONE_ID)
                .toLocalDateTime();
    }
}
