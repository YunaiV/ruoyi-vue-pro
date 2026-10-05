package cn.iocoder.yudao.framework.common.util.json.databind;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link TimestampLocalDateTimeDeserializer} 的单元测试
 *
 * @author 芋道源码
 */
public class TimestampLocalDateTimeDeserializerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDateTime.class, TimestampLocalDateTimeDeserializer.INSTANCE);
        MAPPER.registerModule(module);
    }

    private LocalDateTime parse(String json) throws Exception {
        return MAPPER.readValue(json, LocalDateTime.class);
    }

    @Test
    public void testEpochMilliNumber() throws Exception {
        // 1700000000000 = 2023-11-14T22:13:20Z（本地时区换算后）
        LocalDateTime expected = LocalDateTime.ofInstant(Instant.ofEpochMilli(1700000000000L),
                ZoneId.systemDefault());
        assertEquals(expected, parse("1700000000000"));
    }

    @Test
    public void testEpochMilliString() throws Exception {
        LocalDateTime expected = LocalDateTime.ofInstant(Instant.ofEpochMilli(1700000000000L),
                ZoneId.systemDefault());
        assertEquals(expected, parse("\"1700000000000\""));
    }

    @Test
    public void testSpaceFormat() throws Exception {
        assertEquals(LocalDateTime.of(2026, 9, 11, 8, 0, 0), parse("\"2026-09-11 08:00:00\""));
    }

    @Test
    public void testSpaceFormatWithMillis() throws Exception {
        assertEquals(LocalDateTime.of(2026, 9, 11, 8, 0, 0, 123000000), parse("\"2026-09-11 08:00:00.123\""));
    }

    @Test
    public void testIsoLocal() throws Exception {
        assertEquals(LocalDateTime.of(2026, 9, 11, 8, 0, 0), parse("\"2026-09-11T08:00:00\""));
    }

    @Test
    public void testIsoLocalWithNanos() throws Exception {
        assertEquals(LocalDateTime.of(2026, 9, 11, 8, 0, 0, 123456789),
                parse("\"2026-09-11T08:00:00.123456789\""));
    }

    @Test
    public void testIsoWithOffset() throws Exception {
        // 前端 Date 对象序列化：带 Z（UTC）。2026-09-11T00:00:00Z 换算到系统时区
        LocalDateTime expected = OffsetDateTime.parse("2026-09-11T00:00:00Z")
                .atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        assertEquals(expected, parse("\"2026-09-11T00:00:00Z\""));
    }

    @Test
    public void testIsoWithOffsetMillis() throws Exception {
        // JSON.stringify(Date) 实际产物：带毫秒 + Z（无 value-format 的 Date 对象控件提交格式）
        LocalDateTime expected = OffsetDateTime.parse("2026-09-11T00:00:00.000Z")
                .atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        assertEquals(expected, parse("\"2026-09-11T00:00:00.000Z\""));
    }

    @Test
    public void testIsoWithOffsetPlus() throws Exception {
        // 带 +08:00 时区偏移的形式
        LocalDateTime expected = OffsetDateTime.parse("2026-09-11T08:00:00.000+08:00")
                .atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        assertEquals(expected, parse("\"2026-09-11T08:00:00.000+08:00\""));
    }

    @Test
    public void testDateOnly() throws Exception {
        assertEquals(LocalDateTime.of(2026, 9, 11, 0, 0, 0), parse("\"2026-09-11\""));
    }

    @Test
    public void testIsoWithNegativeOffsetAndMillis() throws Exception {
        LocalDateTime expected = OffsetDateTime.parse("2026-09-11T23:30:00.123-05:30")
                .atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        assertEquals(expected, parse("\"2026-09-11T23:30:00.123-05:30\""));
    }

    @Test
    public void testZeroAndNegativeEpochMilli() throws Exception {
        for (long millis : new long[]{0L, -1L}) {
            LocalDateTime expected = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis),
                    ZoneId.systemDefault());
            assertEquals(expected, parse(Long.toString(millis)));
            assertEquals(expected, parse("\"" + millis + "\""));
        }
    }

    @Test
    public void testSurroundingWhitespace() throws Exception {
        assertEquals(LocalDateTime.of(2026, 9, 11, 8, 0, 0), parse("\" 2026-09-11 08:00:00 \""));
    }

    @Test
    public void testNullAndEmpty() throws Exception {
        assertNull(parse("null"));
        assertNull(parse("\"\""));
        assertNull(parse("\"   \""));
    }

    @Test
    public void testHutoolDateFormats() throws Exception {
        assertEquals(LocalDateTime.of(2026, 9, 11, 0, 0), parse("\"2026/09/11\""));
        assertEquals(LocalDateTime.of(2026, 9, 11, 8, 0), parse("\"2026/09/11 08:00:00\""));
    }

    @Test
    public void testInvalidThrows() {
        // 调用，并断言异常
        assertThrows(Exception.class, () -> parse("\"not-a-date\""));
    }

}
