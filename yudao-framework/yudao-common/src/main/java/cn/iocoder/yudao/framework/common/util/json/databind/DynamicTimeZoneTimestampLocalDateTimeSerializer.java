package cn.iocoder.yudao.framework.common.util.json.databind;

import cn.iocoder.yudao.framework.common.enums.WebCommonConst;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.TimeZone;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_FORMATTER;

public class DynamicTimeZoneTimestampLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

    public static final ZoneId FROM_ZONE_ID = ZoneId.of("UTC");

    @Override
    public void serialize(LocalDateTime localDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ZoneId toZoneId = TimeZone.getDefault().toZoneId();
        if (Objects.nonNull(requestAttributes)) {
            Object object = requestAttributes.getAttribute(WebCommonConst.HTTP_HEADER_TIME_ZONE, RequestAttributes.SCOPE_REQUEST);
            if (Objects.nonNull(object) && object instanceof String) {
                toZoneId=  ZoneId.of((String)object);
            }
        }
        String utcTime = localDateTime
                .atZone(FROM_ZONE_ID)
                .withZoneSameInstant(toZoneId)
                .format(NORM_DATETIME_FORMATTER);
        jsonGenerator.writeString(utcTime);
    }
}
