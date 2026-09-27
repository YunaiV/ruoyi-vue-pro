package cn.iocoder.yudao.framework.common.util.json.databind;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * LocalDateTime 反序列化器，支持毫秒时间戳、常见日期时间字符串和 ISO-8601 日期。
 *
 * @author 老五
 */
public class TimestampLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    public static final TimestampLocalDateTimeDeserializer INSTANCE = new TimestampLocalDateTimeDeserializer();

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        if (StrUtil.isBlank(text)) {
            return null;
        }
        text = text.trim();
        if (text.matches("-?\\d+")) {
            return LocalDateTimeUtil.of(Long.parseLong(text));
        }
        // Hutool 5.x 的自动解析会忽略部分负时区偏移，并截断纳秒。
        if (text.contains("T")) {
            return LocalDateTimeUtil.of(DateTimeFormatter.ISO_DATE_TIME.parseBest(
                    text, Instant::from, LocalDateTime::from));
        }
        return LocalDateTimeUtil.of(DateUtil.parse(text).toInstant());
    }

}
