package cn.iocoder.yudao.framework.common.util.json.databind;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 基于时间戳的 LocalDateTime 反序列化器
 *
 * 兼容范围（覆盖前端/接口实际在用的全部格式，避免把"原先能静默入库"的格式变成报错）：
 * 1. epoch 毫秒：JSON 数字 / 数字字符串（原契约，保持不变）
 * 2. 常见日期时间字符串：yyyy-MM-dd HH:mm:ss（含毫秒）
 * 3. ISO-8601：yyyy-MM-ddTHH:mm:ss（本地 / 带时区，后者覆盖前端 Date 对象序列化）
 * 4. 纯日期：yyyy-MM-dd（当日零点）
 * 无法解析时抛清晰错误（含原始值），不再静默落 1970-01-01（消除静默数据损坏）。
 *
 * @author 老五
 */
public class TimestampLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    public static final TimestampLocalDateTimeDeserializer INSTANCE = new TimestampLocalDateTimeDeserializer();

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DT_MILLIS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // 1. JSON 数字 = epoch 毫秒（原契约，保持）
        if (p.currentToken() == JsonToken.VALUE_NUMBER_INT) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(p.getLongValue()), ZoneId.systemDefault());
        }
        String text = p.getText();
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        text = text.trim();
        // 1. 数字字符串 = epoch 毫秒
        if (text.matches("-?\\d+")) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(text)), ZoneId.systemDefault());
        }
        // 2. yyyy-MM-dd HH:mm:ss / .SSS
        try {
            return LocalDateTime.parse(text, DT);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(text, DT_MILLIS);
        } catch (DateTimeParseException ignored) {
        }
        // 3. ISO-8601 本地（2026-09-11T00:00:00 / .SSS）
        try {
            return LocalDateTime.parse(text);
        } catch (DateTimeParseException ignored) {
        }
        // 3. ISO-8601 带时区（前端 Date 对象序列化：2026-09-11T00:00:00.000Z / +08:00）
        try {
            return OffsetDateTime.parse(text).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
        }
        // 4. 纯日期（2026-09-11）→ 当日零点
        try {
            return LocalDate.parse(text).atStartOfDay();
        } catch (DateTimeParseException ignored) {
        }
        // 无法解析：抛清晰错误（不再静默落 1970）
        throw new IllegalArgumentException("无法解析时间值：" + text
                + "（支持 epoch 毫秒 / yyyy-MM-dd HH:mm:ss / ISO-8601 / yyyy-MM-dd）");
    }

}
