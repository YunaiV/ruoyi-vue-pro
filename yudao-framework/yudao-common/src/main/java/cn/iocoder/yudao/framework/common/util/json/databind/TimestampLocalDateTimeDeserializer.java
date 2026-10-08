package cn.iocoder.yudao.framework.common.util.json.databind;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.core.JacksonException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * LocalDateTime 反序列化器，支持毫秒时间戳、常见日期时间字符串和 ISO-8601 日期。
 *
 * @author 老五
 */
public class TimestampLocalDateTimeDeserializer extends ValueDeserializer<LocalDateTime> {

    public static final TimestampLocalDateTimeDeserializer INSTANCE = new TimestampLocalDateTimeDeserializer();

    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("-?\\d+");

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        String text = p.getString();
        if (StrUtil.isBlank(text)) {
            return null;
        }
        text = text.trim();
        // 情况一：毫秒时间戳
        if (TIMESTAMP_PATTERN.matcher(text).matches()) {
            return LocalDateTimeUtil.of(Long.parseLong(text));
        }
        // 情况二：ISO-8601 日期；标准解析保留时区偏移与纳秒精度
        if (text.contains("T")) {
            return LocalDateTimeUtil.of(DateTimeFormatter.ISO_DATE_TIME.parseBest(
                    text, Instant::from, LocalDateTime::from));
        }
        // 情况三：常见日期时间字符串
        return LocalDateTimeUtil.of(DateUtil.parse(text).toInstant());
    }

}
