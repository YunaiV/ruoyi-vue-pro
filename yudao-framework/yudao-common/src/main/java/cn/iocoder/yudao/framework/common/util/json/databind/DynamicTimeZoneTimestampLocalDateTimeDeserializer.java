package cn.iocoder.yudao.framework.common.util.json.databind;

import cn.iocoder.yudao.framework.common.enums.TimeZoneEnum;
import cn.iocoder.yudao.framework.common.enums.WebCommonEnum;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.TimeZone;


public class DynamicTimeZoneTimestampLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    public static final DynamicTimeZoneTimestampLocalDateTimeDeserializer INSTANCE;

    static {
        INSTANCE = new DynamicTimeZoneTimestampLocalDateTimeDeserializer();
    }

    private DynamicTimeZoneTimestampLocalDateTimeDeserializer() {
    }

    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        ZoneId formZoneId = TimeZone.getDefault().toZoneId();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (Objects.nonNull(requestAttributes)) {
            Object object = requestAttributes.getAttribute(WebCommonEnum.HTTP_HEADER_TIME_ZONE, RequestAttributes.SCOPE_REQUEST);
            if (Objects.nonNull(object) && object instanceof String) {
                formZoneId = ZoneId.of((String) object);
            }
        }

//        字符串转换为LocalDateTime
//        String valueAsString = jsonParser.getValueAsString();
//        return  LocalDateTime.parse(valueAsString, NORM_DATETIME_FORMATTER)
//                .atZone(formZoneId)
//                .withZoneSameInstant(TO_ZONE_ID)
//                .toLocalDateTime();

        long valueAsLong = jsonParser.getValueAsLong();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(valueAsLong), formZoneId);
        return localDateTime
                .atZone(formZoneId)
                .withZoneSameInstant(TimeZoneEnum.UTC_ZONE_ID)
                .toLocalDateTime();
    }
}
