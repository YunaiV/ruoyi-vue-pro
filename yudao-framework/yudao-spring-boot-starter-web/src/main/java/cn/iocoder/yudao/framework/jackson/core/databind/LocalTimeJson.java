package cn.iocoder.yudao.framework.jackson.core.databind;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_HOUR_MINUTE_SECOND;

public class LocalTimeJson {

    public static final LocalTimeSerializer SERIALIZER = new LocalTimeSerializer(DateTimeFormatter
            .ofPattern(FORMAT_HOUR_MINUTE_SECOND)
            .withZone(ZoneId.systemDefault()));

    public static final LocalTimeDeserializer DESERIALIZABLE = new LocalTimeDeserializer(DateTimeFormatter
            .ofPattern(FORMAT_HOUR_MINUTE_SECOND)
            .withZone(ZoneId.systemDefault()));

}
